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

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver.TypedM3CSolver;

/**
 * A factory for constructing {@link M3CSolver}s depending on the given {@link ModalContextFreeProcessSystem}.
 *
 * @author frohme
 */
public final class M3CSolvers {

    private M3CSolvers() {
        // prevent instantiation
    }

    public static M3CSolver<String> addSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveADD(mcfps);
    }

    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedADDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new TypedSolveADD<>(mcfps);
    }

    public static M3CSolver<String> bddSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveBDD(mcfps);
    }

    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedBDDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new TypedSolveBDD<>(mcfps);
    }
}
