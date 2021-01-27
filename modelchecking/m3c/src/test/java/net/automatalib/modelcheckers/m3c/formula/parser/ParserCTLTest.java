package net.automatalib.modelcheckers.m3c.formula.parser;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AWUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EWUNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * author murtovi
 */
public class ParserCTLTest {

    @Test
    public void baseCasesTest() {
        assertEquals("false", new FalseNode());
        assertEquals("true", new TrueNode());
        assertEquals("true && true", new AndNode(new TrueNode(), new TrueNode()));
        assertEquals("true || false", new OrNode(new TrueNode(), new FalseNode()));
        assertEquals("'a'", new AtomicNode("a"));
        assertEquals("\"a\"", new AtomicNode("a"));
        assertEquals("AG true", new AGNode(new TrueNode()));
        assertEquals("AF true", new AFNode(new TrueNode()));
        assertEquals("A(true U false)", new AUNode(new TrueNode(), new FalseNode()));
        assertEquals("A(true W true)", new AWUNode(new TrueNode(), new TrueNode()));
        assertEquals("EG true", new EGNode(new TrueNode()));
        assertEquals("EF true", new EFNode(new TrueNode()));
        assertEquals("E(false U false)", new EUNode(new FalseNode(), new FalseNode()));
        assertEquals("E(true W false)", new EWUNode(new TrueNode(), new FalseNode()));
        assertEquals("true -> false", new OrNode(new NotNode(new TrueNode()), new FalseNode()));
        assertEquals("true <-> false",
                     new AndNode(new OrNode(new NotNode(new TrueNode()), new FalseNode()),
                                 new OrNode(new NotNode(new FalseNode()), new TrueNode())));
        assertEquals("!false", new NotNode(new FalseNode()));
        assertEquals("[]true", new BoxNode("", new TrueNode()));
        assertEquals("<>false", new DiamondNode("", new FalseNode()));
        assertEquals("<a>false", new DiamondNode("a", new FalseNode()));
        assertEquals("[c]true", new BoxNode("c", new TrueNode()));
    }

    private void assertEquals(String ctlFormula, FormulaNode expectedAST) {
        try {
            FormulaNode actualAST = ParserCTL.parse(ctlFormula);
            Assert.assertEquals(actualAST, expectedAST);
        } catch (net.automatalib.modelcheckers.m3c.formula.parser.ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nestedFormulasTest() {
        assertEquals("(true && true) || (false && false)",
                     new OrNode(new AndNode(new TrueNode(), new TrueNode()),
                                new AndNode(new FalseNode(), new FalseNode())));
    }

}
