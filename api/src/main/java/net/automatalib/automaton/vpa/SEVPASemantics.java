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
package net.automatalib.automaton.vpa;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SEVPASemantics<S, I> implements DeterministicAcceptorTS<State<S>, I> {

    private final SEVPA<S, I> sevpa;
    private final VPAlphabet<I> alphabet;

    public SEVPASemantics(SEVPA<S, I> sevpa) {
        this.sevpa = sevpa;
        this.alphabet = sevpa.getInputAlphabet();
    }

    @Override
    public @Nullable State<S> getTransition(State<S> state, I input) {
        final S loc = state.getLocation();
        return switch (alphabet.getSymbolType(input)) {
            case CALL:
                final int newStackElem = sevpa.encodeStackSym(loc, input);
                yield new State<>(sevpa.getModuleEntry(input),
                                  StackContents.push(newStackElem, state.getStackContents()));
            case RETURN: {
                final StackContents contents = state.getStackContents();
                if (contents == null) {
                    yield null;
                }
                final int stackElem = contents.peek();
                final S succ = sevpa.getReturnSuccessor(loc, input, stackElem);
                if (succ == null) {
                    yield null;
                }
                yield new State<>(succ, contents.pop());
            }
            case INTERNAL: {
                final S succ = sevpa.getInternalSuccessor(loc, input);
                if (succ == null) {
                    yield null;
                }
                yield new State<>(succ, state.getStackContents());
            }
        };
    }

    @Override
    public boolean isAccepting(State<S> state) {
        return sevpa.getStateProperty(state.getLocation()) && state.getStackContents() == null;
    }

    @Override
    public State<S> getInitialState() {
        return new State<>(sevpa.getInitialState(), null);
    }
}
