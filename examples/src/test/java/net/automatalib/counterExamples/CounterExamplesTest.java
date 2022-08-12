/* Copyright (C) 2013-2022 TU Dortmund
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
package net.automatalib.counterExamples;

import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.counterExamples.SuperSolver.GraphGenerator.GraphGenerator;
import net.automatalib.counterExamples.SuperSolver.MagicTree;
import net.automatalib.counterExamples.SuperSolver.SuperSolver;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolvers;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Run the examples as part of (integration) testing.
 *
 * @author frohme
 */
public class CounterExamplesTest {

    static Logger logger = LoggerFactory.getLogger(CounterExamplesTest.class);
    GraphGenerator g = new GraphGenerator(false);


    public CounterExamplesTest() throws IOException, ParserConfigurationException, SAXException {
    }

    @Test
    public void checkSimpleFormulasOnP() throws IOException, ParserConfigurationException, SAXException, ParseException {
        checkJVMCompatibility();


        ArrayList<String> formulas = new ArrayList<>();
        formulas.add("mu X.((<b>true) || <>X)");
        //formulas.add("mu X.((<b><b><b><b><b><b><b><b><b><b><b><b><b><b><b><b>true) || <>X)"); //check for multiple b paths
        formulas.add("mu X.((<c>true) || <>X || (<b><b>true))"); //check for shorter c path
        formulas.add("mu X.((<c><c>true) || <>X || (<b><b>true))");
        formulas.add("mu X.(mu Y.(<a><a>true) || <>X || (<b><b>true))"); //check with multiple formulae
        formulas.add("mu X.((<a>true) || <>X )"); //check for a single a path
        formulas.add("true || false");
        formulas.add("false || false|| false|| false|| false|| false|| false|| true");
        DefaultCFMPS<String, Void> cfmps = g.cfmpsList.get(2);
        M3CSolver.TypedM3CSolver<FormulaNode<String, Void>> m3c = M3CSolvers.typedSolver(cfmps);
        SuperSolver<String, Void> solver = new SuperSolver<>(cfmps);



        ArrayList<MagicTree> resultTrees = new ArrayList<>();
        for (String fGoal: formulas) {
            FormulaNode<String, Void> formula = M3CParser.parse(fGoal, l -> l,
                    ap -> null);
            m3c.solve(formula); //keeps context open on each iteration, otherwise crash
            SuperSolver x = new SuperSolver(cfmps);
            MagicTree tree = x.computeWitness(cfmps, formula);
            System.out.println("M3C time: " + x.calcFulfilledTime/1000000+"ms");
            System.out.println("Tree time: " + x.calcTreeTime/1000000+"ms");
            System.out.println("Tree size: " + tree.size());
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            resultTrees.add(tree);
        }


        Assert.assertEquals(resultTrees.get(0).resultString.length(), 3);
        //Assert.assertEquals(resultTrees.get(1).resultString.length(), 33);
        Assert.assertEquals(resultTrees.get(1).resultString.length(), 1);
        Assert.assertEquals(resultTrees.get(2).resultString.length(), 5);
        Assert.assertEquals(resultTrees.get(3).resultString.length(), 5);
        Assert.assertEquals(resultTrees.get(4).resultString.length(), 1);

    }

    @Test
    public void checkCFMPSSetting1System1Fulfilled() throws IOException, ParserConfigurationException, SAXException {
        checkJVMCompatibility();

    }

    @Test
    public void checkPalindromeSeed() throws ParseException {

        ArrayList<String> formulas = new ArrayList<>();
        formulas.add("mu X.(<>X || <b><S><b>true)");
        formulas.add("<S><T><c>true");
        formulas.add("<S><a><S><T><c>true");
        formulas.add("mu X. (<>X || [] false)");
        formulas.add("mu X.((<a>true) || <>X )");
        DefaultCFMPS<String, Void> cfmps = g.cfmpsList.get(4);
        M3CSolver.TypedM3CSolver<FormulaNode<String, Void>> m3c = M3CSolvers.typedSolver(cfmps);
        SuperSolver<String, Void> solver = new SuperSolver<>(cfmps);



        ArrayList<MagicTree> resultTrees = new ArrayList<>();
        for (String fGoal: formulas) {
            FormulaNode<String, Void> formula = M3CParser.parse(fGoal, l -> l,
                    ap -> null);
            System.out.println("formula fulfilled? ->"+ m3c.solve(formula)); //keeps context open on each iteration, otherwise crash
            SuperSolver x = new SuperSolver(cfmps);
            MagicTree tree = x.computeWitness(cfmps, formula);
            System.out.println("M3C time: " + x.calcFulfilledTime/1000000+"ms");
            resultTrees.add(tree);
        }


        /*Assert.assertEquals(resultTrees.get(0).resultString.length(), 3);
        //Assert.assertEquals(resultTrees.get(1).resultString.length(), 33);
        Assert.assertEquals(resultTrees.get(1).resultString.length(), 1);
        Assert.assertEquals(resultTrees.get(2).resultString.length(), 5);
        Assert.assertEquals(resultTrees.get(3).resultString.length(), 5);
        Assert.assertEquals(resultTrees.get(4).resultString.length(), 1);*/

    }

    private static void checkJVMCompatibility() {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <= 8");
        }
    }

}
