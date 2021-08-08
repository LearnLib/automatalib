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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import info.scce.addlib.serializer.XDDSerializer;
import net.automatalib.modelcheckers.m3c.formula.AbstractModalFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author murtovi
 */
public class ADDTransformer<L, AP> extends AbstractPropertyTransformer<ADDTransformer<L, AP>, L, AP> {

    private final BooleanVectorLogicDDManager xddManager;
    private final @MonotonicNonNull XDD<BooleanVector> add;

    public ADDTransformer(BooleanVectorLogicDDManager xddManager, XDD<BooleanVector> add) {
        this.add = add;
        this.xddManager = xddManager;
    }

    public ADDTransformer(BooleanVectorLogicDDManager xddManager, XDD<BooleanVector> add, boolean isMust) {
        super(isMust);
        this.add = add;
        this.xddManager = xddManager;
    }

    /* Initialization of a state property transformer*/
    public ADDTransformer(BooleanVectorLogicDDManager xddManager, DependencyGraph<L, AP> dependGraph) {
        this.xddManager = xddManager;
        final boolean[] terminal = new boolean[dependGraph.getNumVariables()];
        for (EquationalBlock<L, AP> block : dependGraph.getBlocks()) {
            if (block.isMaxBlock()) {
                for (FormulaNode<L, AP> node : block.getNodes()) {
                    terminal[node.getVarNumber()] = true;
                }
            }
        }
        add = xddManager.constant(new BooleanVector(terminal));
    }

    /* Creates the identity function */
    // false-positive, see https://github.com/typetools/checker-framework/issues/2215
    @SuppressWarnings("assignment.type.incompatible")
    public ADDTransformer(BooleanVectorLogicDDManager ddManager) {
        this.xddManager = ddManager;
        this.add = null;
    }

    /* Create the property transformer for an edge */
    public <TP extends ModalEdgeProperty> ADDTransformer(BooleanVectorLogicDDManager xddManager,
                                                         L edgeLabel,
                                                         TP edgeProperty,
                                                         DependencyGraph<L, AP> dependGraph) {
        this.xddManager = xddManager;
        final List<XDD<BooleanVector>> list = new ArrayList<>();
        for (FormulaNode<L, AP> node : dependGraph.getFormulaNodes()) {
            final boolean[] terminal = new boolean[dependGraph.getNumVariables()];
            final XDD<BooleanVector> falseDD = xddManager.constant(new BooleanVector(terminal));
            if (node instanceof AbstractModalFormulaNode) {
                final AbstractModalFormulaNode<L, AP> modalNode = (AbstractModalFormulaNode<L, AP>) node;
                final L action = modalNode.getAction();
                if ((action == null || action.equals(edgeLabel)) &&
                    (!(modalNode instanceof DiamondNode) || edgeProperty.isMust())) {
                    int xj = modalNode.getVarNumberChild();
                    terminal[modalNode.getVarNumber()] = true;
                    final XDD<BooleanVector> thenDD = xddManager.constant(new BooleanVector(terminal));
                    final XDD<BooleanVector> id = xddManager.ithVar(xj, thenDD, falseDD);
                    list.add(id);
                } else if (modalNode instanceof BoxNode) {
                    terminal[modalNode.getVarNumber()] = true;
                    list.add(xddManager.constant(new BooleanVector(terminal)));
                }
            }
        }

        XDD<BooleanVector> tmpADD;
        if (list.isEmpty()) {
            tmpADD = xddManager.constant(new BooleanVector(new boolean[dependGraph.getNumVariables()]));
        } else {
            tmpADD = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                tmpADD = tmpADD.apply(BooleanVector::or, list.get(i));
            }
        }

        this.add = tmpADD;
    }

    @Override
    public Set<Integer> evaluate(boolean[] input) {
        final boolean[] leafData = this.add == null ? input : this.add.eval(input).v().data();
        final Set<Integer> satisfiedVars = new HashSet<>();
        for (int i = 0; i < leafData.length; i++) {
            if (leafData[i]) {
                satisfiedVars.add(i);
            }
        }
        return satisfiedVars;
    }

    @Override
    @SuppressWarnings("dereference.of.nullable") // false-positive
    public ADDTransformer<L, AP> compose(ADDTransformer<L, AP> other, boolean isMust) {
        final XDD<BooleanVector> compAdd;

        if (this.add == null && other.add == null) {
            throw new IllegalStateException("Two identity functions should never be composed");
        } else if (this.add == null) {
            compAdd = new XDD<>(other.add.ptr(), xddManager);
        } else if (other.add == null) {
            compAdd = new XDD<>(this.add.ptr(), xddManager);
        } else {
            compAdd = other.add.monadicApply(arg -> {
                boolean[] terminal = arg.data().clone();
                return this.add.eval(terminal).v();
            });
        }

        return new ADDTransformer<>(xddManager, compAdd, isMust);
    }

    @Override
    public ADDTransformer<L, AP> createUpdate(Set<AP> atomicPropositions,
                                              List<ADDTransformer<L, AP>> compositions,
                                              EquationalBlock<L, AP> currentBlock) {
        XDD<BooleanVector> updatedADD;
        final DiamondOperation<AP> diamondOp = new DiamondOperation<>(atomicPropositions, currentBlock);
        if (compositions.isEmpty()) {
            assert this.add != null : "The identity function should never be updated";
            //TODO: Test this
            updatedADD = this.add.monadicApply(new DiamondOperationDeadlock<>(atomicPropositions, currentBlock));
        } else {
            final XDD<BooleanVector> firstAdd = compositions.get(0).add;
            assert firstAdd != null : "The identity function should never be updated";
            updatedADD = preserveUpdatedTransformer(firstAdd, currentBlock);

            for (ADDTransformer<L, AP> composition : compositions) {
                assert composition.add != null : "The identity function should never be updated";
                updatedADD = updatedADD.apply(diamondOp, composition.add);
            }
        }

        return new ADDTransformer<>(xddManager, updatedADD);
    }

    public XDD<BooleanVector> preserveUpdatedTransformer(XDD<BooleanVector> rightDD,
                                                         EquationalBlock<L, AP> currentBlock) {
        assert this.add != null : "The identity function should never be updated";
        /* We create a new XDD where the information of this.add (the add before the update) is 'injected'
        into rightDD, the first composition DD, such that the bits corresponding to subformulas outside of the current
        block are preserved */
        return this.add.apply((booleanVectorBeforeUpdate, booleanVectorRight) -> {
            boolean[] result = booleanVectorBeforeUpdate.data().clone();
            for (FormulaNode<?, AP> node : currentBlock.getNodes()) {
                result[node.getVarNumber()] = booleanVectorRight.data()[node.getVarNumber()];
            }
            return new BooleanVector(result);
        }, rightDD);
    }

    @Override
    public List<String> serialize() {
        if (add == null) {
            return Collections.emptyList();
        }
        final XDDSerializer<BooleanVector> xddSerializer = new XDDSerializer<>();
        return Collections.singletonList(xddSerializer.serialize(add));
    }

    public @Nullable XDD<BooleanVector> getAdd() {
        return add;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(add);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ADDTransformer<?, ?> that = (ADDTransformer<?, ?>) o;

        return Objects.equals(this.add, that.add);
    }
}
