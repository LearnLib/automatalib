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
package net.automatalib.serialization.dot;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.automaton.transducer.impl.CompactSST;
import net.automatalib.automaton.transducer.impl.FastMealy;
import net.automatalib.automaton.transducer.impl.FastMealyState;
import net.automatalib.graph.impl.CompactGraph;
import net.automatalib.graph.impl.CompactPMPG;
import net.automatalib.graph.impl.CompactPMPGEdge;
import net.automatalib.graph.impl.DefaultCFMPS;
import net.automatalib.ts.modal.impl.CompactMTS;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.MutableProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.ts.modal.transition.impl.ProceduralModalEdgePropertyImpl;
import net.automatalib.word.Word;

final class DOTSerializationUtil {

    static final String EMPTY_RESOURCE = "/empty.dot";
    static final String DFA_RESOURCE = "/dfa.dot";
    static final String NFA_RESOURCE = "/nfa.dot";
    static final String NFA2_RESOURCE = "/nfa2.dot";
    static final String MEALY_RESOURCE = "/mealy.dot";
    static final String MOORE_RESOURCE = "/moore.dot";
    static final String SST_RESOURCE = "/sst.dot";
    static final String GRAPH_RESOURCE = "/graph.dot";
    static final String GRAPH_GLOBAL_RESOURCE = "/graph_global.dot";
    static final String GRAPH_HTML_RESOURCE = "/graph_html.dot";
    static final String MTS_RESOURCE = "/mts.dot";
    static final String CLUSTER_RESOURCE = "/cluster.dot";
    static final String PMPG_RESOURCE = "/pmpg.dot";
    static final String CFMPS_RESOURCE = "/cfmps.dot";
    static final String SPA_RESOURCE = "/spa.dot";
    static final String SBA_RESOURCE = "/sba.dot";
    static final String SPMM_RESOURCE = "/spmm.dot";

    static final String FAULTY_AUTOMATON_RESOURCE = "/faulty_automaton.dot";
    static final String FAULTY_GRAPH_RESOURCE = "/faulty_graph.dot";

    static final String PARSER_RESOURCE = "/parser.dot";

    static final Alphabet<String> STRING_ALPHABET;
    static final Alphabet<String> INTEGER_ALPHABET;

    static final CompactDFA<String> DFA;
    static final CompactNFA<String> NFA;
    static final CompactMealy<String, String> MEALY;
    static final CompactMoore<String, String> MOORE;
    static final CompactSST<Character, Character> SST;
    static final CompactGraph<String, String> GRAPH;
    static final CompactMTS<String> MTS;
    static final DefaultCFMPS<Character, Character> CFMPS;
    static final SPA<?, Character> SPA;
    static final SBA<?, Character> SBA;
    static final SPMM<?, Character, ?, Character> SPMM;

    static {
        STRING_ALPHABET = Alphabets.closedCharStringRange('a', 'c');
        INTEGER_ALPHABET = Alphabets.closedCharStringRange('1', '2');

        DFA = buildDFA();
        NFA = buildNFA();
        MEALY = buildMealy();
        MOORE = buildMoore();
        SST = buildSST();
        GRAPH = buildGraph();
        MTS = buildMTS();
        CFMPS = buildCFMPS();
        SPA = buildSPA();
        SBA = buildSBA();
        SPMM = buildSPMM();
    }

    private DOTSerializationUtil() {}

    static URL getResource(String resource) {
        return DOTSerializationUtil.class.getResource(resource);
    }

    private static CompactDFA<String> buildDFA() {
        final CompactDFA<String> result = new CompactDFA<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState(true);

        result.addTransition(s0, "a", s1);
        result.addTransition(s1, "b", s2);
        result.addTransition(s2, "c", s0);

        return result;
    }

    private static CompactNFA<String> buildNFA() {
        final CompactNFA<String> result = new CompactNFA<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState(true);
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addTransition(s0, "a", s1);
        result.addTransition(s0, "a", s2);
        result.addTransition(s1, "b", s0);
        result.addTransition(s1, "b", s2);
        result.addTransition(s2, "c", s0);
        result.addTransition(s2, "c", s1);

        return result;
    }

    private static CompactMealy<String, String> buildMealy() {
        final CompactMealy<String, String> result = new CompactMealy<>(STRING_ALPHABET);

        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addTransition(s0, "a", s1, "1");
        result.addTransition(s1, "b", s2, "2");
        result.addTransition(s2, "c", s0, "3");

        return result;
    }

    private static CompactMoore<String, String> buildMoore() {
        final CompactMoore<String, String> result = new CompactMoore<>(INTEGER_ALPHABET);

        final Integer s0 = result.addInitialState("a");
        final Integer s1 = result.addState("b");
        final Integer s2 = result.addState("c");

        result.addTransition(s0, "1", s1);
        result.addTransition(s0, "2", s2);
        result.addTransition(s1, "2", s0);
        result.addTransition(s1, "1", s2);
        result.addTransition(s2, "1", s0);
        result.addTransition(s2, "2", s1);

        return result;
    }

    private static CompactSST<Character, Character> buildSST() {
        final CompactSST<Character, Character> result = new CompactSST<>(Alphabets.characters('a', 'c'));

        final Integer s0 = result.addInitialState(Word.fromLetter('x'));
        final Integer s1 = result.addState(Word.epsilon());
        final Integer s2 = result.addState(Word.epsilon());
        final Integer s3 = result.addState(Word.epsilon());

        result.addTransition(s0, 'a', s1, Word.fromLetter('x'));
        result.addTransition(s0, 'b', s1, Word.fromLetter('x'));
        result.addTransition(s0, 'c', s1, Word.fromLetter('x'));

        result.addTransition(s1, 'a', s2, Word.fromString("xx"));
        result.addTransition(s1, 'b', s2, Word.fromString("yx"));
        result.addTransition(s1, 'c', s2, Word.fromString("zx"));

        result.addTransition(s2, 'a', s3, Word.fromString("xx"));
        result.addTransition(s2, 'b', s3, Word.fromString("yx"));
        result.addTransition(s2, 'c', s3, Word.fromString("zx"));

        result.addTransition(s3, 'a', s1, Word.epsilon());
        result.addTransition(s3, 'b', s1, Word.epsilon());
        result.addTransition(s3, 'c', s1, Word.epsilon());

        return result;
    }

    private static CompactGraph<String, String> buildGraph() {
        final CompactGraph<String, String> result = new CompactGraph<>();
        result.addIntNode();

        for (int i = 2; i < 2 << 3; i++) {
            result.addIntNode();
            result.connect((i / 2 + i % 2) - 1, i - 1, Integer.toString(i - 1));
        }

        // update to unique labels "<id>_<level>"
        for (Integer n : result) {
            result.setNodeProperty(n, n.toString() + '_' + (Integer.SIZE - Integer.numberOfLeadingZeros(n + 1)));
        }

        return result;
    }

    private static CompactMTS<String> buildMTS() {
        final CompactMTS<String> result = new CompactMTS<>(STRING_ALPHABET);
        final Integer s0 = result.addInitialState();
        final Integer s1 = result.addState();
        final Integer s2 = result.addState();

        result.addModalTransition(s0, "a", s0, ModalType.MAY);
        result.addModalTransition(s0, "b", s1, ModalType.MUST);
        result.addModalTransition(s0, "c", s2, ModalType.MAY);

        result.addModalTransition(s1, "a", s0, ModalType.MUST);
        result.addModalTransition(s1, "b", s1, ModalType.MAY);
        result.addModalTransition(s1, "c", s2, ModalType.MUST);

        result.addModalTransition(s2, "a", s0, ModalType.MAY);
        result.addModalTransition(s2, "b", s1, ModalType.MUST);
        result.addModalTransition(s2, "c", s2, ModalType.MAY);

        return result;
    }

    private static DefaultCFMPS<Character, Character> buildCFMPS() {
        final ProceduralModalEdgePropertyImpl p1 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.INTERNAL, ModalType.MUST);
        final ProceduralModalEdgePropertyImpl p2 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.INTERNAL, ModalType.MAY);
        final ProceduralModalEdgePropertyImpl p3 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.PROCESS, ModalType.MUST);
        final ProceduralModalEdgePropertyImpl p4 =
                new ProceduralModalEdgePropertyImpl(ProceduralType.PROCESS, ModalType.MAY);

        final CompactPMPG<Character, Character> s = new CompactPMPG<>('?');
        final int s0 = s.addIntNode();
        final int s1 = s.addIntNode(Sets.newHashSet('a', 'b'));
        final int s2 = s.addIntNode(Collections.singleton('c'));

        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e1 = s.connect(s0, s1, p1);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e2 = s.connect(s0, s1, p2);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e3 = s.connect(s1, s2, p3);
        final CompactPMPGEdge<Character, MutableProceduralModalEdgeProperty> e4 = s.connect(s2, s0, p4);

        s.setEdgeLabel(e1, '1');
        s.setEdgeLabel(e2, '2');
        s.setEdgeLabel(e3, '3');
        s.setEdgeLabel(e4, '4');
        s.setInitialNode(s0);

        final CompactPMPG<Character, Character> t = new CompactPMPG<>('?');
        final int t0 = t.addIntNode();
        final int t1 = t.addIntNode(Collections.singleton('d'));

        t.connect(t0, t1, p1);
        t.connect(t0, t1, p2);
        t.connect(t1, t1, p3);
        t.connect(t1, t0, p4);
        t.setFinalNode(t1);

        final Map<Character, CompactPMPG<Character, Character>> pmpgs = Maps.newHashMapWithExpectedSize(2);
        pmpgs.put('s', s);
        pmpgs.put('t', t);

        return new DefaultCFMPS<>('s', pmpgs);
    }

    private static StackSPA<?, Character> buildSPA() {

        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('F', 'G');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final CompactDFA<Character> pF = new CompactDFA<>(alphabet.getProceduralAlphabet());

        final Integer f0 = pF.addInitialState(true);
        final Integer f1 = pF.addState(true);
        final Integer f2 = pF.addState(true);
        final Integer f3 = pF.addState(false);
        final Integer f4 = pF.addState(false);
        final Integer f5 = pF.addState(true);

        pF.setTransition(f0, (Character) 'a', f1);
        pF.setTransition(f0, (Character) 'b', f2);
        pF.setTransition(f0, (Character) 'G', f5);
        pF.setTransition(f1, (Character) 'F', f3);
        pF.setTransition(f2, (Character) 'F', f4);
        pF.setTransition(f3, (Character) 'a', f5);
        pF.setTransition(f4, (Character) 'b', f5);

        final FastDFA<Character> pG = new FastDFA<>(alphabet.getProceduralAlphabet());

        final FastDFAState g0 = pG.addInitialState(false);
        final FastDFAState g1 = pG.addState(true);
        final FastDFAState g2 = pG.addState(false);
        final FastDFAState g3 = pG.addState(true);

        pG.setTransition(g0, 'c', g1);
        pG.setTransition(g0, 'F', g3);
        pG.setTransition(g1, 'G', g2);
        pG.setTransition(g2, 'c', g3);

        return new StackSPA<>(alphabet, 'F', ImmutableMap.of('F', pF, 'G', pG));
    }

    private static StackSBA<?, Character> buildSBA() {

        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('F', 'G');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final CompactDFA<Character> pF = new CompactDFA<>(alphabet);

        final Integer f0 = pF.addInitialState(true);
        final Integer f1 = pF.addState(true);
        final Integer f2 = pF.addState(true);
        final Integer f3 = pF.addState(true);
        final Integer f4 = pF.addState(true);
        final Integer f5 = pF.addState(true);
        final Integer f6 = pF.addState(true);

        pF.setTransition(f0, (Character) 'a', f1);
        pF.setTransition(f0, (Character) 'b', f2);
        pF.setTransition(f0, (Character) 'G', f5);
        pF.setTransition(f0, (Character) 'R', f6);
        pF.setTransition(f1, (Character) 'F', f3);
        pF.setTransition(f1, (Character) 'R', f6);
        pF.setTransition(f2, (Character) 'F', f4);
        pF.setTransition(f2, (Character) 'R', f6);
        pF.setTransition(f3, (Character) 'a', f5);
        pF.setTransition(f4, (Character) 'b', f5);
        pF.setTransition(f5, (Character) 'R', f6);

        final FastDFA<Character> pG = new FastDFA<>(alphabet);

        final FastDFAState g0 = pG.addInitialState(true);
        final FastDFAState g1 = pG.addState(true);
        final FastDFAState g2 = pG.addState(true);
        final FastDFAState g3 = pG.addState(true);
        final FastDFAState g4 = pG.addState(true);

        pG.setTransition(g0, 'c', g1);
        pG.setTransition(g0, 'F', g3);
        pG.setTransition(g1, 'G', g2);
        pG.setTransition(g1, 'R', g4);
        pG.setTransition(g2, 'c', g3);
        pG.setTransition(g3, 'R', g4);

        return new StackSBA<>(alphabet, 'F', ImmutableMap.of('F', pF, 'G', pG));
    }

    private static StackSPMM<?, Character, ?, Character> buildSPMM() {

        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');
        final Alphabet<Character> callAlphabet = Alphabets.characters('F', 'G');
        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final CompactMealy<Character, Character> pF = new CompactMealy<>(alphabet);

        final Integer f0 = pF.addInitialState();
        final Integer f1 = pF.addState();
        final Integer f2 = pF.addState();
        final Integer f3 = pF.addState();
        final Integer f4 = pF.addState();
        final Integer f5 = pF.addState();
        final Integer f6 = pF.addState();

        pF.setTransition(f0, (Character) 'a', f1, (Character) 'x');
        pF.setTransition(f0, (Character) 'b', f2, (Character) 'y');
        pF.setTransition(f0, (Character) 'G', f5, (Character) '+');
        pF.setTransition(f0, (Character) 'R', f6, (Character) '-');
        pF.setTransition(f1, (Character) 'F', f3, (Character) '+');
        pF.setTransition(f1, (Character) 'R', f6, (Character) '-');
        pF.setTransition(f2, (Character) 'F', f4, (Character) '+');
        pF.setTransition(f2, (Character) 'R', f6, (Character) '-');
        pF.setTransition(f3, (Character) 'a', f5, (Character) 'x');
        pF.setTransition(f4, (Character) 'b', f5, (Character) 'y');
        pF.setTransition(f5, (Character) 'R', f6, (Character) '-');

        final FastMealy<Character, Character> pG = new FastMealy<>(alphabet);

        final FastMealyState<Character> g0 = pG.addInitialState();
        final FastMealyState<Character> g1 = pG.addState();
        final FastMealyState<Character> g2 = pG.addState();
        final FastMealyState<Character> g3 = pG.addState();
        final FastMealyState<Character> g4 = pG.addState();

        pG.setTransition(g0, 'c', g1, 'z');
        pG.setTransition(g0, 'F', g3, '+');
        pG.setTransition(g1, 'G', g2, '+');
        pG.setTransition(g1, 'R', g4, '-');
        pG.setTransition(g2, 'c', g3, 'z');
        pG.setTransition(g3, 'R', g4, '-');

        return new StackSPMM<>(alphabet, 'F', '+', '-', ImmutableMap.of('F', pF, 'G', pG));
    }
}
