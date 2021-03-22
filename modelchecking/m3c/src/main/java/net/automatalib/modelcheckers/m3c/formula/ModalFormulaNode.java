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
import java.util.Objects;

public abstract class ModalFormulaNode<L, AP> extends UnaryFormulaNode<L, AP> {

    private final L action;

    public ModalFormulaNode(L action, FormulaNode<L, AP> node) {
        super(node);
        this.action = action;
    }

    public L getAction() {
        return action;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(action);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ModalFormulaNode<?, ?> that = (ModalFormulaNode<?, ?>) o;

        return Objects.equals(action, that.action);
    }

    protected void printMuCalcNode(Appendable a, char leftModalitySymbol, char rightModalitySymbol) throws IOException {
        a.append('(');
        a.append(leftModalitySymbol);

        if (action != null) {
            a.append(action.toString());
        }

        a.append(rightModalitySymbol);
        a.append(' ');
        getLeftChild().print(a);
        a.append(')');
    }

}
