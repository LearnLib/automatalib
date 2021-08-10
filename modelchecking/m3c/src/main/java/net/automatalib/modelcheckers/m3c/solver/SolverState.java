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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

/**
 * @author murtovi
 */
public final class SolverState<N, T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final List<String> updatedPropTransformer;
    private final List<List<String>> compositions;
    private final List<FormulaNode<L, AP>> updatedStateSatisfiedSubformula;
    private final N updatedState;
    private final L updatedStateMPG;
    private final Map<L, Set<?>> workSet;

    SolverState(List<String> updatedPropTransformer,
                List<List<String>> compositions,
                N updatedState,
                L updatedStateMPG,
                Map<L, Set<?>> workSet,
                List<FormulaNode<L, AP>> updatedStateSatisfiedSubformula) {
        this.updatedPropTransformer = updatedPropTransformer;
        this.compositions = compositions;
        this.updatedState = updatedState;
        this.updatedStateMPG = updatedStateMPG;
        this.workSet = workSet;
        this.updatedStateSatisfiedSubformula = updatedStateSatisfiedSubformula;
    }

    public T getUpdatedPropTransformer(TransformerSerializer<T, L, AP> serializer) {
        return serializer.deserialize(this.updatedPropTransformer);
    }

    public List<T> getCompositions(TransformerSerializer<T, L, AP> serializer) {
        final List<T> result = new ArrayList<>(this.compositions.size());

        for (List<String> c : this.compositions) {
            result.add(serializer.deserialize(c));
        }

        return result;
    }

    public List<FormulaNode<L, AP>> getUpdatedStateSatisfiedSubformula() {
        return updatedStateSatisfiedSubformula;
    }

    public N getUpdatedState() {
        return updatedState;
    }

    public L getUpdatedStateMPG() {
        return updatedStateMPG;
    }

    public Map<L, Set<?>> getWorkSet() {
        return workSet;
    }

}
