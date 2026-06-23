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
package net.automatalib.automaton.vpa.impl;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.vpa.SEVPA;

/**
 * Abstract class for k-SEVPAs that implements functionality shared across different subtypes.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input alphabet type
 */
public abstract class AbstractSEVPA<L, I> implements SEVPA<L, I>, StateIDs<L> {

    protected final VPAlphabet<I> alphabet;

    public AbstractSEVPA(VPAlphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public VPAlphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public StateIDs<L> stateIDs() {
        return this;
    }

    @Override
    public int encodeStackSym(L srcLoc, I callSym) {
        return encodeStackSym(srcLoc, alphabet.getCallSymbolIndex(callSym));
    }

    public int encodeStackSym(L srcLoc, int callSymIdx) {
        return alphabet.getNumCalls() * getStateId(srcLoc) + callSymIdx;
    }

    @Override
    public int getNumStackSymbols() {
        return size() * alphabet.getNumCalls();
    }

    public L getStackLoc(int stackSym) {
        return getState(stackSym / alphabet.getNumCalls());
    }

    public I getCallSym(int stackSym) {
        return alphabet.getCallSymbol(stackSym % alphabet.getNumCalls());
    }

}
