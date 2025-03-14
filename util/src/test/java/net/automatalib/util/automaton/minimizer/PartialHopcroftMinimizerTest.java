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
package net.automatalib.util.automaton.minimizer;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.util.partitionrefinement.PruningMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class PartialHopcroftMinimizerTest extends AbstractMinimizationTest {

    private final PruningMode pruningMode;

    @Factory(dataProvider = "pruningModes")
    public PartialHopcroftMinimizerTest(PruningMode pruningMode) {
        this.pruningMode = pruningMode;
    }

    @DataProvider(name = "pruningModes")
    public static Object[][] pruningModes() {
        return new Object[][] {{PruningMode.PRUNE_BEFORE}, {PruningMode.PRUNE_AFTER}, {PruningMode.DONT_PRUNE}};
    }

    @Override
    protected <I> DFA<?, I> minimizeDFA(MutableDFA<?, I> dfa, Alphabet<I> alphabet) {
        return HopcroftMinimizer.minimizePartialDFA(dfa, alphabet, this.pruningMode);
    }

    @Override
    protected <I, O> MealyMachine<?, I, ?, O> minimizeMealy(MutableMealyMachine<?, I, ?, O> mealy,
                                                            Alphabet<I> alphabet) {
        return HopcroftMinimizer.minimizePartialMealy(mealy, alphabet, this.pruningMode);
    }

    @Override
    protected <I, SP, TP> UniversalDeterministicAutomaton<?, I, ?, SP, TP> minimizeUniversal(MutableDeterministic<?, I, ?, SP, TP> automaton,
                                                                                             Alphabet<I> alphabet) {
        return HopcroftMinimizer.minimizePartialUniversal(automaton, alphabet, this.pruningMode);
    }

    @Override
    protected boolean isPruned() {
        return this.pruningMode != PruningMode.DONT_PRUNE;
    }

    @Override
    protected boolean supportsPartial() {
        return true;
    }
}
