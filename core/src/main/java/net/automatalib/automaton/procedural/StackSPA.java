/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.automaton.procedural;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import net.automatalib.api.alphabet.ProceduralInputAlphabet;
import net.automatalib.api.automaton.fsa.DFA;
import net.automatalib.api.automaton.procedural.SPA;
import net.automatalib.api.ts.simple.SimpleDTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A stack-based implementation for the (instrumented) language of an {@link SPA}.
 *
 * @param <S>
 *         procedural state type
 * @param <I>
 *         input symbol type
 */
public class StackSPA<S, I> implements SPA<StackState<S, I, DFA<S, I>>, I>, SimpleDTS<StackState<S, I, DFA<S, I>>, I> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final @Nullable I initialCall;
    private final Map<I, DFA<S, I>> procedures;

    // cast is fine, because we make sure to only query states belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public StackSPA(ProceduralInputAlphabet<I> alphabet,
                    @Nullable I initialCall,
                    Map<I, ? extends DFA<? extends S, I>> procedures) {
        this.alphabet = alphabet;
        this.initialCall = initialCall;
        this.procedures = (Map<I, DFA<S, I>>) procedures;
    }

    @Override
    public StackState<S, I, DFA<S, I>> getTransition(StackState<S, I, DFA<S, I>> state, I input) {
        if (state.isSink() || state.isTerm()) {
            return StackState.sink();
        } else if (alphabet.isInternalSymbol(input)) {
            if (state.isInit()) {
                return StackState.sink();
            }

            final DFA<S, I> model = state.getProcedure();
            final S next = model.getTransition(state.getCurrentState(), input);

            // undefined internal transition
            if (next == null) {
                return StackState.sink();
            }

            return state.updateState(next);
        } else if (alphabet.isCallSymbol(input)) {
            if (state.isInit() && !Objects.equals(this.initialCall, input)) {
                return StackState.sink();
            }

            final DFA<S, I> model = this.procedures.get(input);

            if (model == null) {
                return StackState.sink();
            }

            final S next = model.getInitialState();

            if (next == null) {
                return StackState.sink();
            }

            // store the procedural successor in the stack so that we don't need to look it up on return symbols
            final StackState<S, I, DFA<S, I>> returnState;
            if (state.isInit()) {
                returnState = StackState.term();
            } else {
                final S succ = state.getProcedure().getSuccessor(state.getCurrentState(), input);
                if (succ == null) {
                    return StackState.sink();
                }
                returnState = state.updateState(succ);
            }

            return returnState.push(model, next);
        } else if (alphabet.isReturnSymbol(input)) {
            if (state.isInit()) {
                return StackState.sink();
            }

            // if we returned the state before, we checked that a procedure is available
            final DFA<S, I> model = state.getProcedure();

            // cannot return, reject word
            if (!model.isAccepting(state.getCurrentState())) {
                return StackState.sink();
            }

            return state.pop();
        } else {
            return StackState.sink();
        }
    }

    @Override
    public boolean isAccepting(StackState<S, I, DFA<S, I>> state) {
        return state.isTerm();
    }

    @Override
    public StackState<S, I, DFA<S, I>> getInitialState() {
        return StackState.init();
    }

    @Override
    public @Nullable I getInitialProcedure() {
        return initialCall;
    }

    @Override
    public ProceduralInputAlphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public Map<I, DFA<?, I>> getProcedures() {
        return Collections.unmodifiableMap(procedures);
    }
}
