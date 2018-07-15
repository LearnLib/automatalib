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

import java.util.Arrays;

import javax.annotation.Nullable;

import net.automatalib.words.Alphabet;

public abstract class AbstractCompactSimpleDeterministic<I, SP>
        extends AbstractCompactDeterministic<I, Integer, SP, Void> {

    protected int[] transitions;

    public AbstractCompactSimpleDeterministic(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public AbstractCompactSimpleDeterministic(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.transitions = new int[stateCapacity * alphabetSize];
        Arrays.fill(this.transitions, 0, this.transitions.length, AbstractCompact.INVALID_STATE);
    }

    public AbstractCompactSimpleDeterministic(Alphabet<I> alphabet, AbstractCompactSimpleDeterministic<?, ?> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
    }

    @Override
    public Integer createTransition(int successor, Void property) {
        return successor;
    }

    @Override
    public Integer createTransition(Integer successor, Void properties) {
        return successor;
    }

    @Override
    public int getIntSuccessor(Integer transition) {
        return transition;
    }

    @Override
    public Integer getTransition(int state, int input) {
        return makeId(transitions[state * alphabetSize + input]);
    }

    @Override
    protected void updateStorage(int oldSizeHint, int newSizeHint, UpdateType type) {
        this.transitions = updateStorage(this.transitions, oldSizeHint, newSizeHint, type);
    }

    @Override
    public void setTransitionProperty(Integer transition, @Nullable Void property) {}

    @Override
    public void removeAllTransitions(Integer state) {
        Arrays.fill(transitions, state, state + alphabetSize, AbstractCompact.INVALID_STATE);
    }

    @Override
    public void setTransition(int state, int input, Integer transition) {
        setTransition(state, input, getId(transition));
    }

    @Override
    public void setTransition(int state, int input, int successor, Void property) {
        setTransition(state, input, successor);
    }

    public void setTransition(int state, int inputIdx, int succ) {
        transitions[state * alphabetSize + inputIdx] = succ;
    }

    @Nullable
    @Override
    public Void getTransitionProperty(Integer transition) {
        return null;
    }

    @Override
    public void clear() {
        int endIdx = size() * alphabetSize;
        Arrays.fill(transitions, 0, endIdx, AbstractCompact.INVALID_STATE);
        super.clear();
    }
}