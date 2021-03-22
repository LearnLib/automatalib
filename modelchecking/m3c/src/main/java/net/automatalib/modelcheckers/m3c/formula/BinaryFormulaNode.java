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

import java.io.IOException;

public abstract class BinaryFormulaNode<L, AP> extends FormulaNode<L, AP> {

    public BinaryFormulaNode(FormulaNode<L, AP> leftChild, FormulaNode<L, AP> rightChild) {
        super(leftChild, rightChild);
    }

    protected void printBinaryFormulaNode(Appendable a, String operator) throws IOException {
        a.append('(');
        getLeftChild().print(a);
        a.append(' ');
        a.append(operator);
        a.append(' ');
        getRightChild().print(a);
        a.append(')');
    }

    protected void printUntilNode(Appendable a, char quantifier, char weakOrStrong) throws IOException {
        a.append('(');
        a.append(quantifier);
        a.append('(');
        getLeftChild().print(a);
        a.append(' ');
        a.append(weakOrStrong);
        a.append(' ');
        getRightChild().print(a);
        a.append("))");
    }

}
