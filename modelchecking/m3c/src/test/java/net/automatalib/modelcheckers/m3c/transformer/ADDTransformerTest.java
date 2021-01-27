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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

class ADDTransformerTest {

    private static DependencyGraph dg;
    private static BooleanVectorLogicDDManager xddManager;
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
        xddManager = new BooleanVectorLogicDDManager(dg.getNumVariables());
        orNode = (OrNode) ast.getLeftChild();
        diaNode1 = (DiamondNode) orNode.getLeftChild();
        diaNode2 = (DiamondNode) orNode.getRightChild();
        boxNode = (BoxNode) diaNode1.getLeftChild();
        trueNode = (TrueNode) boxNode.getLeftChild();
    }

    @Test
    void testADDIdentity() {
        ADDTransformer transformer = new ADDTransformer(xddManager, dg.getNumVariables());
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
        ADDTransformer transformer = new ADDTransformer(xddManager, dg);
        Assert.assertTrue(transformer.getAdd().isConstant());
        boolean[] leafData = transformer.getAdd().v().data();
        for (EquationalBlock block : dg.getBlocks()) {
            for (FormulaNode node : block.getNodes()) {
                boolean val = leafData[node.getVarNumber()];
                boolean isMaxBlock = block.isMaxBlock();
                Assert.assertEquals(isMaxBlock, val);
            }
        }
    }

    @Test
    void testEdgeTransformerMust() {
        Edge edge = new Edge(null, null, "b", EdgeType.MUST);
        ADDTransformer transformer = new ADDTransformer(xddManager, edge, dg);
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
        Edge edge = new Edge(null, null, "a", EdgeType.MUST);
        ADDTransformer transformer = new ADDTransformer(xddManager, edge, dg);
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
        ADDTransformer transformer = new ADDTransformer(xddManager, edge, dg);
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
        ADDTransformer transformer = new ADDTransformer(xddManager, dg);
        ADDTransformer identity = new ADDTransformer(xddManager, dg.getNumVariables());
        ADDTransformer composition = transformer.compose(identity);
        Assert.assertEquals(transformer, composition);

        ADDTransformer inverseComposition = identity.compose(transformer);
        Assert.assertEquals(transformer, inverseComposition);
    }

}
