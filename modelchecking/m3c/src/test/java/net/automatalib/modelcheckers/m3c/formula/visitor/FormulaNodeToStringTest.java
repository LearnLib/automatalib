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
package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FormulaNodeToStringTest {

    @Test
    void testBaseCases() {
        testCorrectnessCTL("true");
        testCorrectnessCTL("false");
        testCorrectnessCTL("\"a\"");
        testCorrectnessCTL("AF true");
        testCorrectnessCTL("AG true");
        testCorrectnessCTL("A(false U true)");
        testCorrectnessCTL("A(false W true)");
        testCorrectnessCTL("E(false U true)");
        testCorrectnessCTL("E(false W true)");
        testCorrectnessCTL("true || false");
        testCorrectnessCTL("true && false");

        testCorrectnessMuCalc("mu X.(\"a\" || <>X)");
        testCorrectnessMuCalc("nu X.(\"a\" || <>X)");
    }

    private void testCorrectnessCTL(String ctlFormula) {
        FormulaNode<String, String> ast = null;
        try {
            ast = M3CParser.parse(ctlFormula);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String astToString = ast.toString();
        FormulaNode<String, String> backToAst = null;
        try {
            backToAst = M3CParser.parse(astToString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(ast, backToAst);
    }

    private void testCorrectnessMuCalc(String muCalcFormula) {
        FormulaNode<String, String> ast = null;
        try {
            ast = M3CParser.parse(muCalcFormula);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String astToString = ast.toString();
        FormulaNode<String, String> backToAst = null;
        try {
            backToAst = M3CParser.parse(astToString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(ast, backToAst);
    }

}