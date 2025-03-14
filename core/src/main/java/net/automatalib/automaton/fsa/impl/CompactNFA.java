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
package net.automatalib.automaton.fsa.impl;

import java.util.BitSet;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.base.AbstractCompactSimpleNondet;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.common.util.WrapperUtil;
import net.automatalib.ts.AcceptorPowersetViewTS;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CompactNFA<I> extends AbstractCompactSimpleNondet<I, Boolean> implements MutableNFA<Integer, I> {

    private final BitSet accepting;

    public CompactNFA(Alphabet<I> alphabet, int stateCapacity) {
        super(alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
        this.accepting = new BitSet();
    }

    public CompactNFA(Alphabet<I> alphabet) {
        this(alphabet, DEFAULT_INIT_CAPACITY);
    }

    public CompactNFA(CompactNFA<I> other) {
        this(other.getInputAlphabet(), other);
    }

    protected CompactNFA(Alphabet<I> alphabet, CompactNFA<?> other) {
        super(alphabet, other);
        this.accepting = (BitSet) other.accepting.clone();
    }

    public <I2> CompactNFA<I2> translate(Alphabet<I2> newAlphabet) {
        if (numInputs() != newAlphabet.size()) {
            throw new IllegalArgumentException(
                    "Can only translate automata with matching alphabet sizes, found: " + newAlphabet.size() +
                    " (new) vs. " + numInputs() + " (old)");
        }
        return new CompactNFA<>(newAlphabet, this);
    }

    @Override
    public boolean isAccepting(Integer state) {
        return isAccepting(state.intValue());
    }

    public boolean isAccepting(int stateId) {
        return accepting.get(stateId);
    }

    @Override
    public void flipAcceptance() {
        this.accepting.flip(0, size());
    }

    @Override
    public void setAccepting(Integer state, boolean accepting) {
        setAccepting(state.intValue(), accepting);
    }

    public void setAccepting(int stateId, boolean accepting) {
        if (accepting) {
            this.accepting.set(stateId);
        } else {
            this.accepting.clear(stateId);
        }
    }

    @Override
    public Integer addState(boolean accepting) {
        return addState(Boolean.valueOf(accepting));
    }

    @Override
    public void clear() {
        accepting.clear();
        super.clear();
    }

    @Override
    public void setStateProperty(int stateId, @Nullable Boolean property) {
        setAccepting(stateId, WrapperUtil.booleanValue(property));
    }

    @Override
    public Boolean getStateProperty(int state) {
        return isAccepting(state);
    }

    @Override
    public AcceptorPowersetViewTS<BitSet, I, Integer> powersetView() {
        return new CompactAcceptorPowersetDTS();
    }

    protected class CompactAcceptorPowersetDTS extends CompactPowersetDTS
            implements AcceptorPowersetViewTS<BitSet, I, Integer> {

        @Override
        public boolean isAccepting(BitSet state) {
            for (int i = state.nextSetBit(0); i >= 0; i = state.nextSetBit(i + 1)) {
                if (CompactNFA.this.isAccepting(i)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static final class Creator<I> implements AutomatonCreator<CompactNFA<I>, I> {

        @Override
        public CompactNFA<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
            return new CompactNFA<>(alphabet, numStates);
        }

        @Override
        public CompactNFA<I> createAutomaton(Alphabet<I> alphabet) {
            return new CompactNFA<>(alphabet);
        }
    }

}
