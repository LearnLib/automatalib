/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.serialization.dot;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.graphs.base.compact.CompactSimpleGraph;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class DOTSerializationTest {

    @Test
    public void testRegularExport() throws IOException {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');

        // @formatter:off
        final MealyMachine<?, Character, ?, Character> automaton =
                AutomatonBuilders.forMealy(new CompactMealy<Character, Character>(alphabet))
                                 .withInitial("s0")
                                 .from("s0")
                                    .on('a').withOutput('1').to("s1")
                                 .from("s1")
                                    .on('b').withOutput('2').to("s2")
                                 .from("s2")
                                    .on('c').withOutput('3').to("s0")
                                 .create();
        // @formatter:on

        ThrowingWriter writer = w -> GraphDOT.write(automaton, alphabet, w);
        checkDOTOutput(writer, "/mealy.dot");
    }

    @Test
    public void testVisualizationHelper() throws IOException {

        final CompactSimpleGraph<Void> graph = new CompactSimpleGraph<>();

        final int init = graph.addIntNode();
        int iter = init;

        for (int i = 0; i < 10; i++) {
            int next = graph.addIntNode();
            graph.connect(iter, next);
            iter = next;
        }

        graph.connect(iter, init);

        ThrowingWriter writer = w -> GraphDOT.write(graph,
                                                    w,
                                                    GraphDOT.toDOTVisualizationHelper(new RedTransitionHelper<>()),
                                                    new PreambleHelper<>());
        checkDOTOutput(writer, "/graph.dot");
    }

    private void checkDOTOutput(ThrowingWriter writer, String resource) throws IOException {

        final StringWriter dotWriter = new StringWriter();
        final StringWriter expectedWriter = new StringWriter();

        final Reader mealyReader =
                IOUtil.asBufferedUTF8Reader(DOTSerializationTest.class.getResourceAsStream(resource));
        IOUtil.copy(mealyReader, expectedWriter);

        writer.write(dotWriter);

        Assert.assertEquals(dotWriter.toString(), expectedWriter.toString());
    }

    private interface ThrowingWriter {

        void write(Writer w) throws IOException;
    }

    private static class RedTransitionHelper<E> implements VisualizationHelper<Integer, E> {

        @Override
        public boolean getNodeProperties(Integer node, Map<String, String> properties) {
            if (node % 2 == 0) {
                properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLECIRCLE);
            }
            return true;
        }

        @Override
        public boolean getEdgeProperties(Integer src, E edge, Integer tgt, Map<String, String> properties) {
            properties.put(NodeAttrs.COLOR, "red");
            return true;
        }
    }

    private static class PreambleHelper<N, E> implements DOTVisualizationHelper<N, E> {

        @Override
        public void writePreamble(Appendable a) throws IOException {
            a.append("// this is a preamble").append(System.lineSeparator());
        }

        @Override
        public void writePostamble(Appendable a) throws IOException {
            a.append("// this is a postamble").append(System.lineSeparator());
        }

        @Override
        public boolean getNodeProperties(N node, Map<String, String> properties) {
            return true;
        }

        @Override
        public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
            return true;
        }
    }
}
