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

import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

/**
 * @author murtovi
 */
public class SolveBDD<L, AP> extends AbstractSolveDD<BDDTransformer<L, AP>, L, AP> {

    private BDDManager bddManager;

    public SolveBDD(ModalContextFreeProcessSystem<L, AP> mcfps) {
        super(mcfps);
    }

    @Override
    protected void initDDManager(DependencyGraph<L, AP> dependencyGraph) {
        this.bddManager = new BDDManager();
    }

    @Override
    protected BDDTransformer<L, AP> createInitTransformerEnd(DependencyGraph<L, AP> dependencyGraph) {
        return new BDDTransformer<>(bddManager, dependencyGraph.getNumVariables());
    }

    @Override
    protected BDDTransformer<L, AP> createInitState(DependencyGraph<L, AP> dependencyGraph) {
        return new BDDTransformer<>(bddManager, dependencyGraph);
    }

    @Override
    protected <TP extends ModalEdgeProperty> BDDTransformer<L, AP> createInitTransformerEdge(DependencyGraph<L, AP> dependencyGraph,
                                                                                             L edgeLabel,
                                                                                             TP edgeProperty) {
        return new BDDTransformer<>(bddManager, edgeLabel, edgeProperty, dependencyGraph);
    }

    @Override
    protected void shutdownDDManager() {
        this.bddManager.quit();
    }

    @Override
    protected SolverHistory.DDType getDDType() {
        return SolverHistory.DDType.BDD;
    }

}