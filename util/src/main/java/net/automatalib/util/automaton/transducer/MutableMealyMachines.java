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
package net.automatalib.util.automaton.transducer;

import java.util.Collection;

import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;

public final class MutableMealyMachines {

    private MutableMealyMachines() {
        // prevent instantiation
    }

    public static <I, O> void complete(MutableMealyMachine<?, I, ?, O> mealy,
                                       Collection<? extends I> inputs,
                                       O undefinedOutput) {
        complete(mealy, inputs, undefinedOutput, false);
    }

    public static <S, I, O> void complete(MutableMealyMachine<S, I, ?, O> mealy,
                                          Collection<? extends I> inputs,
                                          O undefinedOutput,
                                          boolean minimize) {
        S sink = null;

        for (S state : mealy) {
            for (I input : inputs) {
                S succ = mealy.getSuccessor(state, input);
                if (succ == null) {
                    if (sink == null) {
                        sink = mealy.addState();
                        for (I inputSym : inputs) {
                            mealy.addTransition(sink, inputSym, sink, undefinedOutput);
                        }
                    }
                    mealy.addTransition(state, input, sink, undefinedOutput);
                }
            }
        }

        if (minimize) {
            HopcroftMinimizer.minimizeMealyInvasive(mealy, inputs);
        }
    }
}
