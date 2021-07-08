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

import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;

public final class SolverHistory<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, NodeIDs<?>> nodeIDs;
    private final Map<L, T> mustTransformers;
    private final Map<L, T> mayTransformers;
    private final List<SolverState<T, L, AP>> solverStates;

    SolverHistory(Map<L, NodeIDs<?>> nodeIDs,
                  Map<L, T> mustTransformers,
                  Map<L, T> mayTransformers,
                  List<SolverState<T, L, AP>> solverStates) {
        this.nodeIDs = nodeIDs;
        this.mustTransformers = mustTransformers;
        this.mayTransformers = mayTransformers;
        this.solverStates = solverStates;
    }

    public List<SolverState<T, L, AP>> getSolverStates() {
        return solverStates;
    }

    public Map<L, T> getMustTransformers() {
        return mustTransformers;
    }

    public Map<L, T> getMayTransformers() {
        return mayTransformers;
    }

    public Map<L, NodeIDs<?>> getNodeIDs() {
        return nodeIDs;
    }

    //TODO boolean isSat();

}
