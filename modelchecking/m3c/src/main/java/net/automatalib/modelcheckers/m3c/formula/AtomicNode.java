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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class AtomicNode<L, AP> extends FormulaNode<L, AP> {

    private final Set<AP> propositions;

    public AtomicNode(AP proposition) {
        this(Collections.singleton(proposition));
    }

    public AtomicNode(Set<AP> propositions) {
        this.propositions = propositions;
    }

    public Set<AP> getPropositions() {
        return propositions;
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T, L, AP> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(propositions);
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

        AtomicNode<?, ?> that = (AtomicNode<?, ?>) o;

        return Objects.equals(propositions, that.propositions);
    }

}
