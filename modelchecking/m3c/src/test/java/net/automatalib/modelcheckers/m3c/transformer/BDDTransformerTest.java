package net.automatalib.modelcheckers.m3c.transformer;

import java.util.ArrayList;
import java.util.List;

import info.scce.addlib.dd.bdd.BDD;
import info.scce.addlib.dd.bdd.BDDManager;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

class BDDTransformerTest {

    private static DependencyGraph dg;
    private static BDDManager bddManager;
    private static OrNode orNode;
    private static DiamondNode diaNode1;
    private static DiamondNode diaNode2;
    private static BoxNode boxNode;
    private static TrueNode trueNode;

    @BeforeClass
    public static void setup() throws ParseException {
        String formula = "mu X.(<b>[b]true || <>X)";
        FormulaNode ast = ParserMuCalc.parse(formula);
        dg = new DependencyGraph(ast);
        bddManager = new BDDManager();
        orNode = (OrNode) ast.getLeftChild();
        diaNode1 = (DiamondNode) orNode.getLeftChild();
        diaNode2 = (DiamondNode) orNode.getRightChild();
        boxNode = (BoxNode) diaNode1.getLeftChild();
        trueNode = (TrueNode) boxNode.getLeftChild();
    }

    @Test
    void testBDDIdentity() {
        BDDTransformer transformer = new BDDTransformer(bddManager, dg.getNumVariables());
        for (int var = 0; var < transformer.getBdds().length; var++) {
            Assert.assertEquals(transformer.getBdds()[var], bddManager.ithVar(var));
        }
    }

    @Test
    void testBDDStateInitialization() {
        BDDTransformer transformer = new BDDTransformer(bddManager, dg);
        for (EquationalBlock block : dg.getBlocks()) {
            for (FormulaNode node : block.getNodes()) {
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
        Edge edge = new Edge(null, null, "b", EdgeType.MUST);
        BDDTransformer transformer = new BDDTransformer(bddManager, edge, dg);

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
        Edge edge = new Edge(null, null, "a", EdgeType.MUST);
        BDDTransformer transformer = new BDDTransformer(bddManager, edge, dg);

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
        Edge edge = new Edge(null, null, "b", EdgeType.MAY);
        BDDTransformer transformer = new BDDTransformer(bddManager, edge, dg);

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
        BDDTransformer transformer = new BDDTransformer(bddManager, dg);
        BDDTransformer identity = new BDDTransformer(bddManager, dg.getNumVariables());
        BDDTransformer composition = transformer.compose(identity);
        Assert.assertEquals(5, composition.getBdds().length);
        Assert.assertEquals(transformer, composition);

        BDDTransformer inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

    @Test
    void testOrBDDListOnes() {
        Edge edge = new Edge(null, null, "b", EdgeType.MUST);
        BDDTransformer edgeTransformer = new BDDTransformer(bddManager, edge, dg);

        BDDTransformer oneTransformer = new BDDTransformer(bddManager);
        oneTransformer.setIsMust(true);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readOne();
        }
        oneTransformer.setBDDs(oneBDDs);

        List<BDDTransformer> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);
        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.readOne(), disjunction);
    }

    @Test
    void testOrBDDListZeros() {
        Edge edge = new Edge(null, null, "b", EdgeType.MUST);
        BDDTransformer edgeTransformer = new BDDTransformer(bddManager, edge, dg);

        BDDTransformer oneTransformer = new BDDTransformer(bddManager);
        oneTransformer.setIsMust(true);
        BDD[] oneBDDs = new BDD[dg.getNumVariables()];
        for (int var = 0; var < oneBDDs.length; var++) {
            oneBDDs[var] = bddManager.readLogicZero();
        }
        oneTransformer.setBDDs(oneBDDs);

        List<BDDTransformer> comps = new ArrayList<>();
        comps.add(edgeTransformer);
        comps.add(oneTransformer);
        BDD disjunction = edgeTransformer.orBddList(comps, diaNode1.getVarNumber());
        Assert.assertEquals(bddManager.ithVar(diaNode1.getVarNumberLeft()), disjunction);
    }
}
