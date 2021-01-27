package net.automatalib.modelcheckers.m3c.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import info.scce.addlib.dd.LabelledRegularDD;
import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanLogicDDManager;
import info.scce.addlib.viewer.DotViewer;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.ModalFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;

public class BDDTransformer extends PropertyTransformer {

    private final BDDManager bddManager;
    /* One BDD for each lhs of equation system */
    private BDD[] bdds;

    /* Initialize Property Transformer for a state */
    public BDDTransformer(BDDManager bddManager, DependencyGraph dependencyGraph) {
        this(bddManager);
        bdds = new BDD[dependencyGraph.getNumVariables()];
        for (EquationalBlock block : dependencyGraph.getBlocks()) {
            if (block.isMaxBlock()) {
                for (FormulaNode node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readOne();
                }
            } else {
                for (FormulaNode node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readLogicZero();
                }
            }
        }
    }

    public BDDTransformer(BDDManager bddManager) {
        this.bddManager = bddManager;
    }

    /* Create Property Transformer for an edge */
    public BDDTransformer(BDDManager bddManager, Edge edge, DependencyGraph dependencyGraph) {
        this(bddManager);
        bdds = new BDD[dependencyGraph.getNumVariables()];
        for (FormulaNode node : dependencyGraph.getFormulaNodes()) {
            int xi = node.getVarNumber();
            if (node instanceof ModalFormulaNode) {
                String action = ((ModalFormulaNode) node).getAction();
                /* action matches edgeLabel AND (node instanceof DiamondNode => edge.isMust) */
                if (edge.labelMatches(action) && (!(node instanceof DiamondNode) || edge.isMust())) {
                    int xj = node.getVarNumberLeft();
                    bdds[xi] = bddManager.ithVar(xj);
                } else if (node instanceof DiamondNode) {
                    bdds[xi] = bddManager.readLogicZero();
                } else if (node instanceof BoxNode) {
                    bdds[xi] = bddManager.readOne();
                }
            } else {
                bdds[xi] = bddManager.readLogicZero();
            }
        }
        isMust = edge.isMust();
    }

    /* The Property Transformer representing the identity function */
    public BDDTransformer(BDDManager bddManager, int numberOfVars) {
        this(bddManager);
        bdds = new BDD[numberOfVars];
        for (int var = 0; var < numberOfVars; var++) {
            bdds[var] = bddManager.ithVar(var);
        }
    }

    public BDDTransformer createUpdate(State state, List<BDDTransformer> compositions, EquationalBlock currentBlock) {
        BDDTransformer updatedTransformer = new BDDTransformer(bddManager);

        /* Set BDDs of updated transformer to initial bdds as we do not update all bdds
         * but only those for the current block */
        updatedTransformer.setBDDs(new BDD[bdds.length]);
        for (int var = 0; var < bdds.length; var++) {
            updatedTransformer.setBDD(var, bdds[var]);
        }
        for (FormulaNode node : currentBlock.getNodes()) {
            updateFormulaNode(state, compositions, updatedTransformer, node);
        }
        return updatedTransformer;
    }

    public void setBDDs(final BDD[] bdds) {
        this.bdds = bdds;
    }

    public void setBDD(int index, BDD bdd) {
        bdds[index] = bdd;
    }

    private void updateFormulaNode(State state,
                                   List<BDDTransformer> compositions,
                                   BDDTransformer updatedTransformer,
                                   FormulaNode node) {
        int varIdx = node.getVarNumber();
        BDD result;
        if (node instanceof BoxNode) {
            result = andBddList(compositions, varIdx);
        } else if (node instanceof DiamondNode) {
            result = orBddList(compositions, varIdx);
        } else if (node instanceof AndNode) {
            result =
                    updatedTransformer.getBdds()[node.getVarNumberLeft()].and(updatedTransformer.getBdds()[node.getVarNumberRight()]);
        } else if (node instanceof OrNode) {
            result =
                    updatedTransformer.getBdds()[node.getVarNumberLeft()].or(updatedTransformer.getBdds()[node.getVarNumberRight()]);
        } else if (node instanceof TrueNode) {
            result = bddManager.readOne();
        } else if (node instanceof FalseNode) {
            result = bddManager.readLogicZero();
        } else if (node instanceof NotNode) {
            result = bdds[node.getVarNumberLeft()].not();
        } else if (node instanceof AtomicNode) {
            String atomicProp = ((AtomicNode) node).getProposition();
            if (state.satisfiesAtomicProposition(atomicProp)) {
                result = bddManager.readOne();
            } else {
                result = bddManager.readLogicZero();
            }
        } else {
            throw new IllegalArgumentException("");
        }
        updatedTransformer.setBDD(varIdx, result);
    }

    public BDD andBddList(List<BDDTransformer> compositions, int var) {
        /* Conjunction over the var-th BDDs of compositions */
        Optional<BDD> result = compositions.stream().map(comp -> comp.getBdds()[var]).reduce(BDD::and);
        return result.orElseGet(bddManager::readOne);
    }

    public BDD orBddList(List<BDDTransformer> compositions, int var) {
        for (BDDTransformer comp : compositions) {
            if (!comp.isMust()) {
                throw new IllegalArgumentException("");
            }
        }
        /* Disjunction over the var-th BDDs of compositions */
        Optional<BDD> result =
                compositions.stream().filter(BDDTransformer::isMust).map(comp -> comp.getBdds()[var]).reduce(BDD::or);
        return result.orElseGet(bddManager::readLogicZero);
    }

    public BDD[] getBdds() {
        return this.bdds;
    }

    @Override
    public Set<Integer> evaluate(boolean[] input) {
        Set<Integer> output = new HashSet<>();
        for (int i = 0; i < bdds.length; i++) {
            if (bdds[i].eval(input).equals(bddManager.readOne())) {
                output.add(i);
            }
        }
        return output;
    }

    public BDDTransformer compose(PropertyTransformer other) {
        if (!(other instanceof BDDTransformer)) {
            throw new IllegalArgumentException("A BDDTransformer can only be composed with another BDDTransformer.");
        }

        BDDTransformer composition = new BDDTransformer(bddManager);
        composition.setBDDs(new BDD[bdds.length]);
        BDD[] otherBdds = ((BDDTransformer) other).getBdds();
        for (int var = 0; var < bdds.length; var++) {
            BDD composedBDD = bdds[var].vectorCompose(otherBdds);
            composition.setBDD(var, composedBDD);
        }
        return composition;
    }

    public void viewBDDs() {
        BooleanLogicDDManager xddManager = new BooleanLogicDDManager();
        DotViewer<XDD<Boolean>> dotViewer = new DotViewer<>();
        List<LabelledRegularDD<XDD<Boolean>>> xdds = new ArrayList<>();
        for (int i = 0; i < bdds.length; i++) {
            XDD<Boolean> xdd = bdds[i].toXDD(xddManager);
            LabelledRegularDD<XDD<Boolean>> labelledXDD = new LabelledRegularDD<>(xdd, "x" + i);
            xdds.add(labelledXDD);
        }
        dotViewer.view(xdds);
        dotViewer.waitUntilAllClosed();
    }

    public int getNumberOfVars() {
        return bdds.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bdds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BDDTransformer that = (BDDTransformer) o;

        return Arrays.equals(bdds, that.bdds);
    }
}
