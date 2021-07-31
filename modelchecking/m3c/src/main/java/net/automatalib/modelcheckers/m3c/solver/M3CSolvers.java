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

    /**
     * Returns a default {@link M3CSolver} solver for string-based modal context-free process systems. This method
     * currently delegates solver construction to {@link #bddSolver(ModalContextFreeProcessSystem)}.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return a default {@link M3CSolver} solver for string-based modal context-free process systems
     *
     * @see #bddSolver(ModalContextFreeProcessSystem)
     */
    public static M3CSolver<String> solver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return bddSolver(mcfps);
    }

    /**
     * Returns a default {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems. This
     * method currently delegates solver construction to {@link #typedBDDSolver(ModalContextFreeProcessSystem)}.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return a default {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems
     *
     * @see #typedBDDSolver(ModalContextFreeProcessSystem)
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return typedBDDSolver(mcfps);
    }

    /**
     * Returns an ADD-backed {@link M3CSolver} solver for string-based modal context-free process systems.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return an ADD-backed {@link M3CSolver} solver for string-based modal context-free process systems
     */
    public static M3CSolver<String> addSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveADD(mcfps);
    }

    /**
     * Returns an ADD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return an ADD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedADDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new TypedSolveADD<>(mcfps);
    }

    /**
     * Returns a BDD-backed {@link M3CSolver} solver for string-based modal context-free process systems.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return an ADD-backed {@link M3CSolver} solver for string-based modal context-free process systems
     */
    public static M3CSolver<String> bddSolver(ModalContextFreeProcessSystem<String, String> mcfps) {
        return new StringSolveBDD(mcfps);
    }

    /**
     * Returns a BDD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems.
     *
     * @param mcfps
     *         the modal context-free process system to evaluate formulae on
     *
     * @return a BDD-backed {@link TypedM3CSolver} solver for strongly-typed modal context-free process systems
     */
    public static <L, AP> TypedM3CSolver<FormulaNode<L, AP>> typedBDDSolver(ModalContextFreeProcessSystem<L, AP> mcfps) {
        return new TypedSolveBDD<>(mcfps);
    }
}