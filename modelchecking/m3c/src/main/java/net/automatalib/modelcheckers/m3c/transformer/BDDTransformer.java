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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.modelcheckers.m3c.formula.AbstractModalFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BDDTransformer<L, AP> extends AbstractPropertyTransformer<BDDTransformer<L, AP>, L, AP> {

    private final BDDManager bddManager;
    /* One BDD for each lhs of equation system */
    private final BDD[] bdds;

    public BDDTransformer(BDDManager bddManager, BDD[] bdds) {
        this.bddManager = bddManager;
        this.bdds = bdds;
    }

    /* Initialize Property Transformer for a state */
    public BDDTransformer(BDDManager bddManager, DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager, new BDD[dependencyGraph.getNumVariables()]);
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

    /* Create Property Transformer for an edge */
    public <TP extends ModalEdgeProperty> BDDTransformer(BDDManager bddManager,
                                                         L edgeLabel,
                                                         TP edgeProperty,
                                                         DependencyGraph<L, AP> dependencyGraph) {
        this(bddManager, new BDD[dependencyGraph.getNumVariables()]);
        for (FormulaNode<L, AP> node : dependencyGraph.getFormulaNodes()) {
            int xi = node.getVarNumber();
            if (node instanceof AbstractModalFormulaNode) {
                L action = ((AbstractModalFormulaNode<L, AP>) node).getAction();
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
        this(bddManager, new BDD[numberOfVars]);
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
        final BDD[] composedBDDs = new BDD[bdds.length];
        for (int var = 0; var < bdds.length; var++) {
            BDD composedBDD = bdds[var].vectorCompose(other.bdds);
            composedBDDs[var] = composedBDD;
        }
        return new BDDTransformer<>(bddManager, composedBDDs);
    }

    @Override
    public BDDTransformer<L, AP> createUpdate(Set<AP> atomicPropositions,
                                              List<BDDTransformer<L, AP>> compositions,
                                              EquationalBlock<L, AP> currentBlock) {
        /* Set BDDs of updated transformer to initial bdds as we do not update all bdds
         * but only those for the current block */
        final BDD[] updatedBDDs = bdds.clone();
        for (FormulaNode<L, AP> node : currentBlock.getNodes()) {
            updateFormulaNode(atomicPropositions, compositions, updatedBDDs, node);
        }
        return new BDDTransformer<>(bddManager, updatedBDDs);
    }

    private void updateFormulaNode(Set<AP> atomicPropositions,
                                   List<BDDTransformer<L, AP>> compositions,
                                   BDD[] updatedBDDs,
                                   FormulaNode<L, AP> node) {
        int varIdx = node.getVarNumber();
        BDD result;
        if (node instanceof BoxNode) {
            result = andBddList(compositions, varIdx);
        } else if (node instanceof DiamondNode) {
            result = orBddList(compositions, varIdx);
        } else if (node instanceof AndNode) {
            result = updatedBDDs[node.getVarNumberLeft()].and(updatedBDDs[node.getVarNumberRight()]);
        } else if (node instanceof OrNode) {
            result = updatedBDDs[node.getVarNumberLeft()].or(updatedBDDs[node.getVarNumberRight()]);
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
        updatedBDDs[varIdx] = result;
    }

    public BDD andBddList(List<BDDTransformer<L, AP>> compositions, int var) {
        /* Conjunction over the var-th BDDs of compositions */
        Optional<BDD> result = compositions.stream().map(comp -> comp.getBDD(var)).reduce(BDD::and);
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
                compositions.stream().filter(BDDTransformer::isMust).map(comp -> comp.getBDD(var)).reduce(BDD::or);
        return result.orElseGet(bddManager::readLogicZero);
    }

    public int getNumberOfVars() {
        return bdds.length;
    }

    public BDD getBDD(int var) {
        return bdds[var];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bdds);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BDDTransformer<?, ?> that = (BDDTransformer<?, ?>) o;

        return Arrays.equals(this.bdds, that.bdds);
    }
}
