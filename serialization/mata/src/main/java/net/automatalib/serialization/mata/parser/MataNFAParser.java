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
package net.automatalib.serialization.mata.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

public class MataNFAParser<S, I, A extends MutableNFA<S, I>> implements InputModelDeserializer<I, A> {

    private final AutomatonCreator<A, I> creator;
    private final Function<String, I> symbolParser;

    public MataNFAParser(AutomatonCreator<A, I> creator, Function<String, I> symbolParser) {
        this.creator = creator;
        this.symbolParser = symbolParser;
    }

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            final ExplicitMataParser parser = new ExplicitMataParser(r);

            try {
                parser.parse();
            } catch (ParseException ex) {
                throw new FormatException(ex);
            }

            return parser.extract(creator, symbolParser);
        }
    }
}
