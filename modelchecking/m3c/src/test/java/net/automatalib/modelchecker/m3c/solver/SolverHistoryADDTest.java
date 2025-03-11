/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.solver;

import java.util.BitSet;
import java.util.Map;

import info.scce.addlib.dd.xdd.XDD;
import info.scce.addlib.dd.xdd.XDDManager;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVector;
import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.modelchecker.m3c.transformer.ADDTransformer;
import net.automatalib.modelchecker.m3c.transformer.ADDTransformerSerializer;
import org.testng.Assert;
import org.testng.SkipException;

public class SolverHistoryADDTest extends AbstractSolverHistoryTest<ADDTransformer<String, String>> {

    private final int numSubformulas;
    private final XDDManager<BooleanVector> xddManager;

    public SolverHistoryADDTest() {
        this.numSubformulas = 5;
        this.xddManager = new BooleanVectorLogicDDManager(numSubformulas);
        this.serializer = new ADDTransformerSerializer<>(xddManager);
    }

    @Override
    public void shutdownDDManager() {
        xddManager.quit();
    }

    @Override
    public AbstractDDSolver<ADDTransformer<String, String>, String, String> getSolver() {
        return new ADDSolver<>(cfmps);
    }

    @Override
    public void testSolverHistory() {
        // TODO this test may fail due to a bug in ADDLib. Remove this method once we bump the dependency
        throw new SkipException("");
    }

    @Override
    public <N> void testInitialPropertyTransformers(SolverData<N, ADDTransformer<String, String>, String, String> data,
                                                    N s1,
                                                    N s2) {

        final ProceduralModalProcessGraph<N, String, ?, String, ?> pmpg = data.getPmpg();
        final Mapping<N, ADDTransformer<String, String>> initialPropertyTransformers =
                data.getInitialPropertyTransformers(serializer);

        final ADDTransformer<String, String> startPT = initialPropertyTransformers.get(pmpg.getInitialNode());
        final ADDTransformer<String, String> endPT = initialPropertyTransformers.get(pmpg.getFinalNode());
        final ADDTransformer<String, String> s1PT = initialPropertyTransformers.get(s1);
        final ADDTransformer<String, String> s2PT = initialPropertyTransformers.get(s2);

        // the PTs of start, s1 and s2 are initialized with an array of zero BDDs
        Assert.assertEquals(startPT, s1PT);
        Assert.assertEquals(startPT, s2PT);
        Assert.assertEquals(s1PT, s2PT);
        XDD<BooleanVector> startDD = startPT.getAdd();
        Assert.assertNotNull(startDD);
        Assert.assertTrue(startDD.isConstant());
        Assert.assertEquals(startDD.v().data(), new boolean[numSubformulas]);

        // the end node PT is initialized with the identity function
        Assert.assertTrue(endPT.isIdentity());
    }

    @Override
    public void testMustTransformers(SolverHistory<ADDTransformer<String, String>, String, String> history) {
        final Map<String, ADDTransformer<String, String>> mustTransformers = history.getMustTransformers(serializer);
        Assert.assertEquals(mustTransformers.size(), 3);
        final ADDTransformer<String, String> aPT = mustTransformers.get("a");
        final ADDTransformer<String, String> bPT = mustTransformers.get("b");
        final ADDTransformer<String, String> ePT = mustTransformers.get("e");
        Assert.assertEquals(aPT, ePT);
        Assert.assertNotEquals(aPT, bPT);

        for (int i = 0; i < Math.pow(2, numSubformulas); i++) {
            String errMsg = "Failure in system: " + i;
            checkATransformer(aPT, errMsg);
            String binaryString = String.format("%05d", Integer.parseInt(Integer.toBinaryString(i)));
            boolean[] input = new boolean[numSubformulas];
            input[0] = binaryString.charAt(0) == '1';
            input[1] = binaryString.charAt(1) == '1';
            input[2] = binaryString.charAt(2) == '1';
            input[3] = binaryString.charAt(3) == '1';
            input[4] = binaryString.charAt(4) == '1';
            BitSet aSatVarNumbers = aPT.evaluate(input);
            boolean[] aResult = new boolean[numSubformulas];
            for (int j = 0; j < numSubformulas; j++) {
                aResult[j] = aSatVarNumbers.get(j);
            }
            Assert.assertEquals(aResult[4], input[0], errMsg);
            Assert.assertFalse(aResult[0], errMsg);
            Assert.assertFalse(aResult[1], errMsg);
            Assert.assertFalse(aResult[2], errMsg);
            Assert.assertFalse(aResult[3], errMsg);

            BitSet bSatVarNumbers = bPT.evaluate(input);
            boolean[] bResult = new boolean[numSubformulas];
            for (int j = 0; j < numSubformulas; j++) {
                bResult[j] = bSatVarNumbers.get(j);
            }
            Assert.assertEquals(bResult[1], input[2], errMsg);
            Assert.assertEquals(bResult[2], input[3], errMsg);
            Assert.assertEquals(bResult[4], input[0], errMsg);
            Assert.assertFalse(bResult[0], errMsg);
            Assert.assertFalse(bResult[3], errMsg);
        }
    }

    private static void checkATransformer(ADDTransformer<String, String> aPT, String errMsg) {
        XDD<BooleanVector> dd = aPT.getAdd();
        Assert.assertNotNull(dd, errMsg);
        Assert.assertEquals(dd.readIndex(), 0, errMsg);
        XDD<BooleanVector> ddT = dd.t();
        XDD<BooleanVector> ddE = dd.e();
        Assert.assertTrue(ddT.isConstant(), errMsg);
        Assert.assertTrue(ddE.isConstant(), errMsg);
        boolean[] expectedTrueBranch = {false, false, false, false, true};
        boolean[] expectedFalseBranch = {false, false, false, false, false};
        Assert.assertEquals(ddT.v().data(), expectedTrueBranch, errMsg);
        Assert.assertEquals(ddE.v().data(), expectedFalseBranch, errMsg);
    }

}
