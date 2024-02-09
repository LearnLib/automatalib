/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.formula.parser;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.exception.FormatException;
import net.automatalib.modelchecker.m3c.formula.AndNode;
import net.automatalib.modelchecker.m3c.formula.AtomicNode;
import net.automatalib.modelchecker.m3c.formula.BoxNode;
import net.automatalib.modelchecker.m3c.formula.DiamondNode;
import net.automatalib.modelchecker.m3c.formula.FalseNode;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.NotNode;
import net.automatalib.modelchecker.m3c.formula.OrNode;
import net.automatalib.modelchecker.m3c.formula.TrueNode;
import net.automatalib.modelchecker.m3c.formula.ctl.AFNode;
import net.automatalib.modelchecker.m3c.formula.ctl.AGNode;
import net.automatalib.modelchecker.m3c.formula.ctl.AUNode;
import net.automatalib.modelchecker.m3c.formula.ctl.AWUNode;
import net.automatalib.modelchecker.m3c.formula.ctl.EFNode;
import net.automatalib.modelchecker.m3c.formula.ctl.EGNode;
import net.automatalib.modelchecker.m3c.formula.ctl.EUNode;
import net.automatalib.modelchecker.m3c.formula.ctl.EWUNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParserCTLTest {

    private final List<FormulaNode<String, String>> formulas = new ArrayList<>();

    @Test
    public void baseCasesTest() throws FormatException {
        assertEquals("false", new FalseNode<>());
        assertEquals("true", new TrueNode<>());
        assertEquals("true && true", new AndNode<>(new TrueNode<>(), new TrueNode<>()));
        assertEquals("true || false", new OrNode<>(new TrueNode<>(), new FalseNode<>()));
        assertEquals("'a'", new AtomicNode<>("a"));
        assertEquals("\"b\"", new AtomicNode<>("b"));
        assertEquals("\"a\" && \"b\" && \"c\"",
                     new AndNode<>(new AtomicNode<>("a"), new AndNode<>(new AtomicNode<>("b"), new AtomicNode<>("c"))));
        assertEquals("AG true", new AGNode<>(new TrueNode<>()));
        assertEquals("AF true", new AFNode<>(new TrueNode<>()));
        assertEquals("A(true U false)", new AUNode<>(new TrueNode<>(), new FalseNode<>()));
        assertEquals("A(true W true)", new AWUNode<>(new TrueNode<>(), new TrueNode<>()));
        assertEquals("EG true", new EGNode<>(new TrueNode<>()));
        assertEquals("EF true", new EFNode<>(new TrueNode<>()));
        assertEquals("E(false U false)", new EUNode<>(new FalseNode<>(), new FalseNode<>()));
        assertEquals("E(true W false)", new EWUNode<>(new TrueNode<>(), new FalseNode<>()));
        assertEquals("true -> false", new OrNode<>(new NotNode<>(new TrueNode<>()), new FalseNode<>()));
        assertEquals("true <-> false",
                     new AndNode<>(new OrNode<>(new NotNode<>(new TrueNode<>()), new FalseNode<>()),
                                   new OrNode<>(new NotNode<>(new FalseNode<>()), new TrueNode<>())));
        assertEquals("!false", new NotNode<>(new FalseNode<>()));
        assertEquals("[]true", new BoxNode<>(new TrueNode<>()));
        assertEquals("<>false", new DiamondNode<>(new FalseNode<>()));
        assertEquals("<a>false", new DiamondNode<>("a", new FalseNode<>()));
        assertEquals("[c]true", new BoxNode<>("c", new TrueNode<>()));
    }

    @Test
    public void tokensAsActionsTest() throws FormatException {
        assertEquals("<A>true", new DiamondNode<>("A", new TrueNode<>()));
        assertEquals("<AF>true", new DiamondNode<>("AF", new TrueNode<>()));
        assertEquals("<AG>true", new DiamondNode<>("AG", new TrueNode<>()));
        assertEquals("<E>true", new DiamondNode<>("E", new TrueNode<>()));
        assertEquals("<EF>true", new DiamondNode<>("EF", new TrueNode<>()));
        assertEquals("<EG>true", new DiamondNode<>("EG", new TrueNode<>()));
        assertEquals("<U>true", new DiamondNode<>("U", new TrueNode<>()));
        assertEquals("<W>true", new DiamondNode<>("W", new TrueNode<>()));
        assertEquals("[A]true", new BoxNode<>("A", new TrueNode<>()));
        assertEquals("[AF]true", new BoxNode<>("AF", new TrueNode<>()));
        assertEquals("[AG]true", new BoxNode<>("AG", new TrueNode<>()));
        assertEquals("[E]true", new BoxNode<>("E", new TrueNode<>()));
        assertEquals("[EF]true", new BoxNode<>("EF", new TrueNode<>()));
        assertEquals("[EG]true", new BoxNode<>("EG", new TrueNode<>()));
        assertEquals("[U]true", new BoxNode<>("U", new TrueNode<>()));
        assertEquals("[W]true", new BoxNode<>("W", new TrueNode<>()));
    }

    @Test
    public void nestedFormulasTest() throws FormatException {
        assertEquals("(true && true) || (false && false)",
                     new OrNode<>(new AndNode<>(new TrueNode<>(), new TrueNode<>()),
                                  new AndNode<>(new FalseNode<>(), new FalseNode<>())));
    }

    @Test(dependsOnMethods = {"baseCasesTest", "tokensAsActionsTest", "nestedFormulasTest"})
    public void testEqualities() {
        for (FormulaNode<String, String> n1 : formulas) {
            for (FormulaNode<String, String> n2 : formulas) {
                Assert.assertEquals(n1.equals(n2), n1 == n2, "n1: " + n1 + ", n2: " + n2);
            }
        }
    }

    @Test
    public void testIllegalFormulas() {
        assertIllegal("true <=> false -> true");
        assertIllegal("true <=> (false -> true)");
        assertIllegal("AG (mu X.(<>X))");
        assertIllegal("(AF true) && (mu X.(<b> true || <>X)");
        assertIllegal("(mu X.(<b> true || <>X) || (AF true)");
    }

    private void assertEquals(String ctlFormula, FormulaNode<String, String> expectedAST) throws FormatException {
        FormulaNode<String, String> actualAST = M3CParser.parse(ctlFormula);
        Assert.assertEquals(actualAST, expectedAST);
        Assert.assertEquals(actualAST.hashCode(), expectedAST.hashCode());

        this.formulas.add(actualAST);
    }

    private void assertIllegal(String formula) {
        Assert.assertThrows(FormatException.class, () -> M3CParser.parse(formula));
    }

}
