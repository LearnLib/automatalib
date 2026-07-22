/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton.impl;

import java.util.Arrays;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.base.AbstractCompact;
import net.automatalib.automaton.base.AbstractCompactDeterministic;
import net.automatalib.automaton.concept.MutableTransitionOutput;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A compact implementation that supports generic transition outputs.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class CompactTransitionOutput<I, O> extends AbstractCompactDeterministic<I, CompactTransition<O>, Void, O>
        implements MutableTransitionOutput<CompactTransition<O>, O> {

    private int[] transitions;
    private @Nullable Object[] outputs;

    public CompactTransitionOutput(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);

        final int size = stateCapacity * numInputs();

        this.transitions = new int[size];
        this.outputs = new Object[size];

        Arrays.fill(transitions, AbstractCompact.INVALID_STATE);
    }

    public CompactTransitionOutput(Alphabet<I> alphabet, int stateCapacity) {
        this(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactTransitionOutput(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public CompactTransitionOutput(CompactTransitionOutput<I, O> other) {
        this(other.getInputAlphabet(), other);
    }

    protected CompactTransitionOutput(Alphabet<I> alphabet, CompactTransitionOutput<?, O> other) {
        super(alphabet, other);
        this.transitions = other.transitions.clone();
        this.outputs = other.outputs.clone();
    }

    @Override
    protected void updateTransitionStorage(Payload payload) {
        this.transitions = updateTransitionStorage(this.transitions, AbstractCompact.INVALID_STATE, payload);
        this.outputs = updateTransitionStorage(this.outputs, null, payload);
    }

    @Override
    public O getTransitionOutput(CompactTransition<O> transition) {
        return transition.getProperty();
    }

    @Override
    public O getTransitionProperty(CompactTransition<O> transition) {
        return transition.getProperty();
    }

    @Override
    @SuppressWarnings("unchecked")
    // Overridden for performance reasons (to prevent object instantiation of default implementation)
    public O getTransitionProperty(int state, int input) {
        return (O) outputs[toMemoryIndex(state, input)];
    }

    @Override
    public void setTransitionProperty(CompactTransition<O> transition, O property) {
        transition.setProperty(property);

        if (transition.isAutomatonTransition()) {
            outputs[transition.getMemoryIdx()] = property;
        }
    }

    @Override
    public void setTransitionOutput(CompactTransition<O> transition, O output) {
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
    // Overridden for performance reasons (to prevent object instantiation of default implementation)
    public int getSuccessor(int state, int input) {
        return transitions[toMemoryIndex(state, input)];
    }

    @Override
    public int getIntSuccessor(CompactTransition<O> transition) {
        return transition.getSuccId();
    }

    @Override
    public CompactTransition<O> createTransition(int succId, O property) {
        return new CompactTransition<>(succId, property);
    }

    @Override
    public void setStateProperty(int state, Void property) {}

    @Override
    public Void getStateProperty(int stateId) {
        return null;
    }

    @Override
    public void setTransition(int state, int input, @Nullable CompactTransition<O> transition) {
        if (transition == null) {
            setTransition(state, input, AbstractCompact.INVALID_STATE, null);
        } else {
            setTransition(state, input, transition.getSuccId(), transition.getProperty());
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
    public @Nullable CompactTransition<O> getTransition(int state, int input) {
        final int idx = toMemoryIndex(state, input);
        final int succ = transitions[idx];

        if (succ == AbstractCompact.INVALID_STATE) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final O output = (O) outputs[idx];

        return new CompactTransition<>(idx, succ, output);
    }

    public static final class Creator<I, O> implements AutomatonCreator<CompactTransitionOutput<I, O>, I> {

        @Override
        public CompactTransitionOutput<I, O> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return new CompactTransitionOutput<>(alphabet, sizeHint);
        }

        @Override
        public CompactTransitionOutput<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactTransitionOutput<>(alphabet);
        }
    }

}
