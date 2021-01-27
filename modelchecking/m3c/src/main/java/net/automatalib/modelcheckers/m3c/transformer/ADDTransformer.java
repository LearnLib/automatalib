package net.automatalib.modelcheckers.m3c.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.ModalFormulaNode;

public class ADDTransformer extends PropertyTransformer {

    private final BooleanVectorLogicDDManager xddManager;
    private XDD<BooleanVector> add;

    public ADDTransformer(BooleanVectorLogicDDManager xddManager) {
        this.xddManager = xddManager;
    }

    /* Initialization of a state property transformer*/
    public ADDTransformer(BooleanVectorLogicDDManager xddManager, DependencyGraph dependGraph) {
        this.xddManager = xddManager;
        boolean[] terminal = new boolean[dependGraph.getNumVariables()];
        for (EquationalBlock block : dependGraph.getBlocks()) {
            if (block.isMaxBlock()) {
                for (FormulaNode node : block.getNodes()) {
                    terminal[node.getVarNumber()] = true;
                }
            }
        }
        add = xddManager.constant(new BooleanVector(terminal));
    }

    /* Creates the identity function */
    public ADDTransformer(BooleanVectorLogicDDManager ddManager, int numberOfVars) {
        this.xddManager = ddManager;
        boolean[] falseArr = new boolean[numberOfVars];
        boolean[] trueArr = new boolean[numberOfVars];
        trueArr[0] = true;
        BooleanVector booleanVector = new BooleanVector(falseArr);
        XDD<BooleanVector> falseDD = xddManager.constant(booleanVector);
        XDD<BooleanVector> thenDD = xddManager.constant(new BooleanVector(trueArr));
        add = xddManager.ithVar(0, thenDD, falseDD);
        thenDD.recursiveDeref();

        for (int i = 1; i < numberOfVars; i++) {
            trueArr = new boolean[numberOfVars];
            trueArr[i] = true;

            thenDD = xddManager.constant(new BooleanVector(trueArr));
            XDD<BooleanVector> proj = xddManager.ithVar(i, thenDD, falseDD);

            add = add.apply(BooleanVector::or, proj);

            thenDD.recursiveDeref();
            proj.recursiveDeref();
        }
    }

    /* Create the property transformer for an edge */
    public ADDTransformer(BooleanVectorLogicDDManager xddManager, Edge edge, DependencyGraph dependGraph) {
        this.xddManager = xddManager;
        List<XDD<BooleanVector>> list = new ArrayList<>();
        for (FormulaNode node : dependGraph.getFormulaNodes()) {
            boolean[] terminal = new boolean[dependGraph.getNumVariables()];
            XDD<BooleanVector> falseDD = xddManager.constant(new BooleanVector(terminal));
            if (node instanceof ModalFormulaNode) {
                String action = ((ModalFormulaNode) node).getAction();
                if (edge.labelMatches(action) && (!(node instanceof DiamondNode) || edge.isMust())) {
                    int xj = node.getVarNumberLeft();
                    terminal[node.getVarNumber()] = true;
                    XDD<BooleanVector> thenDD = xddManager.constant(new BooleanVector(terminal));
                    XDD<BooleanVector> id = xddManager.ithVar(xj, thenDD, falseDD);
                    list.add(id);
                } else if (node instanceof BoxNode) {
                    terminal[node.getVarNumber()] = true;
                    list.add(xddManager.constant(new BooleanVector(terminal)));
                }
            }
        }
        if (list.isEmpty()) {
            add = xddManager.constant(new BooleanVector(new boolean[dependGraph.getNumVariables()]));
        } else {
            add = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                add = add.apply(BooleanVector::or, list.get(i));
            }
        }
    }

    public ADDTransformer createUpdate(State state, List<ADDTransformer> compositions, EquationalBlock currentBlock) {
        ADDTransformer updatedTransformer = new ADDTransformer(xddManager);
        XDD<BooleanVector> updatedADD = null;
        DiamondOperation diamondOp = new DiamondOperation(state, currentBlock);
        if (compositions.size() == 1) {
            ADDTransformer succ = compositions.get(0);
            updatedADD = succ.getAdd().apply(diamondOp, succ.getAdd());
        } else if (compositions.size() > 1) {
            updatedADD = compositions.get(0).getAdd();
            for (int i = 1; i < compositions.size(); i++) {
                updatedADD = compositions.get(i).getAdd().apply(diamondOp, updatedADD);
            }
        }
        updatedTransformer.setAdd(updatedADD);
        return updatedTransformer;
    }

    public XDD<BooleanVector> getAdd() {
        return add;
    }

    public void setAdd(XDD<BooleanVector> add) {
        this.add = add;
    }

    @Override
    public Set<Integer> evaluate(boolean[] input) {
        XDD<BooleanVector> resultLeaf = add.eval(input);
        BooleanVector leafValue = resultLeaf.v();
        boolean[] leafData = leafValue.data();
        Set<Integer> satisfiedVars = new HashSet<>();
        for (int i = 0; i < leafData.length; i++) {
            if (leafValue.data()[i]) {
                satisfiedVars.add(i);
            }
        }
        return satisfiedVars;
    }

    public ADDTransformer compose(PropertyTransformer other) {
        if (!(other instanceof ADDTransformer)) {
            throw new IllegalArgumentException("An ADDTransformer can only be composed with another ADDTransformer");
        }
        ADDTransformer composition = new ADDTransformer(xddManager);
        XDD<BooleanVector> otherAdd = ((ADDTransformer) other).getAdd();
        XDD<BooleanVector> compAdd = otherAdd.monadicApply(arg -> {
            boolean[] terminal = arg.data().clone();
            return this.getAdd().eval(terminal).v();
        });
        composition.setAdd(compAdd);
        return composition;
    }

    @Override
    public int hashCode() {
        return add != null ? add.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ADDTransformer that = (ADDTransformer) o;

        return Objects.equals(add, that.add);
    }
}
