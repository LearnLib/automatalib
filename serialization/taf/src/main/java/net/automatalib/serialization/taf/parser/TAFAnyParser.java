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
package net.automatalib.serialization.taf.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.IOUtil;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

final class TAFAnyParser implements InputModelDeserializer<String, FiniteAlphabetAutomaton<?, String, ?>> {

    @Override
    public InputModelData<String, FiniteAlphabetAutomaton<?, String, ?>> readModel(InputStream is) throws IOException, FormatException {

        try (Reader r = IOUtil.asNonClosingUTF8Reader(is)) {
            final InternalTAFParser parser = new InternalTAFParser(r);

            try {
                final Type type = parser.type();
                switch (type) {
                    case DFA: {
                        final DefaultTAFBuilderDFA<CompactDFA<String>, Integer> builder =
                                new DefaultTAFBuilderDFA<>(parser, new CompactDFA.Creator<>());
                        parser.dfaBody(builder);
                        return new InputModelData<>(builder.finish(), builder.getAlphabet());
                    }
                    case MEALY: {
                        final DefaultTAFBuilderMealy<CompactMealy<String, String>, Integer, CompactTransition<String>>
                                builder = new DefaultTAFBuilderMealy<>(parser, new CompactMealy.Creator<>());
                        parser.mealyBody(builder);
                        return new InputModelData<>(builder.finish(), builder.getAlphabet());
                    }
                    default:
                        throw new IllegalStateException("Unknown type " + type);
                }
            } catch (ParseException ex) {
                throw new FormatException(ex);
            }
        }
    }
}
