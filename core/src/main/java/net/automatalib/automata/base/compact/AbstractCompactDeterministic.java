/* Copyright (C) 2013-2017 TU Dortmund
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
import java.util.Collection;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public abstract class AbstractCompactDeterministic<I, T, SP, TP> implements MutableDeterministic<Integer, I, T, SP, TP>,
                                                                            StateIDs<Integer>,
                                                                            UniversalFiniteAlphabetAutomaton<Integer, I, T, SP, TP>,
                                                                            MutableDeterministic.StateIntAbstraction<I, T, SP, TP>,
                                                                            MutableDeterministic.FullIntAbstraction<T, SP, TP>,
                                                                            GrowableAlphabetAutomaton<I>,
                                                                            Serializable {

    public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
    public static final int DEFAULT_INIT_CAPACITY = 11;

    protected final GrowingAlphabet<I> alphabet;
    protected final float resizeFactor;
    protected int alphabetSize;
    protected Object[] transitions;
    protected int stateCapacity;
    protected int numStates;
    protected int initial = INVALID_STATE;

    public AbstractCompactDeterministic(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        this.alphabet = new SimpleAlphabet<>(alphabet);
        this.alphabetSize = alphabet.size();
        this.transitions = new Object[stateCapacity * alphabetSize];
        this.resizeFactor = resizeFactor;
        this.stateCapacity = stateCapacity;
    }

    public AbstractCompactDeterministic(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactDeterministic(Alphabet<I> alphabet, float resizeFactor) {
        this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
    }

    public void setTransition(int stateId, int inputIdx, int succId) {
        setTransition(stateId, inputIdx, succId, null);
    }

    public void setTransition(int state, int inputIdx, T trans) {
        transitions[state * alphabetSize + inputIdx] = trans;
    }

    public void setTransition(int stateId, int inputIdx, int succId, TP property) {
        setTransition(stateId, inputIdx, createTransition(succId, property));
    }

    @Override
    public void setTransition(Integer state, I input, T transition) {
        setTransition(getId(state), alphabet.getSymbolIndex(input), transition);
    }

    public void setTransition(int state, I input, T trans) {
        setTransition(state, alphabet.getSymbolIndex(input), trans);
    }

    public void setTransition(int stateId, I input, int succId, TP property) {
        setTransition(stateId, input, createTransition(succId, property));
    }

    protected static int getId(Integer id) {
        return (id != null) ? id.intValue() : INVALID_STATE;
    }

    @Override
    public void setInitialState(Integer state) {
        setInitialState(getId(state));
    }

    public void setInitialState(int stateId) {
        initial = stateId;
    }

    @Override
    public FullIntAbstraction<T, SP, TP> fullIntAbstraction(Alphabet<I> alphabet) {
        if (alphabet == this.alphabet) {
            return this;
        }
        return MutableDeterministic.super.fullIntAbstraction(alphabet);
    }

    public FullIntAbstraction<T, SP, TP> fullIntAbstraction() {
        return this;
    }

    @Override
    public StateIntAbstraction<I, T, SP, TP> stateIntAbstraction() {
        return this;
    }

    @Override
    public final Integer getSuccessor(T transition) {
        return makeId(getIntSuccessor(transition));
    }

    protected static Integer makeId(int id) {
        return (id != INVALID_STATE) ? Integer.valueOf(id) : null;
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
        return Integer.valueOf(id);
    }

    @Override
    public int getIntInitialState() {
        return initial;
    }

    @Override
    public Integer getInitialState() {
        return makeId(initial);
    }

    public int addIntState() {
        return addIntState(null);
    }

    public int addIntState(SP property) {
        int newState = createState();
        setStateProperty(newState, property);
        return newState;
    }

    public int addIntInitialState() {
        return addIntInitialState(null);
    }

    public int addIntInitialState(SP property) {
        int newState = addIntState(property);
        setInitialState(newState);
        return newState;
    }

    protected final int createState() {
        int newState = numStates++;
        ensureCapacity(numStates);
        return newState;
    }

    @Override
    public void setStateProperty(Integer state, SP property) {
        setStateProperty(state.intValue(), property);
    }

    public abstract void setStateProperty(int state, SP property);

    public final void ensureCapacity(int newCapacity) {
        if (newCapacity <= stateCapacity) {
            return;
        }

        int newCap = (int) (stateCapacity * resizeFactor);
        if (newCap < newCapacity) {
            newCap = newCapacity;
        }

        Object[] newTrans = new Object[newCap * alphabetSize];
        System.arraycopy(transitions, 0, newTrans, 0, stateCapacity * alphabetSize);
        this.transitions = newTrans;
        this.stateCapacity = newCap;
        ensureCapacity();
    }

    protected void ensureCapacity() {
    }

    public T getTransition(int stateId, I input) {
        return getTransition(stateId, alphabet.getSymbolIndex(input));
    }

    @SuppressWarnings("unchecked")
    public T getTransition(int stateId, int inputIdx) {
        return (T) transitions[stateId * alphabetSize + inputIdx];
    }

    @Override
    public T getTransition(Integer state, I input) {
        return getTransition(getId(state), alphabet.getSymbolIndex(input));
    }

    @Override
    public SP getStateProperty(Integer state) {
        return getStateProperty(getId(state));
    }

    public abstract SP getStateProperty(int stateId);

    @Override
    public void clear() {
        int endIdx = numStates * alphabetSize;
        numStates = 0;
        for (int i = 0; i < endIdx; i++) {
            transitions[i] = null;
        }
        this.initial = INVALID_STATE;
    }

    @Override
    public Integer addState(SP property) {
        return addIntState(property);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        int base = state.intValue() * alphabetSize;
        for (int i = 0; i < alphabetSize; i++) {
            transitions[base++] = null;
        }
    }

    @Override
    public T createTransition(Integer succ, TP property) {
        return createTransition(getId(succ), property);
    }

    public abstract T createTransition(int succId, TP property);

    @Override
    public T copyTransition(T trans, Integer succ) {
        return copyTransition(trans, getId(succ));
    }

    public abstract T copyTransition(T trans, int succId);

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
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
        final Object[] newTransitions = new Object[newArraySize];

        for (int i = 0; i < this.numStates; i++) {
            System.arraycopy(transitions, i * oldAlphabetSize, newTransitions, i * newAlphabetSize, oldAlphabetSize);
        }

        this.transitions = newTransitions;
        this.alphabet.addSymbol(symbol);
        this.alphabetSize = newAlphabetSize;
    }

}
