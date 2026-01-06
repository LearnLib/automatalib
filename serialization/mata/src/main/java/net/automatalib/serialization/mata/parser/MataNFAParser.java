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
package net.automatalib.serialization.mata.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * Parser for reading {@link NFA}s from the <a
 * href="https://github.com/VeriFIT/mata/blob/devel/AUTOMATAFORMAT.md">NFA-explicit</a> format.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <A>
 *         concrete automaton type
 */
public class MataNFAParser<S, I, A extends MutableNFA<S, I>> implements InputModelDeserializer<I, A> {

    private final AutomatonCreator<A, I> creator;
    private final Function<String, I> symbolParser;

    /**
     * Constructor.
     *
     * @param creator
     *         the creator of the concrete NFA instance
     * @param symbolParser
     *         the parser for transforming (string-based) labels to concrete input symbols
     */
    public MataNFAParser(AutomatonCreator<A, I> creator, Function<String, I> symbolParser) {
        this.creator = creator;
        this.symbolParser = symbolParser;
    }

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException, FormatException {
        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            return parse(r, creator, symbolParser);
        }
    }

    /**
     * Reads the contents from the given input stream and de-serializes it into a model instance.
     *
     * @param reader
     *         the reader to read the contents from
     * @param creator
     *         the creator of the concrete NFA instance
     * @param symbolParser
     *         the parser for transforming (string-based) labels to concrete input symbols
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         concrete automaton type
     *
     * @return the de-serialized model data
     *
     * @throws FormatException
     *         if the content of the stream was not in the expected format
     */
    public static <S, I, A extends MutableNFA<S, I>> InputModelData<I, A> parse(Reader reader,
                                                                                AutomatonCreator<A, I> creator,
                                                                                Function<String, I> symbolParser)
            throws FormatException {

        final ExplicitMataParser parser = new ExplicitMataParser(reader);

        try {
            parser.parse();
        } catch (ParseException ex) {
            throw new FormatException(ex);
        }

        return parser.extract(creator, symbolParser);
    }
}
