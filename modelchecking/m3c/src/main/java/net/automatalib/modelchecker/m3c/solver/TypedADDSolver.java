/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.solver;

import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.solver.M3CSolver.TypedM3CSolver;

/**
 * An {@link ADDSolver ADD solver} for strongly-typed formulas.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
public class TypedADDSolver<L, AP> extends ADDSolver<L, AP> implements TypedM3CSolver<FormulaNode<L, AP>> {

    TypedADDSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        super(cfmps);
    }

}
