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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelcheckers.m3c.util.Examples;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractSolverTest<T extends AbstractPropertyTransformer<T, String, String>> {

    @Test
    void testSolve() throws ParseException {
        final M3CSolver<String> solver = getSolver(Examples.getMcfpsAnBn(Collections.emptySet()));

        final String formula = "mu X.(<b><b>true || <>X)";
        assertSolve(solver, formula, true);

        final String negatedFormula = "!(" + formula + ")";
        assertSolve(solver, negatedFormula, false);

        // use !'a' to simulate true, as no state satisfies 'a'
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    @Test
    void testSolveWithSingleAP() throws ParseException {
        final ModalContextFreeProcessSystem<String, String> mcfps = Examples.getMcfpsAnBn(Collections.singleton("a"));
        final M3CSolver<String> solver = getSolver(mcfps);
        final String formula = "mu X.(<>X || 'a,b')";

        assertSolve(solver, formula, false);
    }

    @Test
    void testSolveWithMultipleAPs() throws ParseException {
        final ModalContextFreeProcessSystem<String, String> mcfps =
                Examples.getMcfpsAnBn(new HashSet<>(Arrays.asList("a", "b")));
        final M3CSolver<String> solver = getSolver(mcfps);

        final String formula = "mu X.(<>X || 'a,b')";
        assertSolve(solver, formula, true);

        final String formulaWithNegatedAP = "mu X.(<>X || !'a,b')";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    public abstract M3CSolver<String> getSolver(ModalContextFreeProcessSystem<String, String> mcfps);

    protected <P> void assertSolve(M3CSolver<P> solver, P property, boolean expectedIsSat) throws ParseException {
        Assert.assertEquals(solver.solve(property), expectedIsSat);
    }

}
