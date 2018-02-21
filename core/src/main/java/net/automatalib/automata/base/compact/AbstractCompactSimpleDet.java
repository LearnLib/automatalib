/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.automata.base.compact;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.ts.powerset.DeterministicPowersetView;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.SimpleAlphabet;

public abstract class AbstractCompactSimpleDet<I, SP> implements MutableDeterministic<Integer, I, Integer, SP, Void>,
                                                                 UniversalFiniteAlphabetAutomaton<Integer, I, Integer, SP, Void>,
                                                                 StateIDs<Integer>,
                                                                 MutableDeterministic.StateIntAbstraction<I, Integer, SP, Void>,
                                                                 MutableDeterministic.FullIntAbstraction<Integer, SP, Void>,
                                                                 GrowableAlphabetAutomaton<I>,
                                                                 Serializable {

    public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
    public static final int DEFAULT_INIT_CAPACITY = 11;

    protected Alphabet<I> alphabet;
    protected final float resizeFactor;
    protected int alphabetSize;
    protected int[] transitions;
    protected int stateCapacity;
    protected int numStates;
    protected int initial = INVALID_STATE;

    public AbstractCompactSimpleDet(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
        this.transitions = new int[stateCapacity * alphabetSize];
        Arrays.fill(this.transitions, 0, this.transitions.length, INVALID_STATE);
        this.resizeFactor = resizeFactor;
        this.stateCapacity = stateCapacity;
    }

    public AbstractCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactSimpleDet(Alphabet<I> alphabet, float resizeFactor) {
        this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
    }

    protected AbstractCompactSimpleDet(Alphabet<I> alphabet, AbstractCompactSimpleDet<?, ?> other) {
        this(alphabet, other.numStates, other.initial, other.transitions.clone(), other.resizeFactor);
    }

    protected AbstractCompactSimpleDet(Alphabet<I> alphabet,
                                       int numStates,
                                       int initial,
                                       int[] transitions,
                                       float resizeFactor) {
        this.alphabet = new SimpleAlphabet<>(alphabet);
        this.alphabetSize = alphabet.size();
        this.numStates = numStates;
        if (initial < 0 || initial >= numStates) {
            throw new IllegalArgumentException(
                    "Invalid initial state " + initial + " for automaton with " + numStates + " states");
        }
        this.initial = initial;
        if (transitions.length < numStates * alphabetSize) {
            throw new IllegalArgumentException(
                    "Transition array is not large enough for automaton with " + numStates + " states");
        }
        this.transitions = transitions;
        this.stateCapacity = transitions.length / alphabetSize;
        this.resizeFactor = resizeFactor;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public Collection<Integer> getStates() {
        return CollectionsUtil.intRange(0, numStates);
    }

    @Override
    public StateIDs<Integer> stateIDs() {
        return this;
    }

    @Override
    public int size() {
        return numStates;
    }

    @Override
    public int getStateId(Integer state) {
        return state.intValue();
    }

    @Override
    public Integer getState(int id) {
        return id;
    }

    @Override
    public Integer getState(Iterable<? extends I> input) {
        return wrapState(getIntState(input));
    }

    @Override
    public Integer getInitialState() {
        return wrapState(initial);
    }

    @Override
    public Integer getSuccessor(Integer state, Iterable<? extends I> input) {
        return wrapState(getIntSuccessor(state.intValue(), input));
    }

    @Override
    public Integer getSuccessor(Integer transition) {
        return transition;
    }

    @Override
    public int getSuccessor(int state, I input) {
        return getIntTransition(state, input);
    }

    @Override
    public int getSuccessor(int state, int input) {
        return getIntTransition(state, input);
    }

    public void setInitialState(int state) {
        this.initial = state;
    }

    @Override
    public void setInitialState(Integer state) {
        setInitialState((state != null) ? state.intValue() : INVALID_STATE);
    }

    @Override
    public Integer createTransition(int successor, Void property) {
        return wrapState(successor);
    }

    @Override
    public Integer createTransition(Integer successor, Void properties) {
        return successor;
    }

    public int addIntState() {
        return addIntState(null);
    }

    public int addIntState(SP property) {
        int stateId = numStates++;
        ensureCapacity(numStates);
        initState(stateId, property);
        return stateId;
    }

    public int addIntInitialState() {
        return addIntInitialState(null);
    }

    public int addIntInitialState(SP property) {
        int state = addIntState(property);
        setInitialState(state);
        return state;
    }

    public void ensureCapacity(int newCapacity) {
        if (newCapacity <= stateCapacity) {
            return;
        }

        int newCap = (int) (stateCapacity * resizeFactor);
        if (newCap < newCapacity) {
            newCap = newCapacity;
        }

        int[] newTrans = new int[newCap * alphabetSize];
        System.arraycopy(transitions, 0, newTrans, 0, stateCapacity * alphabetSize);
        Arrays.fill(newTrans, this.transitions.length, newTrans.length, INVALID_STATE);
        this.transitions = newTrans;
        ensureCapacity(stateCapacity, newCap);
        this.stateCapacity = newCap;
    }

    protected void ensureCapacity(int oldCap, int newCap) {
    }

    protected abstract void initState(int stateId, SP property);

    protected static Integer wrapState(int id) {
        if (id < 0) {
            return null;
        }
        return Integer.valueOf(id);
    }

    public int getIntState(Iterable<? extends I> input) {
        return getIntSuccessor(initial, input);
    }

    public int getIntSuccessor(int state, Iterable<? extends I> input) {
        int current = state;

        Iterator<? extends I> inputIt = input.iterator();

        while (current >= 0 && inputIt.hasNext()) {
            current = getIntSuccessor(current, inputIt.next());
        }

        return current;
    }

    @Override
    public int getIntSuccessor(Integer transition) {
        return unwrapState(transition);
    }

    public int getIntSuccessor(int state, I input) {
        return getIntTransition(state, input);
    }

    public int getIntTransition(int state, I input) {
        return getIntTransition(state, alphabet.getSymbolIndex(input));
    }

    public int getIntTransition(int state, int input) {
        int transId = state * alphabetSize + input;
        return transitions[transId];
    }

    public int getIntInitialState() {
        return initial;
    }
    @Override
    public StateIntAbstraction<I, Integer, SP, Void> stateIntAbstraction() {
        return this;
    }


    @Override
    public Integer getTransition(Integer state, I input) {
        int trans = getIntTransition(state.intValue(), input);
        return wrapState(trans);
    }

    @Override
    public Integer getTransition(int state, I input) {
        return wrapState(getSuccessor(state, input));
    }

    @Override
    public Integer getTransition(int state, int input) {
        return wrapState(getSuccessor(state, input));
    }

    @Override
    public void clear() {
        int endIdx = numStates * alphabetSize;
        numStates = 0;
        Arrays.fill(transitions, 0, endIdx, INVALID_STATE);
        initial = INVALID_STATE;
    }

    @Override
    public Integer addState(SP property) {
        return addIntState(property);
    }

    @Override
    public FullIntAbstraction<Integer, SP, Void> fullIntAbstraction(Alphabet<I> alphabet) {
        if (alphabet == this.alphabet) {
            return this;
        }
        return MutableDeterministic.super.fullIntAbstraction(alphabet);
    }

    public FullIntAbstraction<Integer, SP, Void> fullIntAbstraction() {
        return this;
    }

    @Override
    public void setStateProperty(Integer state, SP property) {
        setStateProperty(state.intValue(), property);
    }

    public abstract void setStateProperty(int stateId, SP property);

    @Override
    public void setTransitionProperty(Integer transition, Void property) {
    }

    @Override
    public void removeAllTransitions(Integer state) {
        removeAllTransitions(state.intValue());
    }

    public void removeAllTransitions(int state) {
        int base = state * alphabetSize;

        Arrays.fill(transitions, base, base + alphabetSize, INVALID_STATE);
    }

    @Override
    public Integer copyTransition(Integer trans, Integer succ) {
        return succ;
    }

    @Override
    public SP getStateProperty(Integer state) {
        return getStateProperty(state.intValue());
    }

    public abstract SP getStateProperty(int stateId);


    @Override
    public Void getTransitionProperty(Integer transition) {
        return null;
    }

    protected static int unwrapState(Integer state) {
        if (state == null) {
            return INVALID_STATE;
        }
        return state.intValue();
    }

    @Override
    public void setTransition(int state, int input, Integer transition) {
        setTransition(state, input, unwrapState(transition));
    }

    @Override
    public void setTransition(int state, int input, int successor, Void property) {
        setTransition(state, input, successor);
    }

    @Override
    public void setTransition(int state, I input, Integer transition) {
        setTransition(state, input, unwrapState(transition));
    }

    @Override
    public void setTransition(int state, I input, int successor, Void property) {
        setTransition(state, input, successor);
    }

    @Override
    public void setTransition(Integer state, I input, Integer transition, Void property) {
        setTransition(state, input, transition);
    }

    @Override
    public void setTransition(Integer state, I input, Integer transition) {
        int succId = (transition != null) ? transition.intValue() : INVALID_STATE;
        setTransition(state.intValue(), input, succId);
    }

    public void setTransition(int state, I input, int succ) {
        setTransition(state, alphabet.getSymbolIndex(input), succ);
    }

    public void setTransition(int state, int inputIdx, int succ) {
        transitions[state * alphabetSize + inputIdx] = succ;
    }

    @Override
    public int numInputs() {
        return alphabetSize;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        final int oldAlphabetSize = this.alphabetSize;
        final int newAlphabetSize = oldAlphabetSize + 1;
        final int newArraySize = this.transitions.length + this.stateCapacity;
        final int[] newTransitions = new int[newArraySize];

        Arrays.fill(newTransitions, 0, newArraySize, INVALID_STATE);

        for (int i = 0; i < this.numStates; i++) {
            System.arraycopy(transitions, i * oldAlphabetSize, newTransitions, i * newAlphabetSize, oldAlphabetSize);
        }

        this.transitions = newTransitions;
        this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        this.alphabetSize = newAlphabetSize;
    }

    @Override
    public DeterministicPowersetView<Integer, I, Integer> powersetView() {
        return new DeterministicPowersetView<>(this);
    }
}
