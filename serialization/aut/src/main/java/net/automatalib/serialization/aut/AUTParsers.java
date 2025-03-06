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
package net.automatalib.serialization.aut;

import java.util.function.Function;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.impl.CompactSimpleAutomaton;
import net.automatalib.automaton.simple.SimpleAutomaton;
import net.automatalib.serialization.InputModelDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade for AUT parsing. For further information about the AUT format, see <a
 * href="http://cadp.inria.fr/man/aut.html">http://cadp.inria.fr/man/aut.html</a>.
 */
public final class AUTParsers {

    private AUTParsers() {
        // prevent instantiation
    }

    /**
     * Constructs a {@link InputModelDeserializer} that reads an automaton description and writes it into a
     * {@link SimpleAutomaton}.
     *
     * @return a {@link InputModelDeserializer} that reads an automaton description
     */
    public static InputModelDeserializer<String, CompactSimpleAutomaton<String>> parser() {
        return parser(Function.identity());
    }

    /**
     * Constructs a {@link InputModelDeserializer} that reads an automaton description and writes it into a
     * {@link SimpleAutomaton}.
     *
     * @param inputTransformer
     *         the transformer of String representatives to alphabet symbols
     * @param <I>
     *         input symbol type
     *
     * @return a {@link InputModelDeserializer} that reads an automaton description
     */
    public static <I> InputModelDeserializer<I, CompactSimpleAutomaton<I>> parser(Function<String, I> inputTransformer) {
        return parser(inputTransformer, new CompactSimpleAutomaton.Creator<>());
    }

    /**
     * Constructs a {@link InputModelDeserializer} that reads an automaton description and writes it into a given
     * {@link MutableAutomaton}.
     *
     * @param inputTransformer
     *         the transformer of String representatives to alphabet symbols
     * @param creator
     *         the creator to construct the concrete automaton instance
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <TP>
     *         (nullable) transition property type
     * @param <A>
     *         (concrete) automaton type
     *
     * @return a {@link InputModelDeserializer} that reads an automaton description
     */
    public static <I, T, @Nullable TP, A extends MutableAutomaton<Integer, I, T, ?, TP>> InputModelDeserializer<I, A> parser(
            Function<String, I> inputTransformer,
            AutomatonCreator<A, I> creator) {
        return new InternalAUTParser<>(inputTransformer, creator);
    }
}
