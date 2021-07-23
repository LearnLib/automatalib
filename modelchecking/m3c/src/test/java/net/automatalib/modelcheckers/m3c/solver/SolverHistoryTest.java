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

import com.google.common.collect.Iterators;
import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanLogicDDManager;
import info.scce.addlib.serializer.XDDSerializer;
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
        final ModalProcessGraph<?, String, ?, String, ?> mpg = mcfps.getMPGs().get(mcfps.getMainProcess());
        testSolverHistory(history, mpg);
    }

    private <N> void testSolverHistory(SolverHistory<String, String> history,
                                       ModalProcessGraph<N, String, ?, String, ?> mpg) {
        final N initialNode = mpg.getInitialNode();
        final N s1 = getS1(mpg);
        final N s2 = getS2(mpg, s1);

        testNodeIDs(history, mpg);
        testInitialPropertyTransformers(history, mpg, s1, s2);
        testInitialSatisifedSubformulas(history, mpg, s1, s2);
        testMustTransformers(history);
        Assert.assertTrue(history.getMayTransformers().isEmpty());
        testSolverStates(history, mpg, initialNode, s1, s2);
        Assert.assertEquals(SolverHistory.DDType.BDD, history.getDDType());
        Assert.assertTrue(history.isSat());
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
            final SolverState<N, String, String> solverState =
                    (SolverState<N, String, String>) history.getSolverStates().get(i);
            final N actualState = solverState.getUpdatedState();
            Assert.assertEquals(actualState, expectedState);
            Assert.assertEquals(Iterators.size(mpg.getOutgoingEdges(actualState).iterator()),
                                solverState.getCompositions().size());
            Assert.assertEquals(solverState.getUpdatedStateMPG(), mcfps.getMainProcess());
        }
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

    private <N> void testInitialSatisifedSubformulas(SolverHistory<String, String> history,
                                                     ModalProcessGraph<N, String, ?, String, ?> mpg,
                                                     N s1,
                                                     N s2) {
        final Map<String, Map<?, List<FormulaNode<String, String>>>> initialSatisfiedSubformulas =
                history.getInitialSatisfiedSubformulas();
        Assert.assertEquals(initialSatisfiedSubformulas.size(), 1);
        final Map<N, List<FormulaNode<String, String>>> mpgSatisfiedSubformulas =
                (Map<N, List<FormulaNode<String, String>>>) initialSatisfiedSubformulas.get(mcfps.getMainProcess());
        Assert.assertTrue(mpgSatisfiedSubformulas.get(mpg.getInitialNode()).isEmpty());
        Assert.assertTrue(mpgSatisfiedSubformulas.get(s1).isEmpty());
        Assert.assertTrue(mpgSatisfiedSubformulas.get(s2).isEmpty());
        final List<FormulaNode<String, String>> finalNodeSatisfiedSubformulas =
                mpgSatisfiedSubformulas.get(mpg.getFinalNode());
        Assert.assertEquals(finalNodeSatisfiedSubformulas.size(), 1);
        Assert.assertTrue(finalNodeSatisfiedSubformulas.get(0) instanceof TrueNode);
    }

    private <N> void testInitialPropertyTransformers(SolverHistory<String, String> history,
                                                     ModalProcessGraph<N, String, ?, String, ?> mpg,
                                                     N s1,
                                                     N s2) {
        final Map<String, Map<?, List<String>>> initialPropertyTransformers = history.getInitialPropertyTransformers();
        Assert.assertEquals(initialPropertyTransformers.size(), 1);
        final Map<N, List<String>> mpgInitialPropertyTransformers =
                (Map<N, List<String>>) initialPropertyTransformers.get(mcfps.getMainProcess());
        Assert.assertEquals(mpgInitialPropertyTransformers.size(), 4);
        final List<XDD<Boolean>> startPT = getDDs(mpgInitialPropertyTransformers.get(mpg.getInitialNode()));
        final List<XDD<Boolean>> endPT = getDDs(mpgInitialPropertyTransformers.get(mpg.getFinalNode()));
        final List<XDD<Boolean>> s1PT = getDDs(mpgInitialPropertyTransformers.get(s1));
        final List<XDD<Boolean>> s2PT = getDDs(mpgInitialPropertyTransformers.get(s2));

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
            if (mpg.getEdgeLabel(edge).equals("a")) {
                return mpg.getTarget(edge);
            }
        }
        return null;
    }

    private <N, E> N getS2(ModalProcessGraph<N, String, E, String, ?> mpg, N s1) {
        // s1's only adjacent target is s2
        for (final N adjacentTarget : mpg.getAdjacentTargets(s1)) {
            return adjacentTarget;
        }
        return null;
    }

    private <N> void testNodeIDs(SolverHistory<String, String> history,
                                 ModalProcessGraph<N, String, ?, String, ?> mpg) {
        final Map<String, NodeIDs<?>> nodeIDs = history.getNodeIDs();
        Assert.assertEquals(nodeIDs.size(), 1);
        final NodeIDs<N> mpgNodeIDs = (NodeIDs<N>) nodeIDs.get(mcfps.getMainProcess());

        // check that mpgNodeIDs contains a mapping for each node in mpg
        for (final N node : mpg.getNodes()) {
            mpgNodeIDs.getNodeId(node);
        }
    }

}
