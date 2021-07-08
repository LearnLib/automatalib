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

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class SolverState<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, List<T>> propTransformers;
    private final List<T> compositions;
    private final Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas;
    private final int updatedStateId;
    private final @Nullable L updatedStateMPG;
    private final Map<L, BitSet> workSet;

    SolverState(Map<L, List<T>> propTransformers,
                Map<L, BitSet> workSet,
                Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas) {
        this(propTransformers, Collections.emptyList(), -1, null, workSet, satisfiedSubformulas);
    }

    SolverState(Map<L, List<T>> propTransformers,
                List<T> compositions,
                int updatedStateId,
                @Nullable L updatedStateMPG,
                Map<L, BitSet> workSet,
                Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas) {
        this.propTransformers = propTransformers;
        this.compositions = compositions;
        this.updatedStateId = updatedStateId;
        this.updatedStateMPG = updatedStateMPG;
        this.workSet = workSet;
        this.satisfiedSubformulas = satisfiedSubformulas;
    }

    public Map<L, List<T>> getPropTransformers() {
        return propTransformers;
    }

    public List<T> getCompositions() {
        return compositions;
    }

    public int getUpdatedStateId() {
        return updatedStateId;
    }

    public @Nullable L getUpdatedStateMPG() {
        return updatedStateMPG;
    }

    public Map<L, BitSet> getWorkSet() {
        return workSet;
    }

    public Map<L, List<List<FormulaNode<L, AP>>>> getSatisfiedSubformulas() {
        return satisfiedSubformulas;
    }

}
