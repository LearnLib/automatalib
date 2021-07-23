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
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

public final class SolverHistory<L, AP> {

    private final Map<L, NodeIDs<?>> nodeIDs;
    private final Map<L, Map<?, List<String>>> initialPropertyTransformers;
    private final Map<L, Map<?, List<FormulaNode<L, AP>>>> initialSatisfiedSubformulas;
    private final Map<L, List<String>> mustTransformers;
    private final Map<L, List<String>> mayTransformers;
    private final List<SolverState<?, L, AP>> solverStates;
    private final boolean isSat;
    private final DDType ddType;

    SolverHistory(Map<L, NodeIDs<?>> nodeIDs,
                  Map<L, Map<?, List<String>>> initialPropertyTransformers,
                  Map<L, Map<?, List<FormulaNode<L, AP>>>> initialSatisfiedSubformulas,
                  Map<L, List<String>> mustTransformers,
                  Map<L, List<String>> mayTransformers,
                  List<SolverState<?, L, AP>> solverStates,
                  boolean isSat,
                  DDType ddType) {
        this.nodeIDs = nodeIDs;
        this.initialPropertyTransformers = initialPropertyTransformers;
        this.initialSatisfiedSubformulas = initialSatisfiedSubformulas;
        this.mustTransformers = mustTransformers;
        this.mayTransformers = mayTransformers;
        this.solverStates = solverStates;
        this.isSat = isSat;
        this.ddType = ddType;
    }

    public Map<L, NodeIDs<?>> getNodeIDs() {
        return nodeIDs;
    }

    public Map<L, Map<?, List<String>>> getInitialPropertyTransformers() {
        return initialPropertyTransformers;
    }

    public Map<L, Map<?, List<FormulaNode<L, AP>>>> getInitialSatisfiedSubformulas() {
        return initialSatisfiedSubformulas;
    }

    public Map<L, List<String>> getMustTransformers() {
        return mustTransformers;
    }

    public Map<L, List<String>> getMayTransformers() {
        return mayTransformers;
    }

    public List<SolverState<?, L, AP>> getSolverStates() {
        return solverStates;
    }

    public boolean isSat() {
        return isSat;
    }

    public DDType getDDType() {
        return ddType;
    }

    public enum DDType {
        ADD,
        BDD
    }
}
