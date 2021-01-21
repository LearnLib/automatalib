/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.automata.transducers.impl;

/**
 * A transition of a mealy machine, comprising a successor state and an output symbol.
 *
 * @param <S>
 *         state class.
 * @param <O>
 *         output symbol class.
 *
 * @author Malte Isberner
 */
public class MealyTransition<S, O> {

    private final S successor;
    private O output;

    /**
     * Constructor.
     *
     * @param successor
     *         successor state.
     * @param output
     *         output symbol.
     */
    public MealyTransition(S successor, O output) {
        this.successor = successor;
        this.output = output;
    }

    /**
     * Retrieves the output symbol.
     *
     * @return the output symbol.
     */
    public O getOutput() {
        return output;
    }

    /**
     * Sets the output symbol.
     *
     * @param output
     *         the new output symbol.
     */
    public void setOutput(O output) {
        this.output = output;
    }

    /**
     * Retrieves the successor state.
     *
     * @return the successor state.
     */
    public S getSuccessor() {
        return successor;
    }
}
