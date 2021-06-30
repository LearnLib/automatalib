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

import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import net.automatalib.modelcheckers.m3c.formula.AbstractBinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;

public class DiamondOperation<AP> implements BinaryOperator<BooleanVector> {

    private final EquationalBlock<?, AP> block;
    private final Set<AP> atomicPropositions;

    public DiamondOperation(Set<AP> atomicPropositions, EquationalBlock<?, AP> block) {
        this.atomicPropositions = atomicPropositions;
        this.block = block;
    }

    @Override
    public BooleanVector apply(BooleanVector left, BooleanVector right) {
        boolean[] result = left.data().clone();
        for (FormulaNode<?, AP> node : block.getNodes()) {
            int currentVar = node.getVarNumber();
            if (node instanceof BoxNode) {
                result[currentVar] = result[currentVar] && right.data()[currentVar];
            } else if (node instanceof DiamondNode) {
                result[currentVar] = result[currentVar] || right.data()[currentVar];
            } else if (node instanceof AbstractBinaryFormulaNode) {
                int xj1 = node.getVarNumberLeft();
                int xj2 = node.getVarNumberRight();
                if (node instanceof AndNode) {
                    result[currentVar] = result[xj1] && result[xj2];
                } else {
                    result[currentVar] = result[xj1] || result[xj2];
                }
            } else if (node instanceof TrueNode) {
                result[currentVar] = true;
            } else if (node instanceof FalseNode) {
                result[currentVar] = false;
            } else if (node instanceof NotNode) {
                result[currentVar] = !result[node.getVarNumberLeft()];
            } else if (node instanceof AtomicNode) {
                @SuppressWarnings("unchecked")
                Set<AP> prop = ((AtomicNode<?, AP>) node).getPropositions();
                boolean satisfiesAtomicPropositions = false;
                for (AP ap : atomicPropositions) {
                    if (Objects.equals(ap, prop)) {
                        satisfiesAtomicPropositions = true;
                        break;
                    }
                }
                result[currentVar] = satisfiesAtomicPropositions;
            }
        }
        return new BooleanVector(result);
    }

}