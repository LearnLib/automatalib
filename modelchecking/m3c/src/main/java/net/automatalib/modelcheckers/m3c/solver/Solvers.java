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

public final class Solvers {

    private Solvers() {
        // prevent instantiation
    }

    public static Solver<String> stringADDSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveADD(mcfps);
    }

    public static <L, AP> Solver<FormulaNode<L, AP>> genericADDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new GenericSolveADD<>(mcfps);
    }

    public static Solver<String> stringBDDSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveBDD(mcfps);
    }

    public static <L, AP> Solver<FormulaNode<L, AP>> genericBDDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new GenericSolveBDD<>(mcfps);
    }
}
