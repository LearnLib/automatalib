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
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.words.Alphabet;

@ParametersAreNonnullByDefault
public class CompactMealy<I, O> extends AbstractCompactDeterministic<I, CompactMealyTransition<O>, Void, O>
        implements MutableMealyMachine<Integer, I, CompactMealyTransition<O>, O> {

    private int[] transitions;
    private Object[] outputs;

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        final int size = stateCapacity * numInputs();

        this.transitions = new int[size];
        this.outputs = new Object[size];

        Arrays.fill(transitions, 0, size, AbstractCompact.INVALID_STATE);
    }

    public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactMealy(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    @Override
    protected void updateStorage(Payload payload) {
        this.transitions = updateStorage(this.transitions, AbstractCompact.INVALID_STATE, payload);
        this.outputs = updateStorage(this.outputs, null, payload);
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

        if (transition.isAutomatonTransition()) {
            outputs[transition.getMemoryIdx()] = property;
        }
    }

    @Override
    public void setTransitionOutput(CompactMealyTransition<O> transition, O output) {
        setTransitionProperty(transition, output);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        final int lower = state * numInputs();
        final int upper = lower + numInputs();
        Arrays.fill(transitions, lower, upper, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, lower, upper, null);
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
    public void setStateProperty(int state, Void property) {}

    @Override
    public Void getStateProperty(int stateId) {
        return null;
    }

    @Override
    public void setTransition(int state, int input, CompactMealyTransition<O> transition) {
        if (transition == null) {
            setTransition(state, input, AbstractCompact.INVALID_STATE, null);
        } else {
            setTransition(state, input, transition.getSuccId(), transition.getOutput());
            transition.setMemoryIdx(toMemoryIndex(state, input));
        }
    }

    @Override
    public void setTransition(int state, int input, int successor, @Nullable O property) {
        final int idx = toMemoryIndex(state, input);
        transitions[idx] = successor;
        outputs[idx] = property;
    }

    @Override
    public void clear() {
        int endIdx = size() * numInputs();
        Arrays.fill(transitions, 0, endIdx, AbstractCompact.INVALID_STATE);
        Arrays.fill(outputs, 0, endIdx, null);

        super.clear();
    }

    @Override
    public CompactMealyTransition<O> getTransition(int state, int input) {
        final int idx = toMemoryIndex(state, input);
        final int succ = transitions[idx];

        if (succ == AbstractCompact.INVALID_STATE) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final O output = (O) outputs[idx];

        return new CompactMealyTransition<>(idx, succ, output);
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
