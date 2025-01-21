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
package net.automatalib.automaton.abstraction.impl;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Symbol;
import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.DeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automaton.DeterministicAutomaton.StateIntAbstraction;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.util.TestUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeterministicAutomatonAbstractionTest {

    @Test
    public void testStateAbstraction() {

        final CompactMealy<Symbol<Character>, String> mealy = TestUtil.constructMealy(CompactMealy::new);
        final Delegator<Integer, Symbol<Character>, CompactTransition<String>> readOnly = new Delegator<>(mealy);

        final StateIntAbstraction<Symbol<Character>, CompactTransition<String>> abstraction =
                readOnly.stateIntAbstraction();

        Assert.assertEquals(abstraction.getIntInitialState(), 0);

        final CompactTransition<String> t0 = abstraction.getTransition(0, TestUtil.IN_A);

        Assert.assertEquals(abstraction.getSuccessor(0, TestUtil.IN_A), 1);
        Assert.assertEquals(abstraction.getIntSuccessor(t0), 1);

        final CompactTransition<String> t1 = abstraction.getTransition(1, TestUtil.IN_A);
        final CompactTransition<String> t2 = abstraction.getTransition(1, TestUtil.IN_B);

        Assert.assertEquals(abstraction.getSuccessor(1, TestUtil.IN_A), 2);
        Assert.assertEquals(abstraction.getIntSuccessor(t1), 2);
        Assert.assertEquals(abstraction.getSuccessor(1, TestUtil.IN_B), 0);
        Assert.assertEquals(abstraction.getIntSuccessor(t2), 0);
    }

    @Test
    public void testFullAbstraction() {

        final CompactMealy<Symbol<Character>, String> mealy = TestUtil.constructMealy(CompactMealy::new);
        final Alphabet<Symbol<Character>> alphabet = mealy.getInputAlphabet();
        final Delegator<Integer, Symbol<Character>, CompactTransition<String>> readOnly = new Delegator<>(mealy);

        final FullIntAbstraction<CompactTransition<String>> abstraction = readOnly.fullIntAbstraction(alphabet);

        Assert.assertEquals(abstraction.getIntInitialState(), 0);

        final CompactTransition<String> t0 = abstraction.getTransition(0, 0);

        Assert.assertEquals(abstraction.getSuccessor(0, 0), 1);
        Assert.assertEquals(abstraction.getIntSuccessor(t0), 1);

        final CompactTransition<String> t1 = abstraction.getTransition(1, 0);
        final CompactTransition<String> t2 = abstraction.getTransition(1, 1);

        Assert.assertEquals(abstraction.getSuccessor(1, 0), 2);
        Assert.assertEquals(abstraction.getIntSuccessor(t1), 2);
        Assert.assertEquals(abstraction.getSuccessor(1, 1), 0);
        Assert.assertEquals(abstraction.getIntSuccessor(t2), 0);
    }

    /**
     * Ensure that a (potentially mutable) automaton returns the correct abstraction to test.
     */
    private static class Delegator<S, I, T> implements DeterministicAutomaton<S, I, T> {

        private final DeterministicAutomaton<S, I, T> delegate;

        Delegator(DeterministicAutomaton<S, I, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Collection<S> getStates() {
            return this.delegate.getStates();
        }

        @Override
        public @Nullable T getTransition(S state, I input) {
            return this.delegate.getTransition(state, input);
        }

        @Override
        public S getSuccessor(T transition) {
            return this.delegate.getSuccessor(transition);
        }

        @Override
        public @Nullable S getInitialState() {
            return this.delegate.getInitialState();
        }
    }
}
