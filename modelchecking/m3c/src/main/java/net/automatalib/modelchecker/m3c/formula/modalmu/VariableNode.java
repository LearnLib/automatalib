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
package net.automatalib.modelchecker.m3c.formula.modalmu;

import java.io.IOException;
import java.util.Objects;

import net.automatalib.modelchecker.m3c.formula.AbstractFormulaNode;
import net.automatalib.modelchecker.m3c.formula.visitor.FormulaNodeVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Java representation of an "X" (sub-)formula.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
public class VariableNode<L, AP> extends AbstractFormulaNode<L, AP> {

    private final String variable;

    public VariableNode(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public void print(Appendable a) throws IOException {
        a.append(variable);
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T, L, AP> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(variable);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final VariableNode<?, ?> that = (VariableNode<?, ?>) o;

        return this.variable.equals(that.variable);
    }
}
