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
package net.automatalib.serialization.saf;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.InputModelSerializer;

/**
 * Facade for SAF (simple automaton format) parsing. This class provides several static methods to access
 * {@link InputModelSerializer}s for SAF descriptions of {@link DFA}s, {@link MealyMachine}s, and {@link NFA}s.
 */
public final class SAFWriters {

    private SAFWriters() {
        // prevent instantiation
    }

    /**
     * Returns an {@link InputModelSerializer} for writing {@link DFA}s.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelSerializer} for writing {@link DFA}s
     */
    public static <S, I, A extends DFA<S, I>> InputModelSerializer<I, A> dfa() {
        return new SAFOutput<>(AutomatonType.DFA, new AcceptanceEncoder(), SinglePropertyEncoder.nullEncoder());
    }

    /**
     * Returns an {@link InputModelSerializer} for writing {@link MealyMachine}s.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         (concrete) automaton type
     * @param encoder
     *         the encoder for the transition properties
     *
     * @return an {@link InputModelSerializer} for writing {@link MealyMachine}s
     */
    public static <S, I, T, O, A extends MealyMachine<S, I, T, O>> InputModelSerializer<I, A> mealy(
            SinglePropertyEncoder<O> encoder) {
        return new SAFOutput<>(AutomatonType.MEALY, BlockPropertyEncoder.noopEncoder(), encoder);
    }

    /**
     * Returns an {@link InputModelSerializer} for writing {@link NFA}s.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelSerializer} for writing {@link NFA}s
     */
    public static <S, I, A extends NFA<S, I>> InputModelSerializer<I, A> nfa() {
        return new SAFOutput<>(AutomatonType.NFA, new AcceptanceEncoder(), SinglePropertyEncoder.nullEncoder());
    }

}
