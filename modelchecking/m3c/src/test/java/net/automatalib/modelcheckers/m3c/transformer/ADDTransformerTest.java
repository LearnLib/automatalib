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
package net.automatalib.modelcheckers.m3c.transformer;

import java.util.BitSet;
import java.util.Set;

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.EdgeType;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserMuCalc;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ADDTransformerTest {

    private static DependencyGraph<String, String> dg;
    private static BooleanVectorLogicDDManager xddManager;
    private static OrNode<String, String> orNode;
    private static DiamondNode<String, String> diaNode1;
    private static DiamondNode<String, String> diaNode2;
    private static BoxNode<String, String> boxNode;
    private static TrueNode<String, String> trueNode;

    @BeforeClass
    public static void setup() throws ParseException {
        String formula = "mu X.(<b>[b]true || <>X)";
        FormulaNode<String, String> ast = ParserMuCalc.parse(formula);
        dg = new DependencyGraph<>(ast);
        xddManager = new BooleanVectorLogicDDManager(dg.getNumVariables());
        orNode = (OrNode<String, String>) ast.getLeftChild();
        diaNode1 = (DiamondNode<String, String>) orNode.getLeftChild();
        diaNode2 = (DiamondNode<String, String>) orNode.getRightChild();
        boxNode = (BoxNode<String, String>) diaNode1.getLeftChild();
        trueNode = (TrueNode<String, String>) boxNode.getLeftChild();
    }

    @Test
    void testADDIdentity() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dg.getNumVariables());
        double numVarCombinations = Math.pow(2, dg.getNumVariables());

        /* Check output of each possible input */
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            /* Test correctness of identity by checking if input equals output */
            Set<Integer> satisfiedVars = transformer.evaluate(input);
            for (int idx = 0; i < input.length; i++) {
                if (input[idx]) {
                    Assert.assertTrue(satisfiedVars.contains(idx));
                }
                if (satisfiedVars.contains(idx)) {
                    Assert.assertTrue(input[idx]);
                }
            }
        }
    }

    @Test
    void testBDDStateInitialization() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dg);
        Assert.assertTrue(transformer.getAdd().isConstant());
        boolean[] leafData = transformer.getAdd().v().data();
        for (EquationalBlock<String, String> block : dg.getBlocks()) {
            for (FormulaNode<String, String> node : block.getNodes()) {
                boolean val = leafData[node.getVarNumber()];
                boolean isMaxBlock = block.isMaxBlock();
                Assert.assertEquals(isMaxBlock, val);
            }
        }
    }

    @Test
    void testEdgeTransformerMust() {
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "b", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());

        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            Set<Integer> satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.contains(orNode.getVarNumber()));

            boolean diaNode1ExpectedTrue = input[diaNode1.getVarNumberLeft()];
            boolean diaNode1ActualTrue = satisfiedVars.contains(diaNode1.getVarNumber());
            Assert.assertEquals(diaNode1ExpectedTrue, diaNode1ActualTrue);

            boolean diaNode2ExpectedTrue = input[diaNode2.getVarNumberLeft()];
            boolean diaNode2ActualTrue = satisfiedVars.contains(diaNode2.getVarNumber());
            Assert.assertEquals(diaNode2ExpectedTrue, diaNode2ActualTrue);

            boolean boxNodeExpectedTrue = input[boxNode.getVarNumberLeft()];
            boolean boxNodeActualTrue = satisfiedVars.contains(boxNode.getVarNumber());
            Assert.assertEquals(boxNodeExpectedTrue, boxNodeActualTrue);

            Assert.assertFalse(satisfiedVars.contains(trueNode.getVarNumber()));
        }
    }

    @Test
    void testEdgeTransformerNoMatch() {
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "a", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MUST), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            Set<Integer> satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.contains(orNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.contains(diaNode1.getVarNumber()));

            boolean diaNode2ExpectedTrue = input[diaNode2.getVarNumberLeft()];
            boolean diaNode2ActualTrue = satisfiedVars.contains(diaNode2.getVarNumber());
            Assert.assertEquals(diaNode2ExpectedTrue, diaNode2ActualTrue);

            Assert.assertTrue(satisfiedVars.contains(boxNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.contains(trueNode.getVarNumber()));
        }
    }

    @Test
    void testEdgeTransformerMay() {
        Edge edge = new Edge(null, null, "b", EdgeType.MAY);
        ADDTransformer<String, String> transformer =
                new ADDTransformer<>(xddManager, "b", new ModalEdgePropertyImpl(ModalEdgeProperty.ModalType.MAY), dg);
        double numVarCombinations = Math.pow(2, dg.getNumVariables());
        for (int i = 0; i < numVarCombinations; i++) {

            /* Construct boolean  input vector from BitSet */
            BitSet bs = BitSet.valueOf(new long[] {i});
            boolean[] input = new boolean[dg.getNumVariables()];
            for (int idx = 0; idx < dg.getNumVariables(); idx++) {
                input[idx] = bs.get(idx);
            }

            Set<Integer> satisfiedVars = transformer.evaluate(input);
            Assert.assertFalse(satisfiedVars.contains(orNode.getVarNumber()));

            Assert.assertFalse(satisfiedVars.contains(diaNode1.getVarNumber()));

            Assert.assertFalse(satisfiedVars.contains(diaNode2.getVarNumber()));

            boolean boxNodeExpectedTrue = input[boxNode.getVarNumberLeft()];
            boolean boxNodeActualTrue = satisfiedVars.contains(boxNode.getVarNumber());
            Assert.assertEquals(boxNodeExpectedTrue, boxNodeActualTrue);

            Assert.assertFalse(satisfiedVars.contains(trueNode.getVarNumber()));
        }
    }

    @Test
    void testComposition() {
        ADDTransformer<String, String> transformer = new ADDTransformer<>(xddManager, dg);
        ADDTransformer<String, String> identity = new ADDTransformer<>(xddManager, dg.getNumVariables());
        ADDTransformer<String, String> composition = transformer.compose(identity);
        Assert.assertEquals(transformer, composition);

        ADDTransformer<String, String> inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

}
