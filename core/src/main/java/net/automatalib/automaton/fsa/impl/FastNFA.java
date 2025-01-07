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

import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.base.AbstractFastMutableNondet;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.common.util.WrapperUtil;
import net.automatalib.ts.AcceptorPowersetViewTS;
import net.automatalib.ts.powerset.impl.FastAcceptorPowersetDTS;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FastNFA<I> extends AbstractFastMutableNondet<FastNFAState, I, FastNFAState, Boolean, Void>
        implements MutableNFA<FastNFAState, I> {

    public FastNFA(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public boolean isAccepting(FastNFAState state) {
        return state.isAccepting();
    }

    @Override
    public void setAccepting(FastNFAState state, boolean accepting) {
        state.setAccepting(accepting);
    }

    @Override
    public FastNFAState addState(boolean accepting) {
        return addState(Boolean.valueOf(accepting));
    }

    @Override
    protected FastNFAState createState(@Nullable Boolean property) {
        return new FastNFAState(inputAlphabet.size(), WrapperUtil.booleanValue(property));
    }

    @Override
    public AcceptorPowersetViewTS<Set<FastNFAState>, I, FastNFAState> powersetView() {
        return new FastAcceptorPowersetDTS<>(this);
    }

}
