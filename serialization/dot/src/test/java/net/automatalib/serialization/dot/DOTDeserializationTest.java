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
package net.automatalib.serialization.dot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.common.util.io.UnclosableInputStream;
import net.automatalib.exception.FormatException;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.graph.concept.NodeIDs;
import net.automatalib.graph.impl.CompactPMPG;
import net.automatalib.graph.impl.DefaultCFMPS;
import net.automatalib.serialization.InputModelData;
import net.automatalib.ts.modal.impl.CompactMTS;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty.ProceduralType;
import net.automatalib.ts.modal.transition.impl.ProceduralModalEdgePropertyImpl;
import net.automatalib.visualization.VisualizationHelper.NodeStyles;
import net.automatalib.visualization.VisualizationHelper.PMPGEdgeAttrs;
import net.automatalib.visualization.VisualizationHelper.PMPGNodeAttrs;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DOTDeserializationTest {

    @Test
    public void testRegularDFADeserialization() throws IOException, FormatException {

        final CompactDFA<String> dfa = DOTSerializationUtil.DFA;

        final DFA<?, String> parsed =
                DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.DFA_RESOURCE)).model;

        checkIsomorphism(dfa, parsed, dfa.getInputAlphabet());
    }

    @Test
    public void testRegularNFADeserialization() throws IOException, FormatException {

        final CompactNFA<String> nfa = DOTSerializationUtil.NFA;

        final CompactNFA<String> parsed =
                DOTParsers.nfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA_RESOURCE)).model;

        checkIsomorphism(nfa, parsed, nfa.getInputAlphabet());
    }

    @Test
    public void testRegularNFA2Deserialization() throws IOException, FormatException {

        final CompactNFA<String> parsed = DOTParsers.fsa(new CompactNFA.Creator<>(),
                                                         DOTParsers.DEFAULT_FSA_NODE_PARSER,
                                                         DOTParsers.DEFAULT_EDGE_PARSER,
                                                         Arrays.asList("s0", "s1", "s2"),
                                                         false)
                                                    .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA2_RESOURCE)).model;
        assertNFAProperties(parsed);
    }

    @Test
    public void testRegularNFA3Deserialization() throws IOException, FormatException {

        final CompactNFA<String> parsed = DOTParsers.fsa(new CompactNFA.Creator<>(),
                                                         DOTParsers.DEFAULT_FSA_NODE_PARSER,
                                                         DOTParsers.DEFAULT_EDGE_PARSER,
                                                         GraphDOT.INITIAL_LABEL)
                                                    .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.NFA3_RESOURCE)).model;
        assertNFAProperties(parsed);
    }

    @Test
    public void testRegularMealyDeserialization() throws IOException, FormatException {

        final CompactMealy<String, String> mealy = DOTSerializationUtil.MEALY;

        final MealyMachine<?, String, ?, String> parsed = DOTParsers.mealy()
                                                                    .readModel(DOTSerializationUtil.getResource(
                                                                            DOTSerializationUtil.MEALY_RESOURCE)).model;

        checkIsomorphism(mealy, parsed, mealy.getInputAlphabet());
    }

    @Test
    public void testRegularMooreDeserialization() throws IOException, FormatException {
        final CompactMoore<String, String> moore = DOTSerializationUtil.MOORE;

        final MooreMachine<?, String, ?, String> parsed = DOTParsers.moore()
                                                                    .readModel(DOTSerializationUtil.getResource(
                                                                            DOTSerializationUtil.MOORE_RESOURCE)).model;

        checkIsomorphism(moore, parsed, moore.getInputAlphabet());
    }

    @Test
    public void testRegularGraphDeserialization() throws IOException, FormatException {
        final UniversalGraph<?, ?, String, String> graph = DOTSerializationUtil.GRAPH;

        final UniversalGraph<?, ?, String, String> parsed =
                DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.GRAPH_RESOURCE));

        checkGraphEquivalence(graph, parsed);
    }

    @Test
    public void testRegularMTSDeserialization() throws IOException, FormatException {
        final CompactMTS<String> mts = DOTSerializationUtil.MTS;

        InputModelData<String, CompactMTS<String>> model =
                DOTParsers.mts().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MTS_RESOURCE));

        final Alphabet<String> alphabet = model.alphabet;
        final CompactMTS<String> parsed = model.model;

        checkIsomorphism(mts, parsed, alphabet);
    }

    @Test
    public void testInitialPrefixMTSDeserialization() throws IOException, FormatException {
        final CompactMTS<String> mts = DOTSerializationUtil.MTS;

        InputModelData<String, CompactMTS<String>> model = DOTParsers.mts(CompactMTS::new,
                                                                          DOTParsers.DEFAULT_EDGE_PARSER,
                                                                          DOTParsers.DEFAULT_MTS_EDGE_PARSER,
                                                                          GraphDOT.INITIAL_LABEL)
                                                                     .readModel(DOTSerializationUtil.getResource(
                                                                             DOTSerializationUtil.MTS_RESOURCE));

        final Alphabet<String> alphabet = model.alphabet;
        final CompactMTS<String> parsed = model.model;

        checkIsomorphism(mts, parsed, alphabet);
    }

    @Test
    public void testRegularMMLTDeserialization() throws IOException, FormatException {
        final CompactMMLT<String, String> mts = DOTSerializationUtil.MMLT;

        var model = DOTParsers.mmlt("void", StringSymbolCombiner.getInstance())
                              .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MMLT_RESOURCE));

        var alphabet = model.alphabet;
        var parsed = model.model;

        checkIsomorphism(mts, parsed, alphabet);
    }

    @Test
    public void testMMLTCombinedOutputs() throws IOException, FormatException {
        var mmlt = DOTParsers.mmlt("void", StringSymbolCombiner.getInstance())
                             .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MMLT_MULTI_OUTPUTS)).model;

        var timers = mmlt.getSortedTimers(0);
        Assert.assertEquals(timers.size(), 1);
        Assert.assertEquals(timers.get(0).outputs(), List.of("X", "Y", "Z"));
    }

    @Test
    public void testMMLTSensorModel() throws IOException, FormatException {
        var mmlt = DOTParsers.mmlt("void", StringSymbolCombiner.getInstance())
                             .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.MMLT_SENSOR)).model;

        // Compare to reference:
        var p1 = "p1";
        var p2 = "p2";
        var abort = "abort";
        var collect = "collect";

        int s0 = 0;
        int s1 = 1;
        int s2 = 2;
        int s3 = 3;

        // Check non-delaying transitions:
        assertSilentLoop(mmlt, s0, abort);
        assertSilentLoop(mmlt, s0, collect);
        assertTransition(mmlt, s0, s1, p1, "go");
        assertTransition(mmlt, s0, s2, p2, "go");

        assertTransition(mmlt, s1, s1, abort, "ok");
        Assert.assertTrue(mmlt.isLocalReset(s1, abort));
        assertSilentLoop(mmlt, s1, p1);
        assertSilentLoop(mmlt, s1, p2);
        assertSilentLoop(mmlt, s1, collect);

        assertTransition(mmlt, s2, s3, abort, "void");
        assertSilentLoop(mmlt, s2, p1);
        assertSilentLoop(mmlt, s2, p2);
        assertSilentLoop(mmlt, s2, collect);

        assertSilentLoop(mmlt, s3, abort);
        assertSilentLoop(mmlt, s3, p1);
        assertSilentLoop(mmlt, s3, p2);
        assertTransition(mmlt, s3, s0, collect, "void");

        // Check timers:
        Assert.assertTrue(mmlt.getSortedTimers(s0).isEmpty());
        Assert.assertTrue(mmlt.getSortedTimers(s3).isEmpty());
        Assert.assertEquals(mmlt.getSortedTimers(s1).size(), 3);
        Assert.assertEquals(mmlt.getSortedTimers(s2).size(), 1);

        var firstTimerS1 = mmlt.getSortedTimers(s1).get(0);
        Assert.assertEquals(firstTimerS1.initial(), 3);
        Assert.assertEquals(firstTimerS1.outputs(), List.of("part"));
        Assert.assertTrue(firstTimerS1.periodic());

        var secondTimerS1 = mmlt.getSortedTimers(s1).get(1);
        Assert.assertEquals(secondTimerS1.initial(), 6);
        Assert.assertEquals(secondTimerS1.outputs(), List.of("noise"));
        Assert.assertTrue(secondTimerS1.periodic());

        var thirdTimerS1 = mmlt.getSortedTimers(s1).get(2);
        Assert.assertEquals(thirdTimerS1.initial(), 40);
        Assert.assertEquals(thirdTimerS1.outputs(), List.of("done"));
        Assert.assertFalse(thirdTimerS1.periodic());

        var firstTimerS2 = mmlt.getSortedTimers(s2).get(0);
        Assert.assertEquals(firstTimerS2.initial(), 4);
        Assert.assertEquals(firstTimerS2.outputs(), List.of("done"));
        Assert.assertFalse(firstTimerS2.periodic());
    }

    @Test
    public void testMMLTValidation() {
        var parser = DOTParsers.mmlt(alph -> new CompactMMLT<>(alph, "void", StringSymbolCombiner.getInstance()),
                                     Function.identity(),
                                     s -> StringSymbolCombiner.getInstance().separateSymbols(s),
                                     "s0",
                                     false);

        for (int i = 1; i <= 11; i++) {
            final int id = i;
            Assert.assertThrows(Integer.toString(id),
                                FormatException.class,
                                () -> parser.readModel(DOTSerializationUtil.getResource("/mmlt_error" + id + ".dot")));
        }
    }

    @Test
    public void testCFMPSDeserialization() throws IOException, FormatException {

        final DefaultCFMPS<Character, Character> cfmps = DOTSerializationUtil.CFMPS;

        Function<Map<String, String>, Set<Character>> apParser =
                attr -> DOTCFMPSParser.parseLabelAsProperties(attr, s -> s.charAt(0));
        Function<Map<String, String>, Character> labelParser =
                attr -> attr.getOrDefault(PMPGEdgeAttrs.LABEL, "?").charAt(0);
        Function<Map<String, String>, Character> procedureParser =
                attr -> attr.getOrDefault(PMPGNodeAttrs.PROCESS, "?").charAt(0);

        var model = DOTParsers.cfmps('?', apParser, labelParser, procedureParser)
                              .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.CFMPS_RESOURCE));

        Assert.assertEquals(model.getMainProcess(), cfmps.getMainProcess());
        Assert.assertEquals(model.getPMPGs().keySet(), cfmps.getPMPGs().keySet());

        for (var e : model.getPMPGs().entrySet()) {
            var actual = e.getValue();
            var expected = cfmps.getPMPGs().get(e.getKey());
            checkGraphEquivalence(expected, actual, (s1, s2) -> 0);
            Assert.assertEquals(actual.getInitialNode(), expected.getInitialNode());
            Assert.assertEquals(actual.getFinalNode(), expected.getFinalNode());
        }
    }

    @Test
    public void testCFMPSCustomDeserialization() throws IOException, FormatException {

        var model = DOTParsers.cfmps(label -> new CompactPMPG<>("?"),
                                     attr -> Collections.singleton(Integer.parseInt(attr.getOrDefault(PMPGNodeAttrs.LABEL,
                                                                                                      "")
                                                                                        .split(" ")[1])),
                                     attr -> attr.get(PMPGEdgeAttrs.LABEL),
                                     attr -> attr.getOrDefault(PMPGNodeAttrs.LABEL, "").split(" ")[0],
                                     attr -> new ProceduralModalEdgePropertyImpl(NodeStyles.BOLD.equals(attr.get(
                                             PMPGEdgeAttrs.STYLE)) ? ProceduralType.PROCESS : ProceduralType.INTERNAL,
                                                                                 ModalType.MUST),
                                     "init",
                                     false)
                              .readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.CFMPS2_RESOURCE));

        Assert.assertEquals(model.getMainProcess(), "F");
        Assert.assertEquals(model.getPMPGs().keySet(), Set.of("F", "G"));

        assertPMPGForF(model.getPMPGs().get("F"));
        assertPMPGForG(model.getPMPGs().get("G"));
    }

    @Test
    public void testCFMPSValidation() {

        var parser = DOTParsers.cfmps();

        for (int i = 1; i <= 3; i++) {
            final int id = i;
            Assert.assertThrows(Integer.toString(id),
                                FormatException.class,
                                () -> parser.readModel(DOTSerializationUtil.getResource("/cfmps_error" + id + ".dot")));
        }
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyAutomatonDeserialization() throws IOException, FormatException {
        DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_AUTOMATON_RESOURCE));
    }

    @Test(expectedExceptions = FormatException.class)
    public void testFaultyGraphDeserialization() throws IOException, FormatException {
        DOTParsers.graph().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.FAULTY_GRAPH_RESOURCE));
    }

    @Test
    public void doNotCloseInputStreamTest() throws IOException, FormatException {
        try (InputStream dfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.DFA_RESOURCE);
             InputStream graph = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.GRAPH_RESOURCE);
             InputStream mealy = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MEALY_RESOURCE);
             InputStream moore = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MOORE_RESOURCE);
             InputStream mmlt = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MMLT_RESOURCE);
             InputStream cfmps = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.CFMPS_RESOURCE);
             InputStream mts = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.MTS_RESOURCE);
             InputStream nfa = DOTSerializationUtil.class.getResourceAsStream(DOTSerializationUtil.NFA_RESOURCE)) {
            DOTParsers.dfa().readModel(new UnclosableInputStream(dfa));
            DOTParsers.graph().readModel(new UnclosableInputStream(graph));
            DOTParsers.mealy().readModel(new UnclosableInputStream(mealy));
            DOTParsers.moore().readModel(new UnclosableInputStream(moore));
            DOTParsers.mmlt("void", StringSymbolCombiner.getInstance()).readModel(new UnclosableInputStream(mmlt));
            DOTParsers.cfmps().readModel(new UnclosableInputStream(cfmps));
            DOTParsers.mts().readModel(new UnclosableInputStream(mts));
            DOTParsers.nfa().readModel(new UnclosableInputStream(nfa));
        }
    }

    @Test
    public void testDuplicateTransitions() throws IOException, FormatException {

        final CompactDFA<String> dfa = new CompactDFA<>(Alphabets.closedCharStringRange('a', 'b'));
        final Integer s0 = dfa.addInitialState();
        final Integer s1 = dfa.addState(true);

        dfa.addTransition(s0, "a", s1);
        dfa.addTransition(s0, "b", s1);
        dfa.addTransition(s1, "a", s0);
        dfa.addTransition(s1, "b", s0);

        final StringWriter w = new StringWriter();
        GraphDOT.write(dfa, dfa.getInputAlphabet(), w);

        final ByteArrayInputStream bais = new ByteArrayInputStream(w.toString().getBytes());
        final DFA<?, String> parsed = DOTParsers.dfa().readModel(bais).model;

        checkIsomorphism(dfa, parsed, dfa.getInputAlphabet());
    }

    @Test
    public void testIDs() throws IOException, FormatException {
        final DFA<?, String> parsed =
                DOTParsers.dfa().readModel(DOTSerializationUtil.getResource(DOTSerializationUtil.ID_RESOURCE)).model;

        Assert.assertEquals(parsed.size(), 6);
        Assert.assertTrue(parsed.accepts(Word.fromString("abcd").transform(Objects::toString)));
    }

    private static <S1, S2, I, T1, T2, SP, TP> void checkIsomorphism(UniversalAutomaton<S1, I, T1, SP, TP> source,
                                                                     UniversalAutomaton<S2, I, T2, SP, TP> target,
                                                                     Alphabet<I> alphabet) {

        Assert.assertEquals(source.size(), target.size());

        final Queue<S1> sourceQueue = new ArrayDeque<>(source.getInitialStates());
        final Queue<S2> targetQueue = new ArrayDeque<>(target.getInitialStates());

        Assert.assertEquals(sourceQueue.size(), 1, "This check currently only works for single initial states");
        Assert.assertEquals(targetQueue.size(), 1, "This check currently only works for single initial states");

        final Set<S1> sourceCache = new HashSet<>();
        final Set<S2> targetCache = new HashSet<>();

        while (!sourceQueue.isEmpty() && !targetQueue.isEmpty()) {
            S1 sourceState = sourceQueue.remove();
            S2 targetState = targetQueue.remove();

            Assert.assertEquals(source.getStateProperty(sourceState), target.getStateProperty(targetState));

            sourceCache.add(sourceState);
            targetCache.add(targetState);

            for (I i : alphabet) {
                final List<T1> sourceTransitions = new ArrayList<>(source.getTransitions(sourceState, i));
                final List<T2> targetTransitions = new ArrayList<>(target.getTransitions(targetState, i));

                Assert.assertEquals(sourceTransitions.size(), targetTransitions.size());

                for (int j = 0; j < sourceTransitions.size(); j++) {
                    final T1 sourceTrans = sourceTransitions.get(j);
                    final T2 targetTrans = targetTransitions.get(j);

                    Assert.assertEquals(source.getTransitionProperty(sourceTrans),
                                        target.getTransitionProperty(targetTrans));

                    final S1 sourceSucc = source.getSuccessor(sourceTrans);
                    final S2 targetSucc = target.getSuccessor(targetTrans);

                    Assert.assertEquals(sourceCache.contains(sourceSucc), targetCache.contains(targetSucc));

                    if (!sourceCache.contains(sourceSucc)) {
                        sourceQueue.add(sourceSucc);
                        sourceCache.add(sourceSucc);
                        targetQueue.add(targetSucc);
                        targetCache.add(targetSucc);
                    }
                }
            }
        }

        Assert.assertEquals(sourceQueue.isEmpty(), targetQueue.isEmpty());
    }

    private static <N1, E1, NP extends Comparable<NP>, EP, N2, E2> void checkGraphEquivalence(UniversalGraph<N1, E1, NP, EP> source,
                                                                                              UniversalGraph<N2, E2, NP, EP> target) {
        checkGraphEquivalence(source, target, Comparator.naturalOrder());
    }

    private static <N1, E1, NP, EP, N2, E2> void checkGraphEquivalence(UniversalGraph<N1, E1, ? extends NP, ? extends EP> source,
                                                                       UniversalGraph<N2, E2, ? extends NP, ? extends EP> target,
                                                                       Comparator<NP> comparator) {

        Assert.assertEquals(source.size(), target.size());

        final Queue<N1> sourceQueue = new ArrayDeque<>();
        final Queue<N2> targetQueue = new ArrayDeque<>();

        // since the node ids are parsed natural order, the first nodes are equal
        sourceQueue.add(source.nodeIDs().getNode(0));
        targetQueue.add(target.nodeIDs().getNode(0));

        while (!sourceQueue.isEmpty() && !targetQueue.isEmpty()) {
            N1 sourceNode = sourceQueue.remove();
            N2 targetNode = targetQueue.remove();

            Assert.assertEquals(source.getNodeProperty(sourceNode), target.getNodeProperty(targetNode));

            final List<E1> sourceEdges = new ArrayList<>(source.getOutgoingEdges(sourceNode));
            final List<E2> targetEdges = new ArrayList<>(target.getOutgoingEdges(targetNode));

            Assert.assertEquals(sourceEdges.size(), targetEdges.size());

            // since we have unique node properties, these uniquely identify states
            sourceEdges.sort(Comparator.comparing(e -> source.getNodeProperty(source.getTarget(e)), comparator));
            targetEdges.sort(Comparator.comparing(e -> target.getNodeProperty(target.getTarget(e)), comparator));

            for (int j = 0; j < sourceEdges.size(); j++) {
                final E1 sourceEdge = sourceEdges.get(j);
                final E2 targetEdge = targetEdges.get(j);

                Assert.assertEquals(source.getEdgeProperty(sourceEdge), target.getEdgeProperty(targetEdge));
                Assert.assertEquals(source.getNodeProperty(source.getTarget(sourceEdge)),
                                    target.getNodeProperty(target.getTarget(targetEdge)));
            }
        }

        Assert.assertEquals(sourceQueue.isEmpty(), targetQueue.isEmpty());
    }

    private <T> void assertTransition(MMLT<Integer, String, T, String> model,
                                      int state,
                                      int target,
                                      String input,
                                      String output) {
        var trans = model.getTransition(state, input);
        Assert.assertNotNull(trans);

        if (model.getSuccessor(trans) != target || !model.getTransitionProperty(trans).equals(output)) {
            throw new AssertionError();
        }
    }

    private <T> void assertSilentLoop(MMLT<Integer, String, T, String> model, int state, String input) {
        var trans = model.getTransition(state, input);
        if (trans != null &&
            (model.getSuccessor(trans) != state || !"void".equals(model.getTransitionProperty(trans)))) {
            throw new AssertionError();
        }
        Assert.assertFalse(model.isLocalReset(state, input));
    }

    private static void assertNFAProperties(CompactNFA<String> parsed) {
        Assert.assertEquals(parsed.size(), 3);
        Assert.assertEquals(parsed.getInitialStates().size(), 3);
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("a", "a", "a")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("b", "b")));
        Assert.assertFalse(parsed.accepts(Word.fromSymbols("c")));
        Assert.assertEquals(parsed.getStates(Word.fromSymbols("a", "a", "a")).size(), 1);
        Assert.assertEquals(parsed.getStates(Word.fromSymbols("b", "b")).size(), 1);
        Assert.assertEquals(parsed.getStates(Word.fromLetter("c")).size(), 1);
        Assert.assertTrue(parsed.getStates(Word.fromSymbols("a", "b")).isEmpty());
        Assert.assertTrue(parsed.getStates(Word.fromSymbols("c", "a")).isEmpty());
        Assert.assertTrue(parsed.getStates(Word.fromSymbols("b", "c")).isEmpty());
    }

    private static <N, E, TP extends ProceduralModalEdgeProperty> void assertPMPGForF(ProceduralModalProcessGraph<N, String, E, Integer, TP> f) {
        Assert.assertEquals(f.size(), 8);

        final NodeIDs<N> nodeIDs = f.nodeIDs();
        final N initialNode = f.getInitialNode();
        final N finalNode = f.getFinalNode();

        for (N n : f.getNodes()) {
            Assert.assertEquals(Objects.equals(n, initialNode),
                                f.getNodeProperty(n).equals(Collections.singleton(0)),
                                Objects.toString(n));
            Assert.assertEquals(Objects.equals(n, finalNode),
                                f.getNodeProperty(n).equals(Collections.singleton(1)),
                                Objects.toString(n));
        }

        Assert.assertFalse(f.isConnected(nodeIDs.getNode(0), nodeIDs.getNode(1)));
        Assert.assertTrue(f.isConnected(nodeIDs.getNode(0), nodeIDs.getNode(2)));
        Assert.assertTrue(f.isConnected(nodeIDs.getNode(2), nodeIDs.getNode(1)));

        Collection<E> succs = f.getOutgoingEdges(nodeIDs.getNode(2));
        Assert.assertEquals(succs.size(), 4);

        for (E e : succs) {
            Assert.assertEquals(f.getEdgeLabel(e).equals("G"),
                                f.getEdgeProperty(e).getProceduralType() == ProceduralType.PROCESS);
            Assert.assertEquals(f.getEdgeLabel(e).equals("G"),
                                f.getNodeProperty(f.getTarget(e)).equals(Collections.singleton(3)));
            Assert.assertEquals(f.getEdgeLabel(e).equals("a"),
                                f.getNodeProperty(f.getTarget(e)).equals(Collections.singleton(4)));
            Assert.assertEquals(f.getEdgeLabel(e).equals("b"),
                                f.getNodeProperty(f.getTarget(e)).equals(Collections.singleton(5)));
            Assert.assertEquals(f.getEdgeLabel(e).equals("R"),
                                f.getNodeProperty(f.getTarget(e)).equals(Collections.singleton(1)));
        }
    }

    private static <N, E, TP extends ProceduralModalEdgeProperty> void assertPMPGForG(ProceduralModalProcessGraph<N, String, E, Integer, TP> g) {
        Assert.assertEquals(g.size(), 6);

        final NodeIDs<N> nodeIDs = g.nodeIDs();
        final N initialNode = g.getInitialNode();
        final N finalNode = g.getFinalNode();

        for (N n : g.getNodes()) {
            Assert.assertEquals(Objects.equals(n, initialNode),
                                g.getNodeProperty(n).equals(Collections.singleton(8)),
                                Objects.toString(n));
            Assert.assertEquals(Objects.equals(n, finalNode),
                                g.getNodeProperty(n).equals(Collections.singleton(9)),
                                Objects.toString(n));
        }

        // id = label - 8
        Assert.assertFalse(g.isConnected(nodeIDs.getNode(0), nodeIDs.getNode(1)));
        Assert.assertTrue(g.isConnected(nodeIDs.getNode(0), nodeIDs.getNode(2)));
        Assert.assertTrue(g.isConnected(nodeIDs.getNode(2), nodeIDs.getNode(3)));

        Collection<E> succs = g.getOutgoingEdges(nodeIDs.getNode(2));
        Assert.assertEquals(succs.size(), 2);

        for (E e : succs) {
            Assert.assertEquals(g.getEdgeLabel(e).equals("F"),
                                g.getEdgeProperty(e).getProceduralType() == ProceduralType.PROCESS);
            Assert.assertEquals(g.getEdgeLabel(e).equals("F"),
                                g.getNodeProperty(g.getTarget(e)).equals(Collections.singleton(11)));
            Assert.assertEquals(g.getEdgeLabel(e).equals("?"),
                                g.getNodeProperty(g.getTarget(e)).equals(Collections.singleton(12)));
        }
    }
}
