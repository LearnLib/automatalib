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
 * @author murtovi
 */
public class ParserCTLTest {

    @Test
    public void baseCasesTest() {
        assertEquals("false", new FalseNode<>());
        assertEquals("true", new TrueNode<>());
        assertEquals("true && true", new AndNode<>(new TrueNode<>(), new TrueNode<>()));
        assertEquals("true || false", new OrNode<>(new TrueNode<>(), new FalseNode<>()));
        assertEquals("'a'", new AtomicNode<>("a"));
        assertEquals("\"a\"", new AtomicNode<>("a"));
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

    private void assertEquals(String ctlFormula, FormulaNode<String, String> expectedAST) {
        try {
            FormulaNode<String, String> actualAST = M3CParser.parse(ctlFormula);
            Assert.assertEquals(actualAST, expectedAST);
        } catch (ParseException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nestedFormulasTest() {
        assertEquals("(true && true) || (false && false)",
                     new OrNode<>(new AndNode<>(new TrueNode<>(), new TrueNode<>()),
                                  new AndNode<>(new FalseNode<>(), new FalseNode<>())));
    }

}
