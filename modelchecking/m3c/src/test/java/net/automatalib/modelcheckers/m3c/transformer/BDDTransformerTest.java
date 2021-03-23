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

import java.util.ArrayList;
import java.util.List;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserMuCalc;
import net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType;
import net.automatalib.ts.modal.transition.ModalEdgePropertyImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BDDTransformerTest {

    private static DependencyGraph<String, String> dg;
    private static BDDManager bddManager;
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
        ast = dg.getAST();
        bddManager = new BDDManager();
        orNode = (OrNode<String, String>) ast.getLeftChild();
        diaNode1 = (DiamondNode<String, String>) orNode.getLeftChild();
        diaNode2 = (DiamondNode<String, String>) orNode.getRightChild();
        boxNode = (BoxNode<String, String>) diaNode1.getLeftChild();
        trueNode = (TrueNode<String, String>) boxNode.getLeftChild();
    }

    @Test
    void testBDDIdentity() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg.getNumVariables());
        for (int var = 0; var < transformer.getBdds().length; var++) {
            Assert.assertEquals(transformer.getBdds()[var], bddManager.ithVar(var));
        }
    }

    @Test
    void testBDDStateInitialization() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg);
        for (EquationalBlock<String, String> block : dg.getBlocks()) {
            for (FormulaNode<String, String> node : block.getNodes()) {
                BDD actual = transformer.getBdds()[node.getVarNumber()];
                BDD expected;
                if (block.isMaxBlock()) {
                    expected = bddManager.readOne();
                } else {
                    expected = bddManager.readLogicZero();
                }
                Assert.assertEquals(expected, actual);
            }
        }
    }

    @Test
    void testEdgeTransformerMust() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);

        BDD bddOrNode = transformer.getBdds()[orNode.getVarNumber()];
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBdds()[diaNode1.getVarNumber()];
        BDD expectedBDDDiaNode1 = bddManager.ithVar(diaNode1.getVarNumberLeft());
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBdds()[diaNode2.getVarNumber()];
        BDD expectedBDDDiaNode2 = bddManager.ithVar(diaNode2.getVarNumberLeft());
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBdds()[boxNode.getVarNumber()];
        BDD expectedBDDBoxNode = bddManager.ithVar(boxNode.getVarNumberLeft());
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBdds()[trueNode.getVarNumber()];
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testEdgeTransformerNoMatch() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "a", new ModalEdgePropertyImpl(ModalType.MUST), dg);

        BDD bddOrNode = transformer.getBdds()[orNode.getVarNumber()];
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBdds()[diaNode1.getVarNumber()];
        BDD expectedBDDDiaNode1 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBdds()[diaNode2.getVarNumber()];
        BDD expectedBDDDiaNode2 = bddManager.ithVar(diaNode2.getVarNumberLeft());
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBdds()[boxNode.getVarNumber()];
        BDD expectedBDDBoxNode = bddManager.readOne();
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBdds()[trueNode.getVarNumber()];
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testEdgeTransformerMay() {
        BDDTransformer<String, String> transformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MAY), dg);

        BDD bddOrNode = transformer.getBdds()[orNode.getVarNumber()];
        BDD expectedBDDOrNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDOrNode, bddOrNode);

        BDD bddDiaNode1 = transformer.getBdds()[diaNode1.getVarNumber()];
        BDD expectedBDDDiaNode1 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode1, bddDiaNode1);

        BDD bddDiaNode2 = transformer.getBdds()[diaNode2.getVarNumber()];
        BDD expectedBDDDiaNode2 = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDDiaNode2, bddDiaNode2);

        BDD bddBoxNode = transformer.getBdds()[boxNode.getVarNumber()];
        BDD expectedBDDBoxNode = bddManager.ithVar(boxNode.getVarNumberLeft());
        Assert.assertEquals(expectedBDDBoxNode, bddBoxNode);

        BDD bddTrueNode = transformer.getBdds()[trueNode.getVarNumber()];
        BDD expectedBDDTrueNode = bddManager.readLogicZero();
        Assert.assertEquals(expectedBDDTrueNode, bddTrueNode);
    }

    @Test
    void testComposition() {
        BDDTransformer<String, String> transformer = new BDDTransformer<>(bddManager, dg);
        BDDTransformer<String, String> identity = new BDDTransformer<>(bddManager, dg.getNumVariables());
        BDDTransformer<String, String> composition = transformer.compose(identity);
        Assert.assertEquals(5, composition.getBdds().length);
        Assert.assertEquals(transformer, composition);

        BDDTransformer<String, String> inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

    @Test
    void testOrBDDListOnes() {
        BDDTransformer<String, String> edgeTransformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);

        BDDTransformer<String, String> oneTransformer = new BDDTransformer<>(bddManager);
        oneTransformer.setIsMust(true);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readOne();
        }
        oneTransformer.setBDDs(oneBDDs);

        List<BDDTransformer<String, String>> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);
        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.readOne(), disjunction);
    }

    @Test
    void testOrBDDListZeros() {
        BDDTransformer<String, String> edgeTransformer =
                new BDDTransformer<>(bddManager, "b", new ModalEdgePropertyImpl(ModalType.MUST), dg);
        BDDTransformer<String, String> oneTransformer = new BDDTransformer<>(bddManager);
        oneTransformer.setIsMust(true);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readLogicZero();
        }
        oneTransformer.setBDDs(oneBDDs);

        List<BDDTransformer<String, String>> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);
        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.ithVar(diaNode1.getVarNumberLeft()), disjunction);
    }

}
