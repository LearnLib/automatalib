package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserCTL;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

class CTLToMuCalcTest {

    private static CTLToMuCalc transformer;

    @BeforeClass
    public static void setupParserAndTransformer() {
        transformer = new CTLToMuCalc();
    }

    @Test
    void testTrue() {
        equals("true", new TrueNode());
    }

    private void equals(String inputFormula, FormulaNode expectedResult) {
        FormulaNode ctlNode = null;
        try {
            ctlNode = ParserCTL.parse(inputFormula);
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertEquals(expectedResult, toMuCalc(ctlNode));
    }

    private FormulaNode toMuCalc(FormulaNode ctlNode) {
        return transformer.toMuCalc(ctlNode);
    }

    @Test
    void testFalse() {
        equals("false", new FalseNode());
    }

    @Test
    void testAtomicProposition() {
        equals("\"p\"", new AtomicNode("p"));
    }

    @Test
    void testNegation() {
        equals("!\"p\"", new NotNode(new AtomicNode("p")));
    }

    @Test
    void testBox() {
        equals("[abc]true", new BoxNode("abc", new TrueNode()));
    }

    @Test
    void testDiamond() {
        equals("<>false", new DiamondNode("", new FalseNode()));
    }

    @Test
    void testOr() {
        equals("\"p\" || \"q\"", new OrNode(new AtomicNode("p"), new AtomicNode("q")));
    }

    @Test
    void testAnd() {
        equals("\"p\" && \"q\"", new AndNode(new AtomicNode("p"), new AtomicNode("q")));
    }

    @Test
    void testAF() {
        DiamondNode diamond = new DiamondNode("", new TrueNode());
        BoxNode box = new BoxNode("", new VariableNode("Z0"));
        AndNode and = new AndNode(diamond, box);
        LfpNode expected = new LfpNode("Z0", new OrNode(new AtomicNode("p"), and));
        equals("AF \"p\"", expected);
    }

    @Test
    void testAG() {
        AndNode and = new AndNode(new AtomicNode("p"), new BoxNode("", new VariableNode("Z0")));
        GfpNode expected = new GfpNode("Z0", and);
        equals("AG \"p\"", expected);
    }

    @Test
    void testAU() {
        DiamondNode diamond = new DiamondNode("", new TrueNode());
        BoxNode box = new BoxNode("", new VariableNode("Z0"));
        AndNode innerAnd = new AndNode(diamond, box);
        AndNode outerAnd = new AndNode(new AtomicNode("p"), innerAnd);
        OrNode or = new OrNode(new AtomicNode("q"), outerAnd);
        LfpNode expected = new LfpNode("Z0", or);
        equals("A(\"p\" U \"q\")", expected);
    }

    @Test
    void testAWU() {
        /* A[p WU q] = !E[!q U (!p & !q)] */
        /* !E[!q U (!p & !q)] = !(mu X.((!p & !q) | (!q & <>X))) */
        NotNode notQ = new NotNode(new AtomicNode("q"));
        NotNode notP = new NotNode(new AtomicNode("p"));
        AndNode notPAndNotQ = new AndNode(notP, notQ);
        AndNode innerAnd = new AndNode(notQ, new DiamondNode("", new VariableNode("Z0")));
        OrNode or = new OrNode(notPAndNotQ, innerAnd);
        LfpNode lfpNode = new LfpNode("Z0", or);
        NotNode expected = new NotNode(lfpNode);
        equals("A(\"p\" W \"q\")", expected);
    }

    @Test
    void testEF() {
        OrNode or = new OrNode(new AtomicNode("p"), new DiamondNode("", new VariableNode("Z0")));
        LfpNode expected = new LfpNode("Z0", or);
        equals("EF \"p\"", expected);
    }

    @Test
    void testEG() {
        OrNode or = new OrNode(new DiamondNode("", new VariableNode("Z0")), new BoxNode("", new FalseNode()));
        GfpNode expected = new GfpNode("Z0", new AndNode(new AtomicNode("p"), or));
        equals("EG \"p\"", expected);
    }

    @Test
    void testEU() {
        AndNode and = new AndNode(new AtomicNode("p"), new DiamondNode("", new VariableNode("Z0")));
        LfpNode expected = new LfpNode("Z0", new OrNode(new TrueNode(), and));
        equals("E(\"p\" U true)", expected);
    }

    @Test
    void testEWU() {
        AndNode and = new AndNode(new AtomicNode("p"), new DiamondNode("", new VariableNode("Z0")));
        LfpNode lfp = new LfpNode("Z0", new OrNode(new AtomicNode("q"), and));
        OrNode or = new OrNode(new DiamondNode("", new VariableNode("Z1")), new BoxNode("", new FalseNode()));
        GfpNode gfp = new GfpNode("Z1", new AndNode(new AtomicNode("p"), or));
        equals("E(\"p\" W \"q\")", new OrNode(lfp, gfp));
    }
}
