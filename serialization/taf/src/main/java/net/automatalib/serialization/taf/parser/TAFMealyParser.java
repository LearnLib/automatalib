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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

final class TAFMealyParser<S, T, A extends MutableMealyMachine<S, String, T, String>>
        implements InputModelDeserializer<String, A> {

    private final AutomatonCreator<A, String> creator;

    TAFMealyParser(AutomatonCreator<A, String> creator) {
        this.creator = creator;
    }

    @Override
    public InputModelData<String, A> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            final InternalTAFParser parser = new InternalTAFParser(r);
            final DefaultTAFBuilderMealy<A, S, T> builder = new DefaultTAFBuilderMealy<>(parser, creator);

            try {
                parser.mealy(builder);
            } catch (ParseException ex) {
                throw new FormatException(ex);
            }

            final Alphabet<String> alphabet = builder.getAlphabet(); // finish() will clear the variable
            return new InputModelData<>(builder.finish(), alphabet);
        }
    }
}
