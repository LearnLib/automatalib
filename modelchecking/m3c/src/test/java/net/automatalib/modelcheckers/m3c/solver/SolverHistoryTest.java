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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformerSerializer;
import net.automatalib.modelcheckers.m3c.util.Examples;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class SolverHistoryTest {

    private final ModalContextFreeProcessSystem<String, String> mcfps;
    private final BDDManager bddManager;
    private final BDDTransformerSerializer<String, String> transformerSerializer;

    public SolverHistoryTest() {
        this.mcfps = Examples.getMcfpsAnBn(Collections.emptySet());
        this.bddManager = new BDDManager();
        this.transformerSerializer = new BDDTransformerSerializer<>(this.bddManager);
    }

    @AfterClass
    public void after() {
        bddManager.quit();
    }

    @Test
    public void testSolverHistory() throws ParseException {
        final SolveBDD<String, String> solver = new SolveBDD<>(mcfps);
        final FormulaNode<String, String> formula = M3CParser.parse("mu X.(<b><b>true || <>X)");
        final SolverHistory<BDDTransformer<String, String>, String, String> history =
                solver.solveAndRecordHistory(formula);
        final Map<String, SolverData<?, BDDTransformer<String, String>, String, String>> data = history.getData();

        Assert.assertEquals(data.size(), 1);

        final SolverData<?, BDDTransformer<String, String>, String, String> solverData = data.get("P");

        Assert.assertNotNull(solverData);
        testSolverHistory(history, solverData);
    }

    private <N> void testSolverHistory(SolverHistory<BDDTransformer<String, String>, String, String> history,
                                       SolverData<N, BDDTransformer<String, String>, String, String> data) {
        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final N initialNode = mpg.getInitialNode();
        final N s1 = getS1(mpg);
        final N s2 = getS2(mpg, s1);

        testNodeIDs(data);
        testInitialPropertyTransformers(data, s1, s2);
        testInitialSatisifedSubformulas(data, s1, s2);
        testMustTransformers(history);
        testSolverStates(history, mpg, initialNode, s1, s2);

        Assert.assertTrue(history.getMayTransformers(transformerSerializer).isEmpty());
        Assert.assertTrue(history.isSat());
    }

    private <N> void testNodeIDs(SolverData<N, BDDTransformer<String, String>, String, String> data) {

        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final NodeIDs<N> nodeIDs = data.getNodeIDs();

        // check that nodeIDs contains a mapping for each node in mpg
        for (final N node : mpg.getNodes()) {
            nodeIDs.getNodeId(node);
        }
    }

    private <N> void testInitialPropertyTransformers(SolverData<N, BDDTransformer<String, String>, String, String> data,
                                                     N s1,
                                                     N s2) {

        final ModalProcessGraph<N, String, ?, String, ?> mpg = data.getMpg();
        final Mapping<N, BDDTransformer<String, String>> initialPropertyTransformers =
                data.getInitialPropertyTransformers(transformerSerializer);

        final BDDTransformer<String, String> startPT = initialPropertyTransformers.get(mpg.getInitialNode());
        final BDDTransformer<String, String> endPT = initialPropertyTransformers.get(mpg.getFinalNode());
        final BDDTransformer<String, String> s1PT = initialPropertyTransformers.get(s1);
        final BDDTransformer<String, String> s2PT = initialPropertyTransformers.get(s2);

        // the PTs of start, s1 and s2 are initialized with an array of zero BDDs
        Assert.assertEquals(startPT, s1PT);
        Assert.assertEquals(startPT, s2PT);
        Assert.assertEquals(s1PT, s2PT);
        // the end state PT is initialized with the identity function
        Assert.assertNotEquals(startPT, endPT);

        final BDD zeroDD = bddManager.readLogicZero();
        for (int i = 0; i < startPT.getNumberOfVars(); i++) {
            Assert.assertEquals(startPT.getBDD(i), zeroDD);
        }
        for (int i = 0; i < endPT.getNumberOfVars(); i++) {
            BDD ithDD = endPT.getBDD(i);
            Assert.assertEquals(ithDD, bddManager.ithVar(i));
        }
    }

    private <N> void testInitialSatisifedSubformulas(SolverData<N, BDDTransformer<String, String>, String, String> data,
                                                     N s1,
                                                     N s2) {

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

    private void testMustTransformers(SolverHistory<BDDTransformer<String, String>, String, String> history) {
        final Map<String, BDDTransformer<String, String>> mustTransformers =
                history.getMustTransformers(transformerSerializer);
        Assert.assertEquals(mustTransformers.size(), 3);
        final BDDTransformer<String, String> aDDs = mustTransformers.get("a");
        final BDDTransformer<String, String> bDDs = mustTransformers.get("b");
        final BDDTransformer<String, String> eDDs = mustTransformers.get("e");
        Assert.assertEquals(aDDs, eDDs);
        Assert.assertNotEquals(aDDs, bDDs);

        for (int i = 0; i < aDDs.getNumberOfVars(); i++) {
            final BDD aDD = aDDs.getBDD(i);
            if (i == 4) {
                Assert.assertEquals(aDD, bddManager.ithVar(0));
            } else {
                Assert.assertEquals(aDD, bddManager.readLogicZero());
            }
        }

        for (int i = 0; i < bDDs.getNumberOfVars(); i++) {
            final BDD bDD = bDDs.getBDD(i);
            if (i == 1 || i == 2) {
                Assert.assertEquals(bDD, bddManager.ithVar(i + 1));
            } else if (i == 4) {
                Assert.assertEquals(bDD, bddManager.ithVar(0));
            } else {
                Assert.assertEquals(bDD, bddManager.readLogicZero());
            }
        }
    }

    private <N> void testSolverStates(SolverHistory<BDDTransformer<String, String>, String, String> history,
                                      ModalProcessGraph<N, String, ?, String, ?> mpg,
                                      N initialNode,
                                      N s1,
                                      N s2) {
        final List<N> updatedOrder =
                Arrays.asList(initialNode, s1, initialNode, s2, s1, initialNode, s1, initialNode, s1, initialNode, s1);
        Assert.assertEquals(history.getSolverStates().size(), updatedOrder.size());
        List<Set<N>> workSets = getWorkSetHistory(initialNode, s1, s2);
        boolean[] allAPDeadlockedState = new boolean[5];
        allAPDeadlockedState[3] = true;
        for (int i = 0; i < updatedOrder.size(); i++) {
            final N expectedState = updatedOrder.get(i);
            final SolverState<?, BDDTransformer<String, String>, String, String> solverState =
                    history.getSolverStates().get(i);
            @SuppressWarnings("unchecked")
            final N actualState = (N) solverState.getUpdatedState();
            Assert.assertEquals(actualState, expectedState);
            Assert.assertEquals(mpg.getOutgoingEdges(actualState).size(),
                                solverState.getCompositions(transformerSerializer).size());
            Assert.assertEquals(solverState.getUpdatedStateMPG(), mcfps.getMainProcess());
            Assert.assertEquals(solverState.getWorkSet().get(mcfps.getMainProcess()), workSets.get(i));
            testSatisfiedSubformulasAndUpdatedPT(allAPDeadlockedState, solverState);
        }

    }

    private void testSatisfiedSubformulasAndUpdatedPT(boolean[] allAPDeadlockedState,
                                                      SolverState<?, BDDTransformer<String, String>, String, String> solverState) {
        final BDDTransformer<String, String> updatedPropertyTransformer =
                solverState.getUpdatedPropTransformer(transformerSerializer);
        final Set<Integer> expectedSatisfiedSubformulas = new HashSet<>();

        for (int i = 0; i < updatedPropertyTransformer.getNumberOfVars(); i++) {
            final BDD transformerBDD = updatedPropertyTransformer.getBDD(i);
            if (transformerBDD.eval(allAPDeadlockedState).equals(bddManager.readOne())) {
                expectedSatisfiedSubformulas.add(i);
            }
        }

        final Set<Integer> actualSatisfiedSubformulas = solverState.getUpdatedStateSatisfiedSubformula()
                                                                   .stream()
                                                                   .map(FormulaNode::getVarNumber)
                                                                   .collect(Collectors.toSet());
        Assert.assertEquals(actualSatisfiedSubformulas, expectedSatisfiedSubformulas);
    }

    private <N> List<Set<N>> getWorkSetHistory(N initialNode, N s1, N s2) {
        final Set<N> workSetOnlyS1 = Collections.singleton(s1);
        final Set<N> workSetOnlyInitialNode = Collections.singleton(initialNode);

        return Arrays.asList(new HashSet<>(Arrays.asList(s1, s2)),
                             new HashSet<>(Arrays.asList(initialNode, s2)),
                             Collections.singleton(s2),
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             workSetOnlyInitialNode,
                             workSetOnlyS1,
                             Collections.emptySet());
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
