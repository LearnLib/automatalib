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
package net.automatalib.serialization.taf.parser;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * Facade for TAF (textual automaton format) parsing. This class provides several static methods to access
 * {@link InputModelDeserializer}s for TAF descriptions of {@link DFA}s and {@link MealyMachine}s.
 */
public final class TAFParsers {

    private TAFParsers() {
        // prevent instantiation
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a
     * {@link CompactDFA}. Convenience method for {@link #dfa(AutomatonCreator)}.
     *
     * @return an {@link InputModelDeserializer} that reads a {@link DFA} description
     *
     * @see #dfa(AutomatonCreator)
     */
    public static InputModelDeserializer<String, CompactDFA<String>> dfa() {
        return dfa(new CompactDFA.Creator<>());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a provided
     * {@link MutableDFA}.
     *
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param <S>
     *         state type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelDeserializer} that reads a {@link DFA} description
     */
    public static <S, A extends MutableDFA<S, String>> InputModelDeserializer<String, A> dfa(AutomatonCreator<A, String> creator) {
        return new TAFDFAParser<>(creator);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * {@link CompactMealy}. Convenience method for {@link #mealy(AutomatonCreator)}.
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     *
     * @see #mealy(AutomatonCreator)
     */
    public static InputModelDeserializer<String, CompactMealy<String, String>> mealy() {
        return mealy(new CompactMealy.Creator<>());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * provided {@link MutableMealyMachine}.
     *
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param <S>
     *         state type
     * @param <T>
     *         transition type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     */
    public static <S, T, A extends MutableMealyMachine<S, String, T, String>> InputModelDeserializer<String, A> mealy(
            AutomatonCreator<A, String> creator) {
        return new TAFMealyParser<>(creator);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads either a {@link DFA} or a {@link MealyMachine}
     * description and writes it into a {@link FiniteAlphabetAutomaton}.
     *
     * @return an {@link InputModelDeserializer} that reads either a {@link DFA} or a {@link MealyMachine} description
     *
     * @see #dfa()
     * @see #mealy()
     */
    public static InputModelDeserializer<String, FiniteAlphabetAutomaton<?, String, ?>> any() {
        return new TAFAnyParser();
    }
}
