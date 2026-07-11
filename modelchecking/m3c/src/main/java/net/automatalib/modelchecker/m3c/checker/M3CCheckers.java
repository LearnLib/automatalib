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

import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.solver.ADDSolver;
import net.automatalib.modelchecker.m3c.solver.BDDSolver;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import net.automatalib.modelchecking.ModelChecker;

/**
 * A factory for constructing {@link ModelChecker}s for {@link ContextFreeModalProcessSystem}s. Currently, most model
 * checkers are restricted to returning {@link WitnessTree}s which are able to represent single-word violations.
 */
public final class M3CCheckers {

    private M3CCheckers() {
        // prevent instantiation
    }

    /**
     * Returns a default {@link ModelChecker} solver for string-based {@link ContextFreeModalProcessSystem}s. This
     * method currently delegates solver construction to {@link #bddChecker()}.
     *
     * @return a default {@link ModelChecker} solver for string-based systems
     *
     * @see #bddChecker()
     */
    public static ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>> checker() {
        return bddChecker();
    }

    /**
     * Returns a default {@link ModelChecker} solver for strongly-typed {@link ContextFreeModalProcessSystem}s. This
     * method currently delegates solver construction to {@link #typedBDDChecker()}.
     *
     * @param <L>
     *         label type
     * @param <AP>
     *         atomic proposition type
     *
     * @return a default {@link ModelChecker} solver for strongly-typed systems
     *
     * @see #typedBDDChecker()
     */
    public static <L, AP> ModelChecker<L, ContextFreeModalProcessSystem<L, AP>, FormulaNode<L, AP>, WitnessTree<L, AP>> typedChecker() {
        return typedBDDChecker();
    }

    /**
     * Returns an ADD-backed {@link ModelChecker} for string-based {@link ContextFreeModalProcessSystem}.
     *
     * @return an ADD-backed {@link ModelChecker} for string-based systems
     */
    public static ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>> addChecker() {
        return new GenericStringChecker(ADDSolver::new);
    }

    /**
     * Returns an ADD-backed {@link ModelChecker} for strongly-typed {@link ContextFreeModalProcessSystem}.
     *
     * @param <L>
     *         label type
     * @param <AP>
     *         atomic proposition type
     *
     * @return an ADD-backed {@link ModelChecker} for strongly-typed systems
     */
    public static <L, AP> ModelChecker<L, ContextFreeModalProcessSystem<L, AP>, FormulaNode<L, AP>, WitnessTree<L, AP>> typedADDChecker() {
        return new GenericTypedChecker<>(ADDSolver::new);
    }

    /**
     * Returns a BDD-backed {@link ModelChecker} for string-based {@link ContextFreeModalProcessSystem}.
     *
     * @return a BDD-backed {@link ModelChecker} for string-based systems
     */
    public static ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>> bddChecker() {
        return new GenericStringChecker(BDDSolver::new);
    }

    /**
     * Returns a BDD-backed {@link ModelChecker} for strongly-typed {@link ContextFreeModalProcessSystem}.
     *
     * @param <L>
     *         label type
     * @param <AP>
     *         atomic proposition type
     *
     * @return a BDD-backed {@link ModelChecker} for strongly-typed systems
     */
    public static <L, AP> ModelChecker<L, ContextFreeModalProcessSystem<L, AP>, FormulaNode<L, AP>, WitnessTree<L, AP>> typedBDDChecker() {
        return new GenericTypedChecker<>(BDDSolver::new);
    }

}
