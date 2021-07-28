/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanLogicDDManager;
import info.scce.addlib.serializer.XDDSerializer;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.util.Examples;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SolverHistoryTest {

    private final ModalContextFreeProcessSystem<String, String> mcfps;
    private final XDDSerializer<Boolean> serializer;
    private final BooleanLogicDDManager ddManager;

    public SolverHistoryTest() {
        this.mcfps = Examples.getMcfpsAnBn(Collections.emptySet());
        this.serializer = new XDDSerializer<>();
        this.ddManager = new BooleanLogicDDManager();
    }

    @Test
    public void testSolverHistory() throws ParseException {
        final SolveBDD<String, String> solver = new SolveBDD<>(mcfps);
        final FormulaNode<String, String> formula = M3CParser.parse("mu X.(<b><b>true || <>X)");
        final SolverHistory<String, String> history = solver.solveAndRecordHistory(formula);
        final Map<String, SolverData<String, ?, String>> data = history.getData();

        Assert.assertEquals(data.size(), 1);

        final SolverData<String, ?, String> solverData = data.get("P");

        Assert.assertNotNull(solverData);
        testSolverHistory(history, solverData);
    }

    private <N> void testSolverHistory(SolverHistory<String, String> history, SolverData<String, N, String> data) {
        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final N initialNode = mpg.getInitialNode();
        final N s1 = getS1(mpg);
        final N s2 = getS2(mpg, s1);

        testNodeIDs(data);
        testInitialPropertyTransformers(data, s1, s2);
        testInitialSatisifedSubformulas(data, s1, s2);
        testMustTransformers(history);
        testSolverStates(history, mpg, initialNode, s1, s2);

        Assert.assertTrue(history.getMayTransformers().isEmpty());
        Assert.assertEquals(SolverHistory.DDType.BDD, history.getDDType());
        Assert.assertTrue(history.isSat());
    }

    private <N> void testNodeIDs(SolverData<String, N, String> data) {

        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final NodeIDs<N> nodeIDs = data.getNodeIDs();

        // check that nodeIDs contains a mapping for each node in mpg
        for (final N node : mpg.getNodes()) {
            nodeIDs.getNodeId(node);
        }
    }

    private <N> void testInitialPropertyTransformers(SolverData<String, N, String> data, N s1, N s2) {

        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final Mapping<N, List<String>> initialPropertyTransformers = data.getInitialPropertyTransformers();

        final List<XDD<Boolean>> startPT = getDDs(initialPropertyTransformers.get(mpg.getInitialNode()));
        final List<XDD<Boolean>> endPT = getDDs(initialPropertyTransformers.get(mpg.getFinalNode()));
        final List<XDD<Boolean>> s1PT = getDDs(initialPropertyTransformers.get(s1));
        final List<XDD<Boolean>> s2PT = getDDs(initialPropertyTransformers.get(s2));

        // the PTs of start, s1 and s2 are initialized with an array of zero BDDs
        Assert.assertEquals(startPT, s1PT);
        Assert.assertEquals(startPT, s2PT);
        Assert.assertEquals(s1PT, s2PT);
        // the end state PT is initialized with the identity function
        Assert.assertNotEquals(startPT, endPT);

        final XDD<Boolean> zeroDD = ddManager.zero();
        for (final XDD<Boolean> startDD : startPT) {
            Assert.assertEquals(startDD, zeroDD);
        }
        for (int i = 0; i < endPT.size(); i++) {
            Assert.assertEquals(endPT.get(i), ddManager.ithVar(i));
        }
    }

    private <N> void testInitialSatisifedSubformulas(SolverData<String, N, String> data, N s1, N s2) {

        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final Mapping<N, List<FormulaNode<String, String>>> initialSatisfiedSubformulas =
                data.getInitialSatisfiedSubformulas();

        Assert.assertTrue(initialSatisfiedSubformulas.get(mpg.getInitialNode()).isEmpty());
        Assert.assertTrue(initialSatisfiedSubformulas.get(s1).isEmpty());
        Assert.assertTrue(initialSatisfiedSubformulas.get(s2).isEmpty());

        final List<FormulaNode<String, String>> finalNodeSatisfiedSubformulas =
                initialSatisfiedSubformulas.get(mpg.getFinalNode());
        Assert.assertEquals(finalNodeSatisfiedSubformulas.size(), 1);
        Assert.assertTrue(finalNodeSatisfiedSubformulas.get(0) instanceof TrueNode);
    }

    private void testMustTransformers(SolverHistory<String, String> history) {
        final Map<String, List<String>> mustTransformers = history.getMustTransformers();
        Assert.assertEquals(mustTransformers.size(), 3);
        final List<XDD<Boolean>> aDDs = getDDs(mustTransformers.get("a"));
        final List<XDD<Boolean>> bDDs = getDDs(mustTransformers.get("b"));
        final List<XDD<Boolean>> eDDs = getDDs(mustTransformers.get("e"));
        Assert.assertEquals(aDDs, eDDs);
        Assert.assertNotEquals(aDDs, bDDs);
        for (int i = 0; i < aDDs.size(); i++) {
            if (i == 4) {
                Assert.assertEquals(aDDs.get(i), ddManager.ithVar(0));
            } else {
                Assert.assertEquals(aDDs.get(i), ddManager.zero());
            }
        }
        for (int i = 0; i < bDDs.size(); i++) {
            if (i == 1 || i == 2) {
                Assert.assertEquals(bDDs.get(i), ddManager.ithVar(i + 1));
            } else if (i == 4) {
                Assert.assertEquals(bDDs.get(i), ddManager.ithVar(0));
            } else {
                Assert.assertEquals(bDDs.get(i), ddManager.zero());
            }
        }
    }

    private <N> void testSolverStates(SolverHistory<String, String> history,
                                      ModalProcessGraph<N, String, ?, String, ?> mpg,
                                      N initialNode,
                                      N s1,
                                      N s2) {
        final List<N> updatedOrder =
                Arrays.asList(initialNode, s1, initialNode, s2, s1, initialNode, s1, initialNode, s1, initialNode, s1);
        Assert.assertEquals(history.getSolverStates().size(), updatedOrder.size());

        for (int i = 0; i < updatedOrder.size(); i++) {
            final N expectedState = updatedOrder.get(i);
            final SolverState<?, String, String> solverState = history.getSolverStates().get(i);
            @SuppressWarnings("unchecked")
            final N actualState = (N) solverState.getUpdatedState();
            Assert.assertEquals(actualState, expectedState);
            Assert.assertEquals(mpg.getOutgoingEdges(actualState).size(), solverState.getCompositions().size());
            Assert.assertEquals(solverState.getUpdatedStateMPG(), mcfps.getMainProcess());
        }
    }

    private List<XDD<Boolean>> getDDs(List<String> serializedDDs) {
        final List<XDD<Boolean>> dds = new ArrayList<>(serializedDDs.size());
        for (final String serializedDD : serializedDDs) {
            dds.add(serializer.deserialize(ddManager, serializedDD));
        }
        return dds;
    }

    private <N, E> N getS1(ModalProcessGraph<N, String, E, String, ?> mpg) {
        final N initalNode = mpg.getInitialNode();
        // the initial node is connected to s1 by an "a" labeled edge
        for (final E edge : mpg.getOutgoingEdges(initalNode)) {
            if ("a".equals(mpg.getEdgeLabel(edge))) {
                return mpg.getTarget(edge);
            }
        }
        throw new IllegalStateException("unexpected modal process graph");
    }

    private <N, E> N getS2(ModalProcessGraph<N, String, E, String, ?> mpg, N s1) {
        // s1's only adjacent target is s2
        for (final N adjacentTarget : mpg.getAdjacentTargets(s1)) {
            return adjacentTarget;
        }
        throw new IllegalStateException("unexpected modal process graph");
    }

}
