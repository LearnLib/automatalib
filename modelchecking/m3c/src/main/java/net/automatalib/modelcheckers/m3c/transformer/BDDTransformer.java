/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import info.scce.addlib.dd.LabelledRegularDD;
import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanLogicDDManager;
import info.scce.addlib.viewer.DotViewer;
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
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

public class BDDTransformer<L, AP> extends PropertyTransformer<BDDTransformer<L, AP>, L, AP> {

    private final BDDManager bddManager;
    /* One BDD for each lhs of equation system */
    private BDD[] bdds;

    /* Initialize Property Transformer for a state */
    public BDDTransformer(BDDManager bddManager, DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager);
        bdds = new BDD[dependencyGraph.getNumVariables()];
        for (EquationalBlock<L, AP> block : dependencyGraph.getBlocks()) {
            if (block.isMaxBlock()) {
                for (FormulaNode<L, AP> node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readOne();
                }
            } else {
                for (FormulaNode<L, AP> node : block.getNodes()) {
                    bdds[node.getVarNumber()] = bddManager.readLogicZero();
                }
            }
        }
    }

    public BDDTransformer(BDDManager bddManager) {
        this.bddManager = bddManager;
    }

    /* Create Property Transformer for an edge */
    public <TP extends ModalEdgeProperty> BDDTransformer(BDDManager bddManager,
                                                         L edgeLabel,
                                                         TP edgeProperty,
                                                         DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager);
        bdds = new BDD[dependencyGraph.getNumVariables()];
        for (FormulaNode<L, AP> node : dependencyGraph.getFormulaNodes()) {
            int xi = node.getVarNumber();
            if (node instanceof ModalFormulaNode) {
                L action = ((ModalFormulaNode<L, AP>) node).getAction();
                /* action matches edgeLabel AND (node instanceof DiamondNode => edge.isMust) */
                if ((action == null || action.equals(edgeLabel)) &&
                    (!(node instanceof DiamondNode) || edgeProperty.isMust())) {
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
        isMust = edgeProperty.isMust();
    }

    /* The Property Transformer representing the identity function */
    public BDDTransformer(BDDManager bddManager, int numberOfVars) {
        this(bddManager);
        bdds = new BDD[numberOfVars];
        for (int var = 0; var < numberOfVars; var++) {
            bdds[var] = bddManager.ithVar(var);
        }
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

    @Override
    public BDDTransformer<L, AP> compose(BDDTransformer<L, AP> other) {
        BDDTransformer<L, AP> composition = new BDDTransformer<>(bddManager);
        composition.setBDDs(new BDD[bdds.length]);
        BDD[] otherBdds = other.getBdds();
        for (int var = 0; var < bdds.length; var++) {
            BDD composedBDD = bdds[var].vectorCompose(otherBdds);
            composition.setBDD(var, composedBDD);
        }
        return composition;
    }

    @Override
    public BDDTransformer<L, AP> createUpdate(Set<AP> atomicPropositions,
                                              List<BDDTransformer<L, AP>> compositions,
                                              EquationalBlock<L, AP> currentBlock) {
        BDDTransformer<L, AP> updatedTransformer = new BDDTransformer<>(bddManager);

        /* Set BDDs of updated transformer to initial bdds as we do not update all bdds
         * but only those for the current block */
        updatedTransformer.setBDDs(new BDD[bdds.length]);
        for (int var = 0; var < bdds.length; var++) {
            updatedTransformer.setBDD(var, bdds[var]);
        }
        for (FormulaNode<L, AP> node : currentBlock.getNodes()) {
            updateFormulaNode(atomicPropositions, compositions, updatedTransformer, node);
        }
        return updatedTransformer;
    }

    private void updateFormulaNode(Set<AP> atomicPropositions,
                                   List<BDDTransformer<L, AP>> compositions,
                                   BDDTransformer<L, AP> updatedTransformer,
                                   FormulaNode<L, AP> node) {
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
            Set<AP> atomicProp = ((AtomicNode<L, AP>) node).getPropositions();
            boolean satisfiesAtomicProp = false;
            for (AP ap : atomicPropositions) {
                if (Objects.equals(ap, atomicProp)) {
                    satisfiesAtomicProp = true;
                    break;
                }
            }
            if (satisfiesAtomicProp) {
                result = bddManager.readOne();
            } else {
                result = bddManager.readLogicZero();
            }
        } else {
            throw new IllegalArgumentException("");
        }
        updatedTransformer.setBDD(varIdx, result);
    }

    public BDD andBddList(List<BDDTransformer<L, AP>> compositions, int var) {
        /* Conjunction over the var-th BDDs of compositions */
        Optional<BDD> result = compositions.stream().map(comp -> comp.getBdds()[var]).reduce(BDD::and);
        return result.orElseGet(bddManager::readOne);
    }

    public BDD orBddList(List<BDDTransformer<L, AP>> compositions, int var) {
        for (BDDTransformer<L, AP> comp : compositions) {
            if (!comp.isMust()) {
                throw new IllegalArgumentException("");
            }
        }
        /* Disjunction over the var-th BDDs of compositions */
        Optional<BDD> result =
                compositions.stream().filter(BDDTransformer::isMust).map(comp -> comp.getBdds()[var]).reduce(BDD::or);
        return result.orElseGet(bddManager::readLogicZero);
    }

    public void setBDDs(final BDD[] bdds) {
        this.bdds = bdds;
    }

    public BDD[] getBdds() {
        return this.bdds;
    }

    public void setBDD(int index, BDD bdd) {
        bdds[index] = bdd;
    }

    public void viewBDDs() {
        DotViewer<XDD<Boolean>> dotViewer = new DotViewer<>();
        dotViewer.view(getXDDs());
        dotViewer.waitUntilAllClosed();
    }

    public List<LabelledRegularDD<XDD<Boolean>>> getXDDs() {
        BooleanLogicDDManager xddManager = new BooleanLogicDDManager();
        List<LabelledRegularDD<XDD<Boolean>>> xdds = new ArrayList<>();
        for (int i = 0; i < bdds.length; i++) {
            XDD<Boolean> xdd = bdds[i].toXDD(xddManager);
            LabelledRegularDD<XDD<Boolean>> labelledXDD = new LabelledRegularDD<>(xdd, "x" + i);
            xdds.add(labelledXDD);
        }
        return xdds;
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

        BDDTransformer<?, ?> that = (BDDTransformer<?, ?>) o;

        return Arrays.equals(bdds, that.bdds);
    }
}
