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

import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.graph.TransitionEdge.Property;
import net.automatalib.automaton.impl.UniversalCompactDet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.minimizer.Block;
import net.automatalib.util.minimizer.MinimizationResult;
import net.automatalib.util.minimizer.Minimizer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MinimizerTest extends AbstractMinimizationTest {

    @Override
    protected <I> DFA<?, I> minimizeDFA(MutableDFA<?, I> dfa, Alphabet<I> alphabet) {
        final CompactDFA<I> result = new CompactDFA<>(alphabet, dfa.size());
        Automata.minimize(dfa, alphabet, result);
        return result;
    }

    @Override
    protected <I, O> MealyMachine<?, I, ?, O> minimizeMealy(MutableMealyMachine<?, I, ?, O> mealy,
                                                            Alphabet<I> alphabet) {
        final CompactMealy<I, O> result = new CompactMealy<>(alphabet, mealy.size());
        Automata.minimize(mealy, alphabet, result);
        return result;
    }

    @Override
    protected <I, SP, TP> UniversalDeterministicAutomaton<?, I, ?, SP, TP> minimizeUniversal(MutableDeterministic<?, I, ?, SP, TP> automaton,
                                                                                             Alphabet<I> alphabet) {
        final UniversalCompactDet<I, SP, TP> result = new UniversalCompactDet<>(alphabet, automaton.size());
        Automata.minimize(automaton, alphabet, result);
        return result;
    }

    @Override
    protected boolean isPruned() {
        return true;
    }

    @Override
    protected boolean supportsPartial() {
        return true;
    }

    /**
     * Test-case issue <a href="https://github.com/LearnLib/automatalib/issues/41">#41</a>.
     */
    @Test
    public void testIssue41() {
        testIssue41Internal(dfa -> Minimizer.minimize(dfa.transitionGraphView()));
        testIssue41Internal(dfa -> new Minimizer<Integer, Property<Character, Void>>().performMinimization(dfa.transitionGraphView()));
    }

    private void testIssue41Internal(Function<CompactDFA<Character>, MinimizationResult<Integer, ?>> minimizer) {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        final CompactDFA<Character> dfa = new CompactDFA<>(alphabet);

        final Integer initialState = dfa.addInitialState(false);
        final Integer stateAfterA = dfa.addState(true);
        final Integer stateAfterB = dfa.addState(true);
        dfa.setTransition(initialState, (Character) 'a', stateAfterA);
        dfa.setTransition(initialState, (Character) 'b', stateAfterB);

        final MinimizationResult<Integer, ?> result = minimizer.apply(dfa);
        Assert.assertEquals(result.getNumBlocks(), 2);

        final Block<Integer, ?> initBlock = result.getBlockForState(initialState);
        final Block<Integer, ?> afterABlock = result.getBlockForState(stateAfterA);
        final Block<Integer, ?> afterBBlock = result.getBlockForState(stateAfterB);

        Assert.assertEquals(afterABlock, afterBBlock);
        Assert.assertNotEquals(initBlock, afterABlock);
    }
}
