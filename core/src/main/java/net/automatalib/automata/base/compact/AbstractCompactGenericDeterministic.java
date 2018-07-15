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

import net.automatalib.words.Alphabet;

public abstract class AbstractCompactGenericDeterministic<I, T, SP, TP>
        extends AbstractCompactDeterministic<I, T, SP, TP> {

    protected Object[] transitions;

    public AbstractCompactGenericDeterministic(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactGenericDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.transitions = new Object[stateCapacity * alphabetSize];
    }

    public AbstractCompactGenericDeterministic(Alphabet<I> alphabet, AbstractCompactGenericDeterministic<?, ?, ?, ?> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
    }

    @Override
    public void setTransition(int state, int inputIdx, T trans) {
        transitions[state * alphabetSize + inputIdx] = trans;
    }

    @Override
    public void setTransition(int stateId, int inputIdx, int succId, TP property) {
        setTransition(stateId, inputIdx, createTransition(succId, property));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getTransition(int stateId, int inputIdx) {
        return (T) transitions[stateId * alphabetSize + inputIdx];
    }

    @Override
    public void clear() {
        int endIdx = size() * alphabetSize;
        for (int i = 0; i < endIdx; i++) {
            transitions[i] = null;
        }
        super.clear();
    }

    @Override
    protected void updateStorage(int oldSizeHint, int newSizeHint, UpdateType type) {
        this.transitions = updateStorage(this.transitions, oldSizeHint, newSizeHint, type);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        int base = state.intValue() * alphabetSize;
        for (int i = 0; i < alphabetSize; i++) {
            transitions[base++] = null;
        }
    }

}
