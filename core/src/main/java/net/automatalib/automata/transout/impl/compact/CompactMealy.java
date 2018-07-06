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
package net.automatalib.automata.transout.impl.compact;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.base.compact.AbstractCompactGenericDeterministic;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.words.Alphabet;

public class CompactMealy<I, O> extends AbstractCompactDeterministic<I, CompactMealyTransition<O>, Void, O>
        implements MutableMealyMachine<Integer, I, CompactMealyTransition<O>, O> {

    private int[] transitions;
    private Object[] outputs;

    public CompactMealy(Alphabet<I> alphabet, float resizeFactor) {
        this(alphabet, DEFAULT_INIT_CAPACITY, resizeFactor);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        transitions = new int[stateCapacity * alphabetSize];
        Arrays.fill(transitions, 0, transitions.length, AbstractCompact.INVALID_STATE);
        outputs = new Object[stateCapacity * alphabetSize];
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactMealy(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @Override
    protected void increaseStateCapacity(int oldCapacity, int newCapacity) {
        final int[] newTransitions = new int[newCapacity * alphabetSize];
        final Object[] newOutputs = new Object[newCapacity * alphabetSize];
        System.arraycopy(transitions, 0, newTransitions, 0, oldCapacity * alphabetSize);
        System.arraycopy(outputs, 0, newOutputs, 0, oldCapacity * alphabetSize);
        Arrays.fill(newTransitions, oldCapacity * alphabetSize, newCapacity * alphabetSize, AbstractCompact.INVALID_STATE);
        this.transitions = newTransitions;
        this.outputs = newOutputs;
    }

    @Override
    protected void increaseAlphabetCapacity(int oldAlphabetSize, int newAlphabetSize, int newCapacity) {
        final int[] newTransitions = new int[newCapacity];
        final Object[] newOutputs = new Object[newCapacity];

        for (int i = 0; i < this.size(); i++) {
            System.arraycopy(transitions, i * oldAlphabetSize, newTransitions, i * newAlphabetSize, oldAlphabetSize);
            System.arraycopy(outputs, i * oldAlphabetSize, newOutputs, i * newAlphabetSize, oldAlphabetSize);
            Arrays.fill(newTransitions, i * newAlphabetSize + oldAlphabetSize, (i + 1) * newAlphabetSize, AbstractCompact.INVALID_STATE);
        }

        transitions = newTransitions;
        outputs = newOutputs;
    }

    @Override
    public O getTransitionOutput(CompactMealyTransition<O> transition) {
        return transition.getOutput();
    }

    @Override
    public O getTransitionProperty(CompactMealyTransition<O> transition) {
        return transition.getOutput();
    }

    @Override
    public void setTransitionProperty(CompactMealyTransition<O> transition, O property) {
        transition.setOutput(property);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        Arrays.fill(transitions, state, state + alphabetSize, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, state, state + alphabetSize, null);
    }

    @Override
    public void setTransitionOutput(CompactMealyTransition<O> transition, O output) {
        transition.setOutput(output);
    }

    @Override
    public int getIntSuccessor(CompactMealyTransition<O> transition) {
        return transition.getSuccId();
    }

    @Override
    public CompactMealyTransition<O> createTransition(int succId, O property) {
        return new CompactMealyTransition<>(succId, property);
    }

    @Override
    public void setStateProperty(int state, Void property) {
    }

    @Override
    public Void getStateProperty(int stateId) {
        return null;
    }

    @Override
    public void setTransition(int state, int input, CompactMealyTransition<O> transition) {
        setTransition(state, input, transition.getSuccId(), transition.getOutput());
    }

    @Override
    public void setTransition(int state, int input, int successor, @Nullable O property) {
        transitions[state * alphabetSize + input] = successor;
        outputs[state * alphabetSize + input] = property;
    }

    @Override
    public void clear() {
        int endIdx = size() * alphabetSize;
        Arrays.fill(transitions, 0, endIdx, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, 0, endIdx, null);

        super.clear();
    }

    @Override
    public CompactMealyTransition<O> getTransition(int state, int input) {
        final Integer succ = makeId(transitions[state * alphabetSize + input]);

        if (succ == null) {
            return null;
        }

        return new CompactMealyTransition<>(succ, (O)outputs[state * alphabetSize + input]);
    }

    public static final class Creator<I, O> implements AutomatonCreator<CompactMealy<I, O>, I> {

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactMealy<>(alphabet, sizeHint);
        }

        @Override
        public CompactMealy<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMealy<>(alphabet);
        }
    }

}
