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
import org.checkerframework.checker.nullness.qual.Nullable;

public class ADDTransformer<L, AP> extends AbstractPropertyTransformer<ADDTransformer<L, AP>, L, AP> {

    private final BooleanVectorLogicDDManager xddManager;
    private final XDD<BooleanVector> add;

    public ADDTransformer(BooleanVectorLogicDDManager xddManager, XDD<BooleanVector> add) {
        super();
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
        super();
        this.xddManager = xddManager;
        boolean[] terminal = new boolean[dependGraph.getNumVariables()];
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
    public ADDTransformer(BooleanVectorLogicDDManager ddManager, int numberOfVars) {
        super();
        this.xddManager = ddManager;
        boolean[] falseArr = new boolean[numberOfVars];
        boolean[] trueArr = new boolean[numberOfVars];
        trueArr[0] = true;
        BooleanVector booleanVector = new BooleanVector(falseArr);
        XDD<BooleanVector> falseDD = xddManager.constant(booleanVector);
        XDD<BooleanVector> thenDD = xddManager.constant(new BooleanVector(trueArr));
        XDD<BooleanVector> tmpADD = xddManager.ithVar(0, thenDD, falseDD);
        thenDD.recursiveDeref();

        for (int i = 1; i < numberOfVars; i++) {
            trueArr = new boolean[numberOfVars];
            trueArr[i] = true;

            thenDD = xddManager.constant(new BooleanVector(trueArr));
            XDD<BooleanVector> proj = xddManager.ithVar(i, thenDD, falseDD);

            tmpADD = tmpADD.apply(BooleanVector::or, proj);

            thenDD.recursiveDeref();
            proj.recursiveDeref();
        }

        this.add = tmpADD;
    }

    /* Create the property transformer for an edge */
    public <TP extends ModalEdgeProperty> ADDTransformer(BooleanVectorLogicDDManager xddManager,
                                                         L edgeLabel,
                                                         TP edgeProperty,
                                                         DependencyGraph<L, AP> dependGraph) {
        //        super(edgeProperty.isMust());
        this.xddManager = xddManager;
        List<XDD<BooleanVector>> list = new ArrayList<>();
        for (FormulaNode<L, AP> node : dependGraph.getFormulaNodes()) {
            boolean[] terminal = new boolean[dependGraph.getNumVariables()];
            XDD<BooleanVector> falseDD = xddManager.constant(new BooleanVector(terminal));
            if (node instanceof AbstractModalFormulaNode) {
                L action = ((AbstractModalFormulaNode<L, AP>) node).getAction();
                if ((action == null || action.equals(edgeLabel)) &&
                    (!(node instanceof DiamondNode) || edgeProperty.isMust())) {
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

    @Override
    public ADDTransformer<L, AP> compose(ADDTransformer<L, AP> other, boolean isMust) {
        XDD<BooleanVector> otherAdd = other.getAdd();
        XDD<BooleanVector> compAdd = otherAdd.monadicApply(arg -> {
            boolean[] terminal = arg.data().clone();
            return this.getAdd().eval(terminal).v();
        });
        return new ADDTransformer<>(xddManager, compAdd, isMust);
    }

    @Override
    public ADDTransformer<L, AP> createUpdate(Set<AP> atomicPropositions,
                                              List<ADDTransformer<L, AP>> compositions,
                                              EquationalBlock<L, AP> currentBlock) {
        XDD<BooleanVector> updatedADD;
        DiamondOperation<AP> diamondOp = new DiamondOperation<>(atomicPropositions, currentBlock);
        if (compositions.isEmpty()) {
            updatedADD = this.add;
        } else if (compositions.size() == 1) {
            ADDTransformer<L, AP> succ = compositions.get(0);
            updatedADD = succ.getAdd().apply(diamondOp, succ.getAdd());
        } else {
            updatedADD = compositions.get(0).getAdd();
            for (int i = 1; i < compositions.size(); i++) {
                updatedADD = compositions.get(i).getAdd().apply(diamondOp, updatedADD);
            }
        }

        return new ADDTransformer<>(xddManager, updatedADD);
    }

    @Override
    public List<String> serialize() {
        XDDSerializer<BooleanVector> xddSerializer = new XDDSerializer<>();
        return Collections.singletonList(xddSerializer.serialize(add));
    }

    public XDD<BooleanVector> getAdd() {
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

        ADDTransformer<?, ?> that = (ADDTransformer<?, ?>) o;

        return Objects.equals(this.add, that.add);
    }
}
