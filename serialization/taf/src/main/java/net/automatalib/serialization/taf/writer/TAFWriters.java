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
package net.automatalib.serialization.taf.writer;

import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.InputModelSerializer;

/**
 * Facade for TAF (textual automaton format) writing. This class provides several static methods to access
 * {@link InputModelSerializer}s for TAF descriptions of {@link DFA}s and {@link MealyMachine}s.
 */
public final class TAFWriters {

    private TAFWriters() {
        // prevent instantiation
    }

    /**
     * Returns an {@link InputModelSerializer} for writing {@link DFA}s.
     *
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelSerializer} for writing {@link DFA}s
     */
    public static <I, A extends DFA<?, I>> InputModelSerializer<I, A> dfa() {
        return new TAFDFAWriter<>();
    }

    /**
     * Returns an {@link InputModelSerializer} for writing {@link MealyMachine}s.
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelSerializer} for writing {@link MealyMachine}s
     */
    public static <I, O, A extends MealyMachine<?, I, ?, O>> InputModelSerializer<I, A> mealy() {
        return new TAFMealyWriter<>();
    }

    /**
     * Returns an {@link InputModelSerializer} for writing generic {@link FiniteAlphabetAutomaton}s. During
     * serialization, the writer checks whether the given automaton is either a {@link DFA} or a {@link MealyMachine}
     * and delegates to the respective serializers.
     *
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelSerializer} for writing {@link FiniteAlphabetAutomaton}s
     *
     * @see #dfa()
     * @see #mealy()
     */
    public static <I, A extends FiniteAlphabetAutomaton<?, I, ?>> InputModelSerializer<I, A> any() {
        return new TAFAnyWriter<>();
    }

}
