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
package net.automatalib.automaton.base;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.ShrinkableAutomaton;
import net.automatalib.automaton.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.concept.StateLocalInput;
import net.automatalib.common.util.mapping.ArrayMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.common.util.nid.DynamicList;
import net.automatalib.common.util.nid.IDChangeNotifier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Shared functionality for (non-) deterministic mutable automata.
 */
public abstract class AbstractFastMutable<S extends AbstractFastState<?>, I, T, SP, TP>
        implements ShrinkableAutomaton<S, I, T, SP, TP>,
                   UniversalFiniteAlphabetAutomaton<S, I, T, SP, TP>,
                   StateIDs<S>,
                   SupportsGrowingAlphabet<I>,
                   StateLocalInput<S, I> {

    protected final Alphabet<I> inputAlphabet;
    private final DynamicList<S> states = new DynamicList<>();
    private final IDChangeNotifier<S> tracker = new IDChangeNotifier<>();

    public AbstractFastMutable(Alphabet<I> inputAlphabet) {
        this.inputAlphabet = inputAlphabet;
    }

    @Override
    public int getStateId(S state) {
        return state.getId();
    }

    @Override
    public S getState(int id) {
        return states.get(id);
    }

    @Override
    public S addState(@Nullable SP property) {
        S newState = createState(property);
        states.add(newState);
        return newState;
    }

    @Override
    public void removeAllTransitions(S state) {
        state.clearTransitionObjects();
    }

    protected abstract S createState(@Nullable SP property);

    @Override
    public void removeState(S state, @Nullable S replacement) {
        ShrinkableAutomaton.unlinkState(this, state, replacement, inputAlphabet);
        states.remove(state, tracker);
    }

    @Override
    public void clear() {
        states.clear();
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public <V> MutableMapping<S, V> createDynamicStateMapping() {
        final ArrayMapping<S, V> mapping = new ArrayMapping<>(size());
        tracker.addListener(mapping, true);
        return mapping;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.inputAlphabet.containsSymbol(symbol)) {
            this.inputAlphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        final int newAlphabetSize = this.inputAlphabet.size();

        for (S s : this.getStates()) {
            s.ensureInputCapacity(newAlphabetSize);
        }
    }

    @Override
    public Collection<S> getStates() {
        return states;
    }

    @Override
    public StateIDs<S> stateIDs() {
        return this;
    }

}
