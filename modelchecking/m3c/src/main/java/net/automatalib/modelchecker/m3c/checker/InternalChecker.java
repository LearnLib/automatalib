/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.checker;

import java.util.function.Function;

import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.NotNode;
import net.automatalib.modelchecker.m3c.solver.AbstractDDSolver;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import org.checkerframework.checker.nullness.qual.Nullable;

class InternalChecker<L, AP> {

    private final Function<? super ContextFreeModalProcessSystem<L, AP>, ? extends AbstractDDSolver<?, L, AP>> creator;

    private AbstractDDSolver<?, L, AP> instance;
    private ContextFreeModalProcessSystem<L, AP> lastModel;

    InternalChecker(Function<? super ContextFreeModalProcessSystem<L, AP>, ? extends AbstractDDSolver<?, L, AP>> creator) {
        this.creator = creator;
    }

    protected @Nullable WitnessTree<L, AP> doFindCounterExample(ContextFreeModalProcessSystem<L, AP> cfmps,
                                                                FormulaNode<L, AP> property) {

        final AbstractDDSolver<?, L, AP> solver;

        // reuse solver instance if we check multiple properties on the same model
        if (cfmps == lastModel) {
            solver = instance;
        } else {
            solver = creator.apply(cfmps);
            lastModel = cfmps;
            instance = solver;
        }

        return solver.findWitness(new NotNode<>(property));
    }
}
