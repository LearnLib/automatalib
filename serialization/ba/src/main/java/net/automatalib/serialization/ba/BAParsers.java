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
package net.automatalib.serialization.ba;

import java.util.function.Function;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableFSA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * Facade for BA parsing. For further information about the BA format, see <a
 * href="https://languageinclusion.org/doku.php?id=tools#the_ba_format">
 * https://languageinclusion.org/doku.php?id=tools#the_ba_format</a>.
 */
public final class BAParsers {

    private BAParsers() {
        // prevent instantiation
    }

    /**
     * Returns a parser that reads a string-based {@link DFA} (if possible). May throw an exception during parsing, if
     * the specified automaton is non-deterministic.
     *
     * @return a parser for reading a string-based DFA
     */
    public static InputModelDeserializer<String, CompactDFA<String>> dfa() {
        return dfa(Function.identity());
    }

    /**
     * Returns a parser that reads a typed {@link DFA} (if possible). May throw an exception during parsing, if the
     * specified automaton is non-deterministic.
     *
     * @param labelParser
     *         the type-safe parser for input symbols
     * @param <I>
     *         input symbol type
     *
     * @return a parser for reading a typed DFA
     */
    public static <I> InputModelDeserializer<I, CompactDFA<I>> dfa(Function<String, I> labelParser) {
        return fsa(new CompactDFA.Creator<>(), labelParser);
    }

    /**
     * Returns a parser that reads a string-based {@link NFA}.
     *
     * @return a parser for reading a string-based NFA
     */
    public static InputModelDeserializer<String, CompactNFA<String>> nfa() {
        return nfa(Function.identity());
    }

    /**
     * Returns a parser that reads a typed {@link NFA}.
     *
     * @param labelParser
     *         the type-safe parser for input symbols
     * @param <I>
     *         input symbol type
     *
     * @return a parser for reading a typed NFA
     */
    public static <I> InputModelDeserializer<I, CompactNFA<I>> nfa(Function<String, I> labelParser) {
        return fsa(new CompactNFA.Creator<>(), labelParser);
    }

    /**
     * Returns a parser that reads a custom string-based automaton.
     *
     * @param creator
     *         the creator for the concrete automaton instance
     * @param <S>
     *         state type
     * @param <A>
     *         the concrete automaton type
     *
     * @return a parser for reading a custom string-based automaton
     */
    public static <S, A extends MutableFSA<S, String>> InputModelDeserializer<String, A> fsa(AutomatonCreator<A, String> creator) {
        return fsa(creator, Function.identity());
    }

    /**
     * Returns a parser that reads a custom typed automaton.
     *
     * @param creator
     *         the creator for the concrete automaton instance
     * @param labelParser
     *         the type-safe parser for input symbols
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         the concrete automaton type
     *
     * @return a parser for reading a custom typed automaton
     */
    public static <S, I, A extends MutableFSA<S, I>> InputModelDeserializer<I, A> fsa(AutomatonCreator<? extends A, I> creator,
                                                                                      Function<String, I> labelParser) {
        return new InternalBAParser<>(creator, labelParser);
    }
}
