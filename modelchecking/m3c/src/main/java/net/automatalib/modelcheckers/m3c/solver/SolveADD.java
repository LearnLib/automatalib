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

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

public class SolveADD<L, AP> extends SolveDD<ADDTransformer, L, AP> {

    private BooleanVectorLogicDDManager ddManager;

    public SolveADD(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) {
        super(mcfps, formula, formulaIsCtl);
    }

    public SolveADD(ModalContextFreeProcessSystem<L, AP> mcfps, FormulaNode formula, boolean formulaIsCtl) {
        super(mcfps, formula, formulaIsCtl);
    }

    @Override
    protected void initDDManager() {
        this.ddManager = new BooleanVectorLogicDDManager(dependGraph.getNumVariables());
    }

    @Override
    protected ADDTransformer createInitTransformerEnd() {
        return new ADDTransformer(ddManager, dependGraph.getNumVariables());
    }

    @Override
    protected ADDTransformer createInitState() {
        return new ADDTransformer(ddManager, dependGraph);
    }

    @Override
    protected <TP extends ModalEdgeProperty> ADDTransformer createInitTransformerEdge(L edgeLabel, TP edgeProperty) {
        return new ADDTransformer(ddManager, edgeLabel, edgeProperty, dependGraph);
    }

}
