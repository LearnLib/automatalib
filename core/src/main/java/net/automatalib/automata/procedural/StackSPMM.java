/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.procedural;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.MealyTransition;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;

/**
 * A stack-based implementation for the (instrumented) semantics of a System of Procedural Automata.
 *
 * @param <S>
 *         hypotheses state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class StackSPMM<S, I, T, O>
        implements SPMM<StackState<S, I, MealyMachine<S, I, T, O>>, I, MealyTransition<StackState<S, I, MealyMachine<S, I, T, O>>, O>, O> {

    private final ProceduralInputAlphabet<I> inputAlphabet;
    private final ProceduralOutputAlphabet<O> outputAlphabet;

    private final I initialCall;
    private final O initialOutput;
    private final Map<I, MealyMachine<S, I, T, O>> procedures;

    @SuppressWarnings("unchecked")
    public StackSPMM(ProceduralInputAlphabet<I> inputAlphabet,
                     ProceduralOutputAlphabet<O> outputAlphabet,
                     I initialCall,
                     O initialOutput,
                     Map<I, ? extends MealyMachine<? extends S, I, ? extends T, O>> procedures) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.initialCall = initialCall;
        this.initialOutput = initialOutput;
        this.procedures = (Map<I, MealyMachine<S, I, T, O>>) procedures;
    }

    @Override
    public MealyTransition<StackState<S, I, MealyMachine<S, I, T, O>>, O> getTransition(StackState<S, I, MealyMachine<S, I, T, O>> state,
                                                                                        I input) {
        if (state.isSink() || state.isTerm()) {
            return sink();
        } else if (inputAlphabet.isInternalSymbol(input)) {
            if (state.isInit()) {
                return sink();
            }

            final MealyMachine<S, I, T, O> model = state.getProcedure();
            final T t = model.getTransition(state.getCurrentState(), input);

            // undefined internal transition
            if (t == null || outputAlphabet.isErrorSymbol(model.getTransitionOutput(t))) {
                return sink();
            }

            return new MealyTransition<>(state.updateState(model.getSuccessor(t)), model.getTransitionOutput(t));
        } else if (inputAlphabet.isCallSymbol(input)) {
            if (state.isInit() && !Objects.equals(this.initialCall, input)) {
                return sink();
            }

            final MealyMachine<S, I, T, O> model = this.procedures.get(input);

            if (model == null) {
                return sink();
            }

            final S next = model.getInitialState();

            if (next == null) {
                return sink();
            }

            // store the procedural successor in the stack so that we don't need to look it up on return symbols
            final StackState<S, I, MealyMachine<S, I, T, O>> returnState;
            final O output;
            if (state.isInit()) {
                returnState = StackState.term();
                output = initialOutput;
            } else {
                final MealyMachine<S, I, T, O> p = state.getProcedure();
                final T t = p.getTransition(state.getCurrentState(), input);

                if (t == null || outputAlphabet.isErrorSymbol(p.getTransitionOutput(t))) {
                    return sink();
                }
                returnState = state.updateState(p.getSuccessor(t));
                output = p.getTransitionOutput(t);
            }

            return new MealyTransition<>(returnState.push(model, next), output);
        } else if (inputAlphabet.isReturnSymbol(input)) {
            if (state.isInit()) {
                return sink();
            }

            // if we returned the state before, we checked that a procedure is available
            final MealyMachine<S, I, T, O> model = state.getProcedure();
            final T t = model.getTransition(state.getCurrentState(), input);

            if (t == null || outputAlphabet.isErrorSymbol(model.getTransitionOutput(t))) {
                return sink();
            }

            return new MealyTransition<>(state.pop(), model.getTransitionOutput(t));
        } else {
            return sink();
        }
    }

    @Override
    public StackState<S, I, MealyMachine<S, I, T, O>> getInitialState() {
        return StackState.init();
    }

    @Override
    public I getInitialProcedure() {
        return initialCall;
    }

    @Override
    public ProceduralInputAlphabet<I> getInputAlphabet() {
        return this.inputAlphabet;
    }

    @Override
    public ProceduralOutputAlphabet<O> getOutputAlphabet() {
        return this.outputAlphabet;
    }

    @Override
    public Map<I, MealyMachine<?, I, ?, O>> getProcedures() {
        return Collections.unmodifiableMap(procedures);
    }

    @Override
    public O getTransitionOutput(MealyTransition<StackState<S, I, MealyMachine<S, I, T, O>>, O> transition) {
        return transition.getOutput();
    }

    @Override
    public StackState<S, I, MealyMachine<S, I, T, O>> getSuccessor(MealyTransition<StackState<S, I, MealyMachine<S, I, T, O>>, O> transition) {
        return transition.getSuccessor();
    }

    private MealyTransition<StackState<S, I, MealyMachine<S, I, T, O>>, O> sink() {
        return new MealyTransition<>(StackState.sink(), outputAlphabet.getErrorSymbol());
    }

}