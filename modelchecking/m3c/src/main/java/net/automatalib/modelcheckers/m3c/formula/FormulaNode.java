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
package net.automatalib.modelcheckers.m3c.formula;

import net.automatalib.commons.util.strings.Printable;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.NNFVisitor;

public interface FormulaNode<L, AP> extends Printable {

    FormulaNode<L, AP> getLeftChild();

    FormulaNode<L, AP> getRightChild();

    <T> T accept(FormulaNodeVisitor<T, L, AP> visitor);

    default FormulaNode<L, AP> toNNF() {
        return new NNFVisitor<L, AP>().transformToNNF(this);
    }

    int getVarNumberLeft();

    int getVarNumber();

    void setVarNumber(int varNumber);

    int getVarNumberRight();
}