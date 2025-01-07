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
package net.automatalib.serialization.saf;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * Facade for SAF (simple automaton format) parsing. This class provides several static methods to access
 * {@link InputModelDeserializer}s for SAF descriptions of {@link DFA}s, {@link MealyMachine}s, and {@link NFA}s.
 */
public final class SAFParsers {

    private SAFParsers() {
        // prevent instantiation
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a
     * {@link CompactDFA} using an {@link Integer}-based alphabet. Convenience method for
     * {@link #dfa(AutomatonCreator)}.
     *
     * @return an {@link InputModelDeserializer} that reads a {@link DFA} description
     *
     * @see #dfa(AutomatonCreator)
     */
    public static InputModelDeserializer<Integer, CompactDFA<Integer>> dfa() {
        return dfa(new CompactDFA.Creator<>());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a given
     * {@link MutableDFA} using an {@link Integer}-based alphabet.
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
    public static <S, A extends MutableDFA<S, Integer>> InputModelDeserializer<Integer, A> dfa(AutomatonCreator<A, Integer> creator) {
        return new SAFNativeInput<>(AutomatonType.DFA,
                                    creator,
                                    new AcceptanceDecoder(),
                                    SinglePropertyDecoder.nullDecoder());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a
     * {@link CompactDFA} using the given alphabet. Input symbols are interpreted index-wise from the automaton
     * description. Convenience method for {@link #dfa(AutomatonCreator, Alphabet)}.
     *
     * @param alphabet
     *         the alphabet to use
     * @param <I>
     *         input symbol type
     *
     * @return an {@link InputModelDeserializer} that reads a {@link DFA} description
     *
     * @see #dfa(AutomatonCreator, Alphabet)
     */
    public static <I> InputModelDeserializer<I, CompactDFA<I>> dfa(Alphabet<I> alphabet) {
        return dfa(new CompactDFA.Creator<>(), alphabet);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link DFA} description and writes it into a given
     * {@link MutableDFA} using the given alphabet. Input symbols are interpreted index-wise from the automaton
     * description.
     *
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param alphabet
     *         the alphabet to use
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelDeserializer} that reads a {@link DFA} description
     */
    public static <S, I, A extends MutableDFA<S, I>> InputModelDeserializer<I, A> dfa(AutomatonCreator<A, I> creator,
                                                                                      Alphabet<I> alphabet) {
        return new SAFInput<>(AutomatonType.DFA,
                              creator,
                              alphabet,
                              new AcceptanceDecoder(),
                              SinglePropertyDecoder.nullDecoder());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * {@link CompactMealy} using an {@link Integer}-based alphabet. Convenience method for
     * {@link #mealy(AutomatonCreator, SinglePropertyDecoder)}.
     *
     * @param <O>
     *         output symbol type
     * @param decoder
     *         the decoder for the transition properties
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     *
     * @see #mealy(AutomatonCreator, SinglePropertyDecoder)
     */
    public static <O> InputModelDeserializer<Integer, CompactMealy<Integer, O>> mealy(SinglePropertyDecoder<O> decoder) {
        return mealy(new CompactMealy.Creator<>(), decoder);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * given {@link MutableMealyMachine} using the given alphabet. Input symbols are interpreted index-wise from the
     * automaton description.
     *
     * @param <S>
     *         state type
     * @param <T>
     *         transition type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         (concrete) automaton type
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param decoder
     *         the decoder for the transition properties
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     */
    public static <S, T, O, A extends MutableMealyMachine<S, Integer, T, O>> InputModelDeserializer<Integer, A> mealy(
            AutomatonCreator<A, Integer> creator,
            SinglePropertyDecoder<O> decoder) {
        return new SAFNativeInput<>(AutomatonType.MEALY, creator, BlockPropertyDecoder.nullDecoder(), decoder);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * {@link CompactMealy} using the given alphabet. Input symbols are interpreted index-wise from the automaton
     * description. Convenience method for {@link #mealy(AutomatonCreator, Alphabet, SinglePropertyDecoder)}
     *
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param alphabet
     *         the alphabet to use
     * @param decoder
     *         the decoder for the transition properties
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     *
     * @see #mealy(AutomatonCreator, Alphabet, SinglePropertyDecoder)
     */
    public static <I, O> InputModelDeserializer<I, CompactMealy<I, O>> mealy(Alphabet<I> alphabet,
                                                                             SinglePropertyDecoder<O> decoder) {
        return mealy(new CompactMealy.Creator<>(), alphabet, decoder);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads a {@link MealyMachine} description and writes it into a
     * given {@link MutableMealyMachine} using the given alphabet. Input symbols are interpreted index-wise from the
     * automaton description.
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
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param alphabet
     *         the alphabet to use
     * @param decoder
     *         the decoder for the transition properties
     *
     * @return an {@link InputModelDeserializer} that reads a {@link MealyMachine} description
     */
    public static <S, I, T, O, A extends MutableMealyMachine<S, I, T, O>> InputModelDeserializer<I, A> mealy(
            AutomatonCreator<A, I> creator,
            Alphabet<I> alphabet,
            SinglePropertyDecoder<O> decoder) {
        return new SAFInput<>(AutomatonType.MEALY, creator, alphabet, BlockPropertyDecoder.nullDecoder(), decoder);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads an {@link NFA} description and writes it into a
     * {@link CompactNFA} using an {@link Integer}-based alphabet. Convenience method for
     * {@link #nfa(AutomatonCreator)}.
     *
     * @return an {@link InputModelDeserializer} that reads an {@link NFA} description
     *
     * @see #nfa(AutomatonCreator)
     */
    public static InputModelDeserializer<Integer, CompactNFA<Integer>> nfa() {
        return nfa(new CompactNFA.Creator<>());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads an {@link NFA} description and writes it into a given
     * {@link MutableNFA} using an {@link Integer}-based alphabet.
     *
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param <S>
     *         state type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelDeserializer} that reads an {@link NFA} description
     */
    public static <S, A extends MutableNFA<S, Integer>> InputModelDeserializer<Integer, A> nfa(AutomatonCreator<A, Integer> creator) {
        return new SAFNativeInput<>(AutomatonType.NFA,
                                    creator,
                                    new AcceptanceDecoder(),
                                    SinglePropertyDecoder.nullDecoder());
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads an {@link NFA} description and writes it into a
     * {@link CompactNFA} using the given alphabet. Input symbols are interpreted index-wise from the automaton
     * description. Convenience method for {@link #nfa(AutomatonCreator, Alphabet)}.
     *
     * @param alphabet
     *         the alphabet to use
     * @param <I>
     *         input symbol type
     *
     * @return an {@link InputModelDeserializer} that reads an {@link NFA} description
     *
     * @see #nfa(AutomatonCreator, Alphabet)
     */
    public static <I> InputModelDeserializer<I, CompactNFA<I>> nfa(Alphabet<I> alphabet) {
        return nfa(new CompactNFA.Creator<>(), alphabet);
    }

    /**
     * Constructs an {@link InputModelDeserializer} that reads an {@link NFA} description and writes it into a given
     * {@link MutableNFA} using the given alphabet. Input symbols are interpreted index-wise from the automaton
     * description.
     *
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param alphabet
     *         the alphabet to use
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return an {@link InputModelDeserializer} that reads an {@link NFA} description
     */
    public static <S, I, A extends MutableNFA<S, I>> InputModelDeserializer<I, A> nfa(AutomatonCreator<A, I> creator,
                                                                                      Alphabet<I> alphabet) {
        return new SAFInput<>(AutomatonType.NFA,
                              creator,
                              alphabet,
                              new AcceptanceDecoder(),
                              SinglePropertyDecoder.nullDecoder());
    }
}
