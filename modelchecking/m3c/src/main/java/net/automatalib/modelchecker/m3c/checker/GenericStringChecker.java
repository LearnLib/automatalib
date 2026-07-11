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

import java.util.Collection;
import java.util.function.Function;

import net.automatalib.exception.FormatException;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.solver.AbstractDDSolver;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import net.automatalib.modelchecking.ModelChecker;
import org.checkerframework.checker.nullness.qual.Nullable;

class GenericStringChecker extends InternalChecker<String, String>
        implements ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>> {

    GenericStringChecker(Function<? super ContextFreeModalProcessSystem<String, String>, ? extends AbstractDDSolver<?, String, String>> creator) {
        super(creator);
    }

    @Override
    public @Nullable WitnessTree<String, String> findCounterExample(ContextFreeModalProcessSystem<String, String> cfmps,
                                                                    Collection<? extends String> inputs,
                                                                    String property) {

        final FormulaNode<String, String> formula;

        try {
            formula = M3CParser.parse(property);
        } catch (FormatException e) {
            throw new ModelCheckingException(e);
        }

        return doFindCounterExample(cfmps, formula);
    }
}
