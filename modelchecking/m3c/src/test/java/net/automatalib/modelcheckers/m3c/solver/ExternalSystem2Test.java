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
package net.automatalib.modelcheckers.m3c.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import net.automatalib.commons.util.IOUtil;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

//TODO: Merge with ExternalSystemTest once it supports ADD and BDD Backends
public class ExternalSystem2Test {

    private final Function<ModalContextFreeProcessSystem<String, String>, M3CSolver<String>> solverProvider;

    @Factory(dataProvider = "solvers")
    public ExternalSystem2Test(Function<ModalContextFreeProcessSystem<String, String>, M3CSolver<String>> solverProvider) {
        this.solverProvider = solverProvider;
    }

    @DataProvider
    public static Object[] solvers() {
        Function<ModalContextFreeProcessSystem<String, String>, M3CSolver<String>> addSolver = M3CSolvers::addSolver;
        Function<ModalContextFreeProcessSystem<String, String>, M3CSolver<String>> bddSolver = M3CSolvers::bddSolver;
        return new Function<?, ?>[] {addSolver, bddSolver};
    }

    @Test
    public void testPalindrome() throws IOException, ParserConfigurationException, SAXException, ParseException {
        testRERSBenchmark("/mcfps/palindrome/");
    }

    private void testRERSBenchmark(String id)
            throws IOException, ParserConfigurationException, SAXException, ParseException {

        try (InputStream seed = ExternalSystem2Test.class.getResourceAsStream(id + "seed.xml");
             InputStream properties = ExternalSystem2Test.class.getResourceAsStream(id + "properties.txt");
             InputStream solutions = ExternalSystem2Test.class.getResourceAsStream(id + "solutions.txt")) {

            final ModalContextFreeProcessSystem<String, String> mcfps = ExternalSystemDeserializer.parse(seed);
            final M3CSolver<String> solver = this.solverProvider.apply(mcfps);

            try (BufferedReader propertiesReader = new BufferedReader(IOUtil.asUTF8Reader(properties));
                 BufferedReader solutionsReader = new BufferedReader(IOUtil.asUTF8Reader(solutions))) {

                String prop = propertiesReader.readLine();
                String sol = solutionsReader.readLine();

                while (prop != null || sol != null) {
                    Assert.assertNotNull(prop, "Unequal number of properties and solutions");
                    Assert.assertNotNull(sol, "Unequal number of properties and solutions");

                    Assert.assertEquals(solver.solve(prop),
                                        Boolean.parseBoolean(sol),
                                        "Error solving: \"" + prop + "\" with " + solver);

                    prop = propertiesReader.readLine();
                    sol = solutionsReader.readLine();
                }
            }
        }
    }
}
