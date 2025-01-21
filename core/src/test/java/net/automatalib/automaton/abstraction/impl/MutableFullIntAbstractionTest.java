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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.impl.MutableAutomatonTest;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.annotations.Test;

public class MutableFullIntAbstractionTest extends MutableAutomatonTest {

    // disable tests for non-deterministic automata
    @Test(enabled = false)
    @Override
    public void testCompactNFA() {}

    // disable tests for non-deterministic automata
    @Test(enabled = false)
    @Override
    public void testFastNFA() {}

    // disable tests for non-deterministic automata
    @Test(enabled = false)
    @Override
    public void testFastProbMealy() {}

    // disable tests for non-deterministic automata
    @Test(enabled = false)
    @Override
    public void testCompactMTS() {}

    @Override
    protected <M extends MutableAutomaton<S, I, T, SP, TP>, S, I, T, SP, TP> M createInitialAutomaton(AutomatonCreator<M, I> creator,
                                                                                                      Alphabet<I> alphabet,
                                                                                                      int size) {
        final M automaton = creator.createAutomaton(alphabet, size);

        if (automaton instanceof MutableDeterministic) {

            @SuppressWarnings("unchecked")
            final M result = (M) new MockUp<>((MutableDeterministic<S, I, T, SP, TP>) automaton, alphabet);

            for (int i = 0; i < size; i++) {
                result.addState();
            }

            return result;
        } else {
            throw new IllegalArgumentException(automaton + " is not a " + MutableDeterministic.class);
        }
    }

    private static class MockUp<S, I, T, SP, TP> implements MutableDeterministic<S, I, T, SP, TP> {

        final MutableDeterministic<S, I, T, SP, TP> delegate;
        final MutableDeterministic.FullIntAbstraction<T, SP, TP> abstraction;
        final StateIDs<S> stateIDs;
        final Alphabet<I> alphabet;

        MockUp(MutableDeterministic<S, I, T, SP, TP> delegate, Alphabet<I> alphabet) {
            this.delegate = delegate;
            this.abstraction = delegate.fullIntAbstraction(alphabet);
            this.stateIDs = delegate.stateIDs();
            this.alphabet = alphabet;
        }

        @Override
        public Collection<S> getStates() {
            return IntStream.range(0, abstraction.size()).mapToObj(stateIDs::getState).collect(Collectors.toList());
        }

        @Override
        public T getTransition(S state, I input) {
            return abstraction.getTransition(stateIDs.getStateId(state), alphabet.getSymbolIndex(input));
        }

        @Override
        public SP getStateProperty(S state) {
            return abstraction.getStateProperty(stateIDs.getStateId(state));
        }

        @Override
        public TP getTransitionProperty(T transition) {
            return abstraction.getTransitionProperty(transition);
        }

        @Override
        public TP getTransitionProperty(S state, I input) {
            return abstraction.getTransitionProperty(stateIDs.getStateId(state), alphabet.getSymbolIndex(input));
        }

        @Override
        public S getSuccessor(T transition) {
            return stateIDs.getState(abstraction.getIntSuccessor(transition));
        }

        @Override
        public @Nullable S getSuccessor(S state, I input) {
            int succ = abstraction.getSuccessor(stateIDs.getStateId(state), alphabet.getSymbolIndex(input));
            return succ == FullIntAbstraction.INVALID_STATE ? null : stateIDs.getState(succ);
        }

        @Override
        public @Nullable S getInitialState() {
            final int intInitial = abstraction.getIntInitialState();
            return intInitial == FullIntAbstraction.INVALID_STATE ? null : stateIDs.getState(intInitial);
        }

        @Override
        public void setInitialState(@Nullable S state) {
            abstraction.setInitialState(state == null ? FullIntAbstraction.INVALID_STATE : stateIDs.getStateId(state));
        }

        @Override
        public void setTransition(S state, @Nullable I input, @Nullable T transition) {
            abstraction.setTransition(stateIDs.getStateId(state), alphabet.getSymbolIndex(input), transition);
        }

        @Override
        public void setTransition(S state, @Nullable I input, @Nullable S successor, @Nullable TP property) {
            abstraction.setTransition(stateIDs.getStateId(state),
                                      alphabet.getSymbolIndex(input),
                                      stateIDs.getStateId(successor),
                                      property);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public S addState(@Nullable SP property) {
            return stateIDs.getState(abstraction.addIntState(property));
        }

        @Override
        public S addState() {
            return stateIDs.getState(abstraction.addIntState());
        }

        @Override
        public S addInitialState() {
            return stateIDs.getState(abstraction.addIntInitialState());
        }

        @Override
        public S addInitialState(@Nullable SP property) {
            return stateIDs.getState(abstraction.addIntInitialState(property));
        }

        @Override
        public void setStateProperty(S state, @Nullable SP property) {
            abstraction.setStateProperty(stateIDs.getStateId(state), property);
        }

        @Override
        public void setTransitionProperty(T transition, @Nullable TP property) {
            abstraction.setTransitionProperty(transition, property);
        }

        @Override
        public void removeAllTransitions(S state) {
            for (int i = 0; i < alphabet.size(); i++) {
                abstraction.setTransition(stateIDs.getStateId(state), i, null);
            }
        }

        @Override
        public T createTransition(S successor, @Nullable TP properties) {
            return abstraction.createTransition(stateIDs.getStateId(successor), properties);
        }
    }
}
