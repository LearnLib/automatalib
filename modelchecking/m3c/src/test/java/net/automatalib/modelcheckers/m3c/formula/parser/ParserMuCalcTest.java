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
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParserMuCalcTest {

    @Test
    public void baseCasesTest() {
        assertEquals("false", new FalseNode());
        assertEquals("true", new TrueNode());
        assertEquals("true && true", new AndNode(new TrueNode(), new TrueNode()));
        assertEquals("true || false", new OrNode(new TrueNode(), new FalseNode()));
        assertEquals("'a'", new AtomicNode("a"));
        assertEquals("\"a\"", new AtomicNode("a"));
        assertEquals("true -> false", new OrNode(new NotNode(new TrueNode()), new FalseNode()));
        assertEquals("true <-> false",
                     new AndNode(new OrNode(new NotNode(new TrueNode()), new FalseNode()),
                                 new OrNode(new NotNode(new FalseNode()), new TrueNode())));
        assertEquals("!false", new NotNode(new FalseNode()));
        assertEquals("[]true", new BoxNode("", new TrueNode()));
        assertEquals("<>false", new DiamondNode("", new FalseNode()));
        assertEquals("<ab>false", new DiamondNode("ab", new FalseNode()));
        assertEquals("[ab]true", new BoxNode("ab", new TrueNode()));
        assertEquals("mu XY.(XY || false)", new LfpNode("XY", new OrNode(new VariableNode("XY"), new FalseNode())));
        assertEquals("nu ZY.(ZY || false)", new GfpNode("ZY", new OrNode(new VariableNode("ZY"), new FalseNode())));
    }

    private void assertEquals(String muCalcFormula, FormulaNode expectedAST) {
        try {
            FormulaNode actualAST = ParserMuCalc.parse(muCalcFormula);
            Assert.assertEquals(actualAST, expectedAST);
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
