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
package net.automatalib.incremental.dfa;

import java.io.IOException;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.common.util.Pair;
import net.automatalib.incremental.IntegrationUtil;
import net.automatalib.incremental.IntegrationUtil.ParsedTraces;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IncrementalPCDFADAGBuilderTest extends AbstractIncrementalPCDFABuilderTest {

    @Override
    protected <I> IncrementalDFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet) {
        return new IncrementalPCDFADAGBuilder<>(alphabet);
    }

    @Override
    protected String getDOTResource() {
        return "/dfa/pc_dag.dot";
    }

    /**
     * (DFA-adjusted) test-case based on <a href="https://github.com/LearnLib/automatalib/issues/79">AutomataLib
     * issue #79</a>.
     */
    @Test
    public void testAutomataLib79() {
        final Alphabet<Character> alphabet = Alphabets.characters('0', '3');

        final Word<Character> in1 = Word.fromString("12");
        final Word<Character> in2 = Word.fromString("302");
        final Word<Character> in3 = Word.fromString("3023102");
        final Word<Character> in4 = Word.fromString("30231023102");

        final IncrementalPCDFADAGBuilder<Character> builder = new IncrementalPCDFADAGBuilder<>(alphabet);

        builder.insert(in1, true);
        builder.insert(in2, true);
        builder.insert(in3, true);
        builder.insert(in4, false); // threw a ConflictException previously

        Assert.assertEquals(builder.lookup(in1), Acceptance.TRUE);
        Assert.assertEquals(builder.lookup(in2), Acceptance.TRUE);
        Assert.assertEquals(builder.lookup(in3), Acceptance.TRUE);
        Assert.assertEquals(builder.lookup(in4), Acceptance.FALSE);
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in confluence
     * propagation.
     */
    @Test
    public void testIntegrationSPA() throws IOException {
        runIntegration("/spa/dfa_traces.gz");
    }

    /**
     * This tests case validates a set of traces from an external system which exposed an issue in state purging.
     */
    @Test
    public void testIntegrationSBA() throws IOException {
        runIntegration("/sba/dfa_traces.gz");
    }

    private void runIntegration(String path) throws IOException {
        final ParsedTraces<Integer, Boolean> parsedData = IntegrationUtil.parseDFATraces(path);
        final Alphabet<Integer> alphabet = parsedData.alphabet;
        final List<Pair<Word<Integer>, Boolean>> traces = parsedData.traces;

        Assert.assertTrue(IntegrationUtil.isPrefixClosed(traces));

        final IncrementalDFABuilder<Integer> cache = createIncrementalPCDFABuilder(alphabet);

        // test insertion without errors
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            cache.insert(trace.getFirst(), trace.getSecond());
        }

        // test caching properties
        for (Pair<Word<Integer>, Boolean> trace : traces) {
            final Word<Integer> word = trace.getFirst();
            final boolean accepted = trace.getSecond();

            Assert.assertEquals(accepted, cache.lookup(word).toBoolean());

            // test prefix-closedness
            if (accepted) {
                for (Word<Integer> prefix : word.prefixes(false)) {
                    Assert.assertTrue(cache.lookup(prefix).toBoolean());
                }
            }
        }
    }
}
