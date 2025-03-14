/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.modelchecker.m3c.formula;

import java.io.IOException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Abstract super-class for unary (sub-) formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
public abstract class AbstractUnaryFormulaNode<L, AP> extends AbstractFormulaNode<L, AP> {

    private final FormulaNode<L, AP> child;

    public AbstractUnaryFormulaNode(FormulaNode<L, AP> child) {
        this.child = child;
    }

    public FormulaNode<L, AP> getChild() {
        return this.child;
    }

    public int getVarNumberChild() {
        return this.child.getVarNumber();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final AbstractUnaryFormulaNode<?, ?> that = (AbstractUnaryFormulaNode<?, ?>) o;

        return this.child.equals(that.child);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + child.hashCode();
        return result;
    }

    protected void printUnaryFormulaNode(Appendable a, String operator) throws IOException {
        a.append('(');
        a.append(operator);
        a.append(' ');
        getChild().print(a);
        a.append(')');
    }
}
