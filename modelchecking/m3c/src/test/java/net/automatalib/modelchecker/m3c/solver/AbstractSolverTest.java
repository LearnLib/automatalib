/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.modelchecker.m3c.solver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.MutableProceduralModalProcessGraph;
import net.automatalib.graph.ProceduralModalProcessGraph;
import net.automatalib.graph.base.DefaultCFMPS;
import net.automatalib.graph.base.compact.CompactPMPG;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.formula.parser.ParseException;
import net.automatalib.modelchecker.m3c.solver.M3CSolver.TypedM3CSolver;
import net.automatalib.modelchecker.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.modelchecker.m3c.util.Examples;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractSolverTest<T extends AbstractPropertyTransformer<T, String, String>> {

    @Test
    void testSolve() throws ParseException {
        final M3CSolver<String> solver = getSolver(Examples.getCfmpsAnBn(Collections.emptySet()));

        final String formula = "mu X.(<b><b>true || <>X)";
        assertSolve(solver, formula, true);

        final String negatedFormula = "!(" + formula + ")";
        assertSolve(solver, negatedFormula, false);

        // use !'a' to simulate true, as no node satisfies 'a'
        final String formulaWithNegatedAP = "mu X.(<b><b>!'a' || <>X)";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    @Test
    void testSolveWithSingleAP() throws ParseException {
        final ContextFreeModalProcessSystem<String, String> cfmps = Examples.getCfmpsAnBn(Collections.singleton("a"));
        final M3CSolver<String> solver = getSolver(cfmps);
        final String formula = "mu X.(<>X || 'a,b')";

        assertSolve(solver, formula, false);
    }

    @Test
    void testSolveWithMultipleAPs() throws ParseException {
        final ContextFreeModalProcessSystem<String, String> cfmps =
                Examples.getCfmpsAnBn(new HashSet<>(Arrays.asList("a", "b")));
        final M3CSolver<String> solver = getSolver(cfmps);

        final String formula = "mu X.(<>X || 'a' && 'b')";
        assertSolve(solver, formula, true);

        final String formulaWithNegatedAP = "mu X.(<>X || !('a' && 'b'))";
        assertSolve(solver, formulaWithNegatedAP, true);
    }

    @Test
    void testSBASystem() throws ParseException {
        final ContextFreeModalProcessSystem<String, Void> cfmps = Examples.getSBASystem();
        final TypedM3CSolver<FormulaNode<String, Void>> solver = getTypedSolver(cfmps);

        final Function<String, String> labelParser = Function.identity();
        final Function<String, Void> apParser = s -> null;

        assertSolve(solver, M3CParser.parse("EF <P1><P2><P3>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("EF <b>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("EF <c>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("EF <c><R>true", labelParser, apParser), false);
        assertSolve(solver, M3CParser.parse("EF <c><c>true", labelParser, apParser), false);
        assertSolve(solver, M3CParser.parse("EF <c><d>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><a>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><a><R>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><b>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><b><R>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><b><R><R>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3><c>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3><c><c>true", labelParser, apParser), false);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3><c><d>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3><c><R>true", labelParser, apParser), false);
        assertSolve(solver, M3CParser.parse("<P1><P2><P3><P4>true", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("EF <P4><>true", labelParser, apParser), false);
        assertSolve(solver, M3CParser.parse("EF [] false", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("AF [] false", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("AG EF [] false", labelParser, apParser), true);
        assertSolve(solver, M3CParser.parse("AG ([P1] (AF <R>true))", labelParser, apParser), false);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithInvalidMainProcess() {
        ContextFreeModalProcessSystem<String, String> cfmps = new ContextFreeModalProcessSystem<String, String>() {

            @Override
            public Map<String, ProceduralModalProcessGraph<?, String, ?, String, ?>> getPMPGs() {
                return new HashMap<>();
            }

            @Override
            public @Nullable String getMainProcess() {
                return "P";
            }
        };
        getSolver(cfmps);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithNoMainProcess() {
        ContextFreeModalProcessSystem<String, String> cfmps = new ContextFreeModalProcessSystem<String, String>() {

            @Override
            public Map<String, ProceduralModalProcessGraph<?, String, ?, String, ?>> getPMPGs() {
                return new HashMap<>();
            }

            @Override
            public @Nullable String getMainProcess() {
                return null;
            }
        };
        getSolver(cfmps);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void testSolveWithUnguardedProcess() {
        final CompactPMPG<String, String> pmpg = getUnguardedPMPG(new CompactPMPG<>(""));
        getSolver(new DefaultCFMPS<>("P", Collections.singletonMap("P", pmpg)));
    }

    private static <N, E, AP, M extends MutableProceduralModalProcessGraph<N, String, E, AP, ?>> M getUnguardedPMPG(M pmpg) {
        final N start = pmpg.addNode();
        final N end = pmpg.addNode();

        pmpg.setInitialNode(start);
        pmpg.setFinalNode(end);

        final E e1 = pmpg.connect(start, end);
        pmpg.getEdgeProperty(e1).setMust();
        pmpg.setEdgeLabel(e1, "a");

        final E e2 = pmpg.connect(start, end);
        pmpg.getEdgeProperty(e2).setMust();
        pmpg.getEdgeProperty(e2).setProcess();
        pmpg.setEdgeLabel(e2, "P");

        return pmpg;
    }

    public abstract M3CSolver<String> getSolver(ContextFreeModalProcessSystem<String, String> cfmps);

    public abstract <L, AP> TypedM3CSolver<FormulaNode<L, AP>> getTypedSolver(ContextFreeModalProcessSystem<L, AP> cfmps);

    protected <P> void assertSolve(M3CSolver<P> solver, P property, boolean expectedIsSat) throws ParseException {
        Assert.assertEquals(solver.solve(property), expectedIsSat);
    }

}
