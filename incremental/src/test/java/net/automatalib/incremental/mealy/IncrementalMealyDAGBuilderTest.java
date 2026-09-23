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
package net.automatalib.incremental.mealy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;
import net.automatalib.incremental.IntegrationUtil;
import net.automatalib.incremental.IntegrationUtil.ParsedTraces;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IncrementalMealyDAGBuilderTest extends AbstractIncrementalMealyBuilderTest {

    @Override
    protected <I, O> IncrementalMealyBuilder<I, O> createIncrementalMealyBuilder(Alphabet<I> alphabet) {
        return new IncrementalMealyDAGBuilder<>(alphabet);
    }

    @Override
    protected String getDOTResource() {
        return "/mealy/dag.dot";
    }

    /**
     * Test case based on <a href="https://github.com/LearnLib/automatalib/issues/79">AutomataLib issue #79</a>.
     */
    @Test
    public void testAutomataLib79() {
        final Alphabet<Character> alphabet = Alphabets.characters('0', '3');

        final Word<Character> in1 = Word.fromString("12");
        final Word<Character> in2 = Word.fromString("302");
        final Word<Character> in3 = Word.fromString("3023102");
        final Word<Character> in4 = Word.fromString("30231023");

        final Word<Character> out1 = Word.fromString("21");
        final Word<Character> out2 = Word.fromString("101");
        final Word<Character> out3 = Word.fromString("1013201");
        final Word<Character> out4 = Word.fromString("10132010");

        final IncrementalMealyDAGBuilder<Character, Character> builder = new IncrementalMealyDAGBuilder<>(alphabet);

        builder.insert(in1, out1);
        builder.insert(in2, out2);
        builder.insert(in3, out3);
        builder.insert(in4, out4); // threw a ConflictException previously

        Assert.assertEquals(builder.lookup(in1), out1);
        Assert.assertEquals(builder.lookup(in2), out2);
        Assert.assertEquals(builder.lookup(in3), out3);
        Assert.assertEquals(builder.lookup(in4), out4);
    }

    /**
     * This test case validates a set of traces from an external system which exposed an issue in confluence
     * propagation.
     */
    @Test
    public void testIntegration() throws IOException {
        validateTraces("/spa/mealy_traces.gz");
    }

    /**
     * Test case based on <a href="https://github.com/LearnLib/learnlib/issues/76">LearnLib issue #76</a>.
     */
    @Test
    public void testLearnLib76() throws IOException {
        validateTraces("/learnlib76/mealy.gz");
    }

    /**
     * This test case validates a bug in finding separating words which occurred in an external benchmark.
     */
    @Test
    public void testSeparatingWord() throws IOException, FormatException {
        IncrementalMealyBuilder<Integer, Integer> cache = validateTraces("/mealy/integration/mealy.gz");

        var input = Word.fromSymbols(9, 0, 9, 6, 6, 1, 6, 6, 11, 11, 8, 0, 5, 0);
        var hypOut = Word.fromSymbols(2, 3, 2, 2, 2, 2, 2, 2, 5, 5, 6, 2, 7, 0);
        var incOut = Word.fromSymbols(2, 3, 2, 2, 2, 2, 2, 2, 5, 5, 6, 2, 7, 3);

        try (InputStream is = IncrementalMealyDAGBuilderTest.class.getResourceAsStream("/mealy/integration/mealy.dot")) {
            var model = DOTParsers.mealy(IncrementalMealyDAGBuilderTest::parseEdges).readModel(is);
            var automaton = model.model;
            var inputs = model.alphabet;

            Assert.assertEquals(hypOut, automaton.computeOutput(input));
            Assert.assertEquals(incOut, cache.lookup(input));

            var sepWord = cache.findSeparatingWord(automaton, inputs, false);

            Assert.assertNotNull(sepWord);
            Assert.assertNotEquals(cache.lookup(sepWord), automaton.computeOutput(sepWord));
        }
    }

    private static Pair<Integer, Integer> parseEdges(Map<String, String> attr) {
        final String label = attr.get(EdgeAttrs.LABEL);

        Assert.assertNotNull(label);

        final String[] tokens = label.split("/");

        Assert.assertEquals(tokens.length, 2);

        return Pair.of(Integer.parseInt(tokens[0].trim()), Integer.parseInt(tokens[1].trim()));
    }

    private IncrementalMealyBuilder<Integer, Integer> validateTraces(String pathToTraces) throws IOException {
        final ParsedTraces<Integer, Word<Integer>> parsedData = IntegrationUtil.parseMealyTraces(pathToTraces);
        final Alphabet<Integer> alphabet = parsedData.alphabet;
        final List<Pair<Word<Integer>, Word<Integer>>> traces = parsedData.traces;

        final IncrementalMealyBuilder<Integer, Integer> cache = createIncrementalMealyBuilder(alphabet);

        // test insertion without errors
        for (Pair<Word<Integer>, Word<Integer>> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final Word<Integer> value = trace.getSecond();

            cache.insert(input, value);
            // test direct caching behavior
            Assert.assertEquals(value, cache.lookup(input));
        }

        // test global caching behavior
        for (Pair<Word<Integer>, Word<Integer>> trace : traces) {
            final Word<Integer> input = trace.getFirst();
            final Word<Integer> value = trace.getSecond();

            Assert.assertEquals(value, cache.lookup(input));
        }

        return cache;
    }
}
