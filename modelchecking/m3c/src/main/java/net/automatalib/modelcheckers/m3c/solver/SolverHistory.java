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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.transformer.TransformerSerializer;

/**
 * @author murtovi
 */
public final class SolverHistory<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, SolverData<?, T, L, AP>> data;
    private final Map<L, List<String>> mustTransformers;
    private final Map<L, List<String>> mayTransformers;
    private final List<SolverState<?, T, L, AP>> solverStates;
    private final boolean isSat;

    SolverHistory(Map<L, SolverData<?, T, L, AP>> data,
                  Map<L, List<String>> mustTransformers,
                  Map<L, List<String>> mayTransformers,
                  List<SolverState<?, T, L, AP>> solverStates,
                  boolean isSat) {
        this.data = data;
        this.mustTransformers = mustTransformers;
        this.mayTransformers = mayTransformers;
        this.solverStates = solverStates;
        this.isSat = isSat;
    }

    public Map<L, SolverData<?, T, L, AP>> getData() {
        return data;
    }

    public Map<L, T> getMustTransformers(TransformerSerializer<T, L, AP> serializer) {
        return transform(this.mustTransformers, serializer);
    }

    public Map<L, T> getMayTransformers(TransformerSerializer<T, L, AP> serializer) {
        return transform(this.mayTransformers, serializer);
    }

    public List<SolverState<?, T, L, AP>> getSolverStates() {
        return solverStates;
    }

    public boolean isSat() {
        return isSat;
    }

    private Map<L, T> transform(Map<L, List<String>> input, TransformerSerializer<T, L, AP> serializer) {
        final Map<L, T> result = Maps.newHashMapWithExpectedSize(input.size());

        for (Entry<L, List<String>> e : input.entrySet()) {
            result.put(e.getKey(), serializer.deserialize(e.getValue()));
        }

        return result;
    }
}
