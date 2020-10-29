/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.incremental;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.Pair;
import net.automatalib.incremental.dfa.IncrementalDFADAGBuilderTest;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

/**
 * Utility class for integration tests.
 *
 * @author frohme
 */
public final class IntegrationUtil {

    private IntegrationUtil() {
        // do not instantiate
    }

    /**
     * This method returns traces from an external system, which exposed some issues in the DAG-based caches.
     * <p>
     * To reduce the size of the data, the traces have been encoded as shorts and follow the following structure:
     * <ol>
     *     <li>number of input symbols</li>
     *     <li>number of traces</li>
     *     <li>traces, encoded as
     *     <ol>
     *         <li>length of trace</li>
     *         <li>symbol 1</li>
     *         <li>symbol 2</li>
     *         <li>...</li>
     *         <li>symbol n</li>
     *         <li>boolean, indicating acceptance/rejection</li>
     *     </ol>
     *     </li>
     * </ol>
     */
    public static ParsedTraces<Integer, Boolean> parseDFATraces() throws IOException {
        try (InputStream res = IntegrationUtil.class.getResourceAsStream("/dfa_traces.gz");
             InputStream buf = IOUtil.asBufferedInputStream(res);
             InputStream is = IOUtil.asUncompressedInputStream(buf); DataInputStream in = new DataInputStream(is)) {

            final short numSyms = in.readShort();
            final short numTraces = in.readShort();

            final Alphabet<Integer> alphabet = Alphabets.integers(0, numSyms - 1);
            final List<Pair<Word<Integer>, Boolean>> traces = new ArrayList<>(numTraces);

            final WordBuilder<Integer> builder = new WordBuilder<>();
            for (int i = 0; i < numTraces; i++) {
                final short length = in.readShort();

                builder.clear();
                builder.ensureCapacity(length);

                for (int j = 0; j < length; j++) {
                    builder.append(alphabet.getSymbol(in.readShort()));
                }

                final boolean result = in.readBoolean();
                traces.add(Pair.of(builder.toWord(), result));
            }

            return new ParsedTraces<>(alphabet, traces);
        }
    }

    /**
     * This method returns traces from an external system, which exposed some issues in the DAG-based caches.
     * <p>
     * To reduce the size of the data, the traces have been encoded as shorts and follow the following structure:
     * <ol>
     *     <li>number of input symbols</li>
     *     <li>number of traces</li>
     *     <li>traces, encoded as
     *     <ul>
     *         <li>length of trace</li>
     *         <li>input symbol 1</li>
     *         <li>output symbol 1</li>
     *         <li>input symbol 2</li>
     *         <li>output symbol 2</li>
     *         <li>...</li>
     *         <li>input symbol n</li>
     *         <li>output symbol n</li>
     *     </ul>
     *     </li>
     * </ol>
     */
    public static ParsedTraces<Integer, Word<Integer>> parseMealyTraces() throws IOException {
        try (InputStream res = IncrementalDFADAGBuilderTest.class.getResourceAsStream("/mealy_traces.gz");
             InputStream buf = IOUtil.asBufferedInputStream(res);
             InputStream is = IOUtil.asUncompressedInputStream(buf); DataInputStream in = new DataInputStream(is)) {

            final int numInputSyms = in.readShort();
            final int numOutputSyms = in.readShort();
            final int numTraces = in.readShort();

            final Alphabet<Integer> inputAlphabet = Alphabets.integers(0, numInputSyms - 1);
            final Alphabet<Integer> outputAlphabet = Alphabets.integers(0, numOutputSyms - 1);
            final List<Pair<Word<Integer>, Word<Integer>>> traces = new ArrayList<>(numTraces);

            final WordBuilder<Integer> inputBuilder = new WordBuilder<>();
            final WordBuilder<Integer> outputBuilder = new WordBuilder<>();
            for (int i = 0; i < numTraces; i++) {
                final int length = in.readShort();

                inputBuilder.clear();
                outputBuilder.clear();
                inputBuilder.ensureCapacity(length);
                outputBuilder.ensureCapacity(length);

                for (int j = 0; j < length; j++) {
                    inputBuilder.append(inputAlphabet.getSymbol(in.readShort()));
                    outputBuilder.append(outputAlphabet.getSymbol(in.readShort()));
                }

                traces.add(Pair.of(inputBuilder.toWord(), outputBuilder.toWord()));
            }
            return new ParsedTraces<>(inputAlphabet, traces);
        }
    }

    /**
     * Validates that for each positively annotated traces, none of its prefixes is annotated negatively.
     */
    public static boolean isPrefixClosed(Collection<Pair<Word<Integer>, Boolean>> traces) {
        final Map<Boolean, List<Word<Integer>>> map = traces.stream()
                                                            .collect(Collectors.partitioningBy(Pair::getSecond,
                                                                                               Collectors.mapping(Pair::getFirst,
                                                                                                                  Collectors
                                                                                                                          .toList())));
        final Set<Word<Integer>> pos = new HashSet<>(map.get(true));
        final Set<Word<Integer>> neg = new HashSet<>(map.get(false));

        for (Word<Integer> t : pos) {
            for (Word<Integer> prefix : t.prefixes(false)) {
                if (neg.contains(prefix)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static final class ParsedTraces<I, D> {

        public final Alphabet<I> alphabet;
        public final List<Pair<Word<I>, D>> traces;

        public ParsedTraces(Alphabet<I> alphabet, List<Pair<Word<I>, D>> traces) {
            this.alphabet = alphabet;
            this.traces = traces;
        }
    }
}
