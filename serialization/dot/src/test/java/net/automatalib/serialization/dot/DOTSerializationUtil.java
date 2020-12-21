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
package net.automatalib.serialization.dot;

import java.net.URL;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMoore;
import net.automatalib.automata.transducers.impl.compact.CompactSST;
import net.automatalib.graphs.base.compact.CompactGraph;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
final class DOTSerializationUtil {

    static final String DFA_RESOURCE = "/dfa.dot";
    static final String NFA_RESOURCE = "/nfa.dot";
    static final String NFA2_RESOURCE = "/nfa2.dot";
    static final String MEALY_RESOURCE = "/mealy.dot";
    static final String MOORE_RESOURCE = "/moore.dot";
    static final String SST_RESOURCE = "/sst.dot";
    static final String GRAPH_RESOURCE = "/graph.dot";

    static final String FAULTY_AUTOMATON_RESOURCE = "/faulty_automaton.dot";
    static final String FAULTY_GRAPH_RESOURCE = "/faulty_graph.dot";

    static final String PARSER_RESOURCE = "/parser.dot";

    static final CompactDFA<String> DFA;
    static final CompactNFA<String> NFA;
    static final CompactMealy<String, String> MEALY;
    static final CompactMoore<String, String> MOORE;
    static final CompactSST<Character, Character> SST;
    static final CompactGraph<String, String> GRAPH;

    static {
        // @formatter:off
        DFA = AutomatonBuilders.newDFA(Alphabets.closedCharStringRange('a', 'c'))
                               .withInitial("s0")
                               .from("s0").on("a").to("s1")
                               .from("s1").on("b").to("s2")
                               .from("s2").on("c").to("s0")
                               .withAccepting("s2")
                               .create();

        NFA = AutomatonBuilders.newNFA(Alphabets.closedCharStringRange('a', 'c'))
                               .withInitial("s0")
                               .from("s0").on("a").to("s1", "s2")
                               .from("s1").on("b").to("s2", "s0")
                               .from("s2").on("c").to("s0", "s1")
                               .withAccepting("s0")
                               .create();

        MEALY = AutomatonBuilders.<String, String>newMealy(Alphabets.closedCharStringRange('a', 'c'))
                                 .withInitial("s0")
                                 .from("s0").on("a").withOutput("1").to("s1")
                                 .from("s1").on("b").withOutput("2").to("s2")
                                 .from("s2").on("c").withOutput("3").to("s0")
                                 .create();

        MOORE = AutomatonBuilders.<String, String>newMoore(Alphabets.closedCharStringRange('1', '2'))
                                 .withInitial("s0", "a")
                                 .withOutput("s1", "b")
                                 .withOutput("s2", "c")
                                 .from("s0").on("1").to("s1")
                                 .from("s1").on("1").to("s2")
                                 .from("s2").on("1").to("s0")
                                 .from("s0").on("2").to("s2")
                                 .from("s1").on("2").to("s0")
                                 .from("s2").on("2").to("s1")
                                 .create();

        SST = AutomatonBuilders.<Character, Character>newSST(Alphabets.characters('a', 'c'))
                               .withInitial("s0")
                               .withStateProperty(Word.fromCharSequence("x"), "s0")
                               .withStateProperty(Word.epsilon(), "s1", "s2", "s3")
                               .from("s0").on('a', 'b', 'c').withProperty(Word.fromCharSequence("x")).to("s1")
                               .from("s1").on('a').withProperty(Word.fromCharSequence("xx")).to("s2")
                               .from("s1").on('b').withProperty(Word.fromCharSequence("yx")).to("s2")
                               .from("s1").on('c').withProperty(Word.fromCharSequence("zx")).to("s2")
                               .from("s2").on('a').withProperty(Word.fromCharSequence("xx")).to("s3")
                               .from("s2").on('b').withProperty(Word.fromCharSequence("yx")).to("s3")
                               .from("s2").on('c').withProperty(Word.fromCharSequence("zx")).to("s3")
                               .from("s3").on('a', 'b', 'c').withProperty(Word.epsilon()).to("s1")
                               .create();
        // @formatter:on

        // create of full binary tree of depth 4
        GRAPH = new CompactGraph<>();
        GRAPH.addIntNode();

        for (int i = 2; i < 2 << 3; i++) {
            GRAPH.addIntNode();
            GRAPH.connect((i / 2 + i % 2) - 1, i - 1, Integer.toString(i - 1));
        }

        // update to unique labels "<id>_<level>"
        for (Integer n : GRAPH) {
            GRAPH.setNodeProperty(n, n.toString() + '_' + (Integer.SIZE - Integer.numberOfLeadingZeros(n + 1)));
        }
    }

    private DOTSerializationUtil() {}

    static URL getResource(String resource) {
        return DOTSerializationUtil.class.getResource(resource);
    }
}
