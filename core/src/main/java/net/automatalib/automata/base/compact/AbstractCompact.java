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
import java.util.Collection;
import java.util.function.IntFunction;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.MutableDeterministic.FullIntAbstraction;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Abstract super class for compact (i.e. array-based) automata representations.
 *
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public abstract class AbstractCompact<I, T, SP, TP> implements MutableAutomaton<Integer, I, T, SP, TP>,
                                                               StateIDs<Integer>,
                                                               UniversalFiniteAlphabetAutomaton<Integer, I, T, SP, TP>,
                                                               GrowableAlphabetAutomaton<I>,
                                                               Serializable {

    protected static final float DEFAULT_RESIZE_FACTOR = 1.5f;
    protected static final int DEFAULT_INIT_CAPACITY = 11;
    protected static final int INVALID_STATE = FullIntAbstraction.INVALID_STATE;

    protected Alphabet<I> alphabet;
    private final float resizeFactor;
    protected int alphabetSize;
    private int stateCapacity;
    private int numStates;

    public AbstractCompact(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompact(Alphabet<I> alphabet, AbstractCompact<?, ?, ?, ?> other) {
        this(alphabet, other.stateCapacity, other.resizeFactor);
        this.numStates = other.numStates;
    }

    public AbstractCompact(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
        this.resizeFactor = resizeFactor;
        this.stateCapacity = stateCapacity;
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
        return getId(state);
    }

    @Override
    public Integer getState(int id) {
        return makeId(id);
    }

    @Override
    public void clear() {
        numStates = 0;
    }

    @Override
    public final Integer addState(SP property) {
        int newState = numStates++;
        ensureCapacity(numStates);
        setStateProperty(newState, property);
        return newState;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    private void ensureCapacity(int newCapacity) {
        if (newCapacity <= stateCapacity) {
            return;
        }

        final int newCap = Math.max((int) (stateCapacity * resizeFactor), newCapacity);

        updateStorage(new UpdatePayload(this.stateCapacity, newCap, UpdateType.NEW_STATE));

        this.stateCapacity = newCap;
    }

    @Override
    public final void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        updateStorage(new UpdatePayload(this.alphabetSize, this.alphabetSize + 1, UpdateType.NEW_ALPHABET_SYMBOL));

        this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        this.alphabetSize++;
    }

    protected static int getId(Integer id) {
        return (id != null) ? id.intValue() : INVALID_STATE;
    }

    protected static Integer makeId(int id) {
        return (id != INVALID_STATE) ? Integer.valueOf(id) : null;
    }

    public abstract void setStateProperty(int state, SP property);

    protected abstract void updateStorage(UpdatePayload payload);

    protected final int[] updateStorage(int[] oldStorage, int defaultValue, UpdatePayload payload) {
        return updateStorage(oldStorage, payload, int[]::new, (arr, idx) -> arr[idx] = defaultValue);
    }

    protected final Object[] updateStorage(Object[] oldStorage, Object defaultValue, UpdatePayload payload) {
        return updateStorage(oldStorage, payload, Object[]::new, (arr, idx) -> arr[idx] = defaultValue);
    }

    protected final <T> T[] updateStorage(T[] oldStorage,
                                          IntFunction<T[]> arrayConstructor,
                                          T defaultValue,
                                          UpdatePayload payload) {
        return updateStorage(oldStorage, payload, arrayConstructor, (arr, idx) -> arr[idx] = defaultValue);
    }

    private <T> T updateStorage(T oldStorage,
                                UpdatePayload payload,
                                IntFunction<T> arrayConstructor,
                                ArrayInitializer<T> initializer) {
        final int oldSizeHint = payload.oldSizeHint;
        final int newSizeHint = payload.newSizeHint;

        switch (payload.type) {
            case NEW_STATE: {
                final T newStorage = arrayConstructor.apply(newSizeHint * alphabetSize);
                System.arraycopy(oldStorage, 0, newStorage, 0, oldSizeHint * alphabetSize);

                for (int i = oldSizeHint * alphabetSize; i < newSizeHint * alphabetSize; i++) {
                    initializer.setDefaultValue(newStorage, i);
                }
                return newStorage;
            }
            case NEW_ALPHABET_SYMBOL: {
                final T newStorage = arrayConstructor.apply(newSizeHint * numStates);
                for (int i = 0; i < numStates; i++) {
                    System.arraycopy(oldStorage, i * oldSizeHint, newStorage, i * newSizeHint, oldSizeHint);

                    for (int j = i * newSizeHint + oldSizeHint; j < (i + 1) * newSizeHint; j++) {
                        initializer.setDefaultValue(newStorage, j);
                    }
                }
                return newStorage;
            }
            default:
                throw new IllegalArgumentException("Unknown update type: " + payload.type);
        }
    }

    private enum UpdateType {
        NEW_STATE,
        NEW_ALPHABET_SYMBOL
    }

    protected static class UpdatePayload {

        private final int oldSizeHint;
        private final int newSizeHint;
        private final UpdateType type;

        UpdatePayload(int oldSizeHint, int newSizeHint, UpdateType type) {
            this.oldSizeHint = oldSizeHint;
            this.newSizeHint = newSizeHint;
            this.type = type;
        }
    }

    private interface ArrayInitializer<T> {

        void setDefaultValue(T array, int idx);
    }

}
