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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.GrowableAlphabetAutomaton;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.MutableDeterministic.FullIntAbstraction;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Abstract super class for compact automata representations. Compactness is achieved by representing states as
 * integers, such that each state can be conveniently used to address a memory location. As a result most of the
 * relevant automata data (such as successors, state- or transition properties) can be stored in compact arrays.
 * <p>
 * This class provides basic implementations (as far as possible on this abstract level) for several of the implemented
 * concepts and offers utility methods to subclasses, for updating their array-based automata data.
 *
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 *
 * @author frohme
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public abstract class AbstractCompact<I, T, SP, TP> implements MutableAutomaton<Integer, I, T, SP, TP>,
                                                               StateIDs<Integer>,
                                                               UniversalFiniteAlphabetAutomaton<Integer, I, T, SP, TP>,
                                                               GrowableAlphabetAutomaton<I>,
                                                               Serializable {

    protected static final float DEFAULT_RESIZE_FACTOR = 1.5f;
    protected static final int DEFAULT_INIT_CAPACITY = 11;
    protected static final int INVALID_STATE = FullIntAbstraction.INVALID_STATE;

    private Alphabet<I> alphabet;
    private final float resizeFactor;
    private int alphabetSize;
    private int stateCapacity;
    private int numStates;

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
        return toId(state);
    }

    @Override
    public Integer getState(int id) {
        return toState(id);
    }

    @Override
    public void clear() {
        numStates = 0;
    }

    @Override
    public final Integer addState(@Nullable SP property) {
        return addIntState(property);
    }

    public int addIntState(@Nullable SP property) {
        int newState = numStates++;
        ensureCapacity(numStates);
        setStateProperty(newState, property);
        return newState;
    }

    private void ensureCapacity(int newCapacity) {
        if (newCapacity <= stateCapacity) {
            return;
        }

        final int newCap = Math.max((int) (stateCapacity * resizeFactor), newCapacity);

        updateStorage(Payload.of(this.stateCapacity, newCap, numStates, alphabetSize, UpdateOperation.NEW_STATE));

        this.stateCapacity = newCap;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public final void addAlphabetSymbol(I symbol) {

        if (this.alphabet.containsSymbol(symbol)) {
            return;
        }

        updateStorage(Payload.of(this.alphabetSize,
                                 this.alphabetSize + 1,
                                 numStates,
                                 alphabetSize,
                                 UpdateOperation.NEW_ALPHABET_SYMBOL));

        this.alphabet = Alphabets.withNewSymbol(this.alphabet, symbol);
        this.alphabetSize++;
    }

    public abstract void setStateProperty(int state, @Nullable SP property);

    /**
     * Implementing classes need to implement this method in order to react to changes to the layout of their
     * array-based data, e.g. due to calls to {@link #addState()} or {@link #addAlphabetSymbol(Object)}.
     * <p>
     * Subclasses may use one of the {@link #updateStorage(Object[], IntFunction, Object, Payload)}... methods, to
     * conveniently delegate this task to this base class. This leaves subclasses only with the task to invoke the
     * provided update methods for each of their local array storages.
     *
     * @param payload
     *         the payload containing the necessary information for the update operation. This object must be passed
     *         as-is to the {@link #updateStorage(Object[], IntFunction, Object, Payload)}... methods.
     */
    protected abstract void updateStorage(Payload payload);

    /**
     * Return a copy of the provided array with updated memory layout.
     *
     * @param oldStorage
     *         the current array
     * @param defaultValue
     *         default value for newly allocated array positions
     * @param payload
     *         the payload object
     *
     * @return a copy of the provided array with updated memory layout.
     *
     * @see #updateStorage(Object[], Object, Payload)
     * @see #updateStorage(Object[], IntFunction, Object, Payload)
     */
    protected final int[] updateStorage(int[] oldStorage, int defaultValue, Payload payload) {
        return payload.type.updateStorage(oldStorage, payload, int[]::new, (arr, idx) -> arr[idx] = defaultValue);
    }

    /**
     * Return a copy of the provided array with updated memory layout.
     *
     * @param oldStorage
     *         the current array
     * @param defaultValue
     *         default value for newly allocated array positions
     * @param payload
     *         the payload object
     *
     * @return a copy of the provided array with updated memory layout.
     *
     * @see #updateStorage(int[], int, Payload)
     * @see #updateStorage(Object[], IntFunction, Object, Payload)
     */
    protected final Object[] updateStorage(Object[] oldStorage, @Nullable Object defaultValue, Payload payload) {
        return payload.type.updateStorage(oldStorage, payload, Object[]::new, (arr, idx) -> arr[idx] = defaultValue);
    }

    /**
     * Return a copy of the provided array with updated memory layout.
     *
     * @param oldStorage
     *         the current array
     * @param defaultValue
     *         default value for newly allocated array positions
     * @param payload
     *         the payload object
     *
     * @return a copy of the provided array with updated memory layout.
     *
     * @see #updateStorage(int[], int, Payload)
     * @see #updateStorage(Object[], Object, Payload)
     */
    protected final <T> T[] updateStorage(T[] oldStorage,
                                          IntFunction<T[]> arrayConstructor,
                                          @Nullable T defaultValue,
                                          Payload payload) {
        return payload.type.updateStorage(oldStorage, payload, arrayConstructor, (arr, idx) -> arr[idx] = defaultValue);
    }

    protected static Integer toState(int id) {
        return (id != INVALID_STATE) ? id : null;
    }

    protected static int toId(@Nullable Integer id) {
        return (id != null) ? id : INVALID_STATE;
    }

    /**
     * Returns for a given state id and input symbol index, the memory location for its associated data.
     *
     * @param stateId
     *         the state id
     * @param inputId
     *         the index of input symbol
     *
     * @return the memory location for the given state id and input symbol index
     *
     * @see #getSymbolIndex(Object)
     */
    protected final int toMemoryIndex(int stateId, int inputId) {
        return stateId * alphabetSize + inputId;
    }

    protected final int getSymbolIndex(@Nullable I input) {
        return alphabet.getSymbolIndex(input);
    }

    public final int numInputs() {
        return alphabetSize;
    }

    /**
     * An enum containing the different kind of update operations. Each enum constant implements the required {@link
     * UpdateOperation#updateStorage(Object, Payload, IntFunction, ArrayInitializer)} method.
     */
    private enum UpdateOperation {
        /**
         * A new state is added to the automaton. As a result, new space must be allocated at the end of the current
         * memory.
         */
        NEW_STATE {
            @Override
            <T> T updateStorage(T oldStorage,
                                Payload p,
                                IntFunction<T> arrayConstructor,
                                ArrayInitializer<T> initializer) {

                final T newStorage = arrayConstructor.apply(p.newSizeHint * p.alphabetSize);
                System.arraycopy(oldStorage, 0, newStorage, 0, p.oldSizeHint * p.alphabetSize);

                for (int i = p.oldSizeHint * p.alphabetSize; i < p.newSizeHint * p.alphabetSize; i++) {
                    initializer.setDefaultValue(newStorage, i);
                }
                return newStorage;
            }
        },
        /**
         * A new symbol is added to the automaton. As a result, new space must be allocated in between the state-local
         * blocks in the memory.
         */
        NEW_ALPHABET_SYMBOL {
            @Override
            <T> T updateStorage(T oldStorage,
                                Payload p,
                                IntFunction<T> arrayConstructor,
                                ArrayInitializer<T> initializer) {

                final T newStorage = arrayConstructor.apply(p.newSizeHint * p.numStates);

                for (int i = 0; i < p.numStates; i++) {
                    System.arraycopy(oldStorage, i * p.oldSizeHint, newStorage, i * p.newSizeHint, p.oldSizeHint);

                    for (int j = i * p.newSizeHint + p.oldSizeHint; j < (i + 1) * p.newSizeHint; j++) {
                        initializer.setDefaultValue(newStorage, j);
                    }
                }
                return newStorage;
            }
        };

        /**
         * Perform the update operation.
         *
         * @param oldStorage
         *         the existing storage information (in array form)
         * @param payload
         *         the payload containing the necessary information for performing the update operation
         * @param arrayConstructor
         *         the provider for instantiating the new array instance
         * @param initializer
         *         the function for initializing the newly allocated array positions
         * @param <T>
         *         the array type
         *
         * @return the new array
         */
        abstract <T> T updateStorage(T oldStorage,
                                     Payload payload,
                                     IntFunction<T> arrayConstructor,
                                     ArrayInitializer<T> initializer);
    }

    /**
     * A utility SAM interface.
     *
     * @param <T>
     *         the array type
     */
    private interface ArrayInitializer<T> {

        void setDefaultValue(T array, int idx);
    }

    /**
     * A utility class that encapsulates necessary information for performing an update of the stored automata data.
     */
    protected static final class Payload {

        private final int oldSizeHint;
        private final int newSizeHint;
        private final int alphabetSize;
        private final int numStates;
        private final UpdateOperation type;

        private Payload(int oldSizeHint, int newSizeHint, int numStates, int alphabetSize, UpdateOperation type) {
            this.oldSizeHint = oldSizeHint;
            this.newSizeHint = newSizeHint;
            this.alphabetSize = alphabetSize;
            this.numStates = numStates;
            this.type = type;
        }

        private static Payload of(int oldSizeHint,
                                  int newSizeHint,
                                  int numStates,
                                  int alphabetSize,
                                  UpdateOperation type) {
            return new Payload(oldSizeHint, newSizeHint, numStates, alphabetSize, type);
        }
    }

}
