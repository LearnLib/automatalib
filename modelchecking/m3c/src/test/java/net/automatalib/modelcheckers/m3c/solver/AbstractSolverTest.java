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
import java.util.Map;
import java.util.Set;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.graphs.base.compact.CompactMPG;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractSolverTest<T extends AbstractPropertyTransformer<T, String, String>> {

    protected static ModalContextFreeProcessSystem<String, String> mcfps;

    @BeforeClass
    public static void setup() {
        mcfps = getMcfps(new HashSet<>());
    }

    private static ModalContextFreeProcessSystem<String, String> getMcfps(Set<String> finalNodesAP) {
        final CompactMPG<String, String> mpg = buildMPG(new CompactMPG<>(), finalNodesAP);

        return new ModalContextFreeProcessSystem<String, String>() {

            @Override
            public Map<String, ModalProcessGraph<?, String, ?, String, ?>> getMPGs() {
                return Collections.singletonMap("P", mpg);
            }

            @Override
            public String getMainProcess() {
                return "P";
            }
        };
    }

    private static <N, E, AP, MMPG extends MutableModalProcessGraph<N, String, E, AP, ?>> MMPG buildMPG(MMPG mpg,
                                                                                                        Set<AP> finalNodeAPs) {

        final N start = mpg.addNode();
        final N end = mpg.addNode();
        final N s1 = mpg.addNode();
        final N s2 = mpg.addNode();

        mpg.setInitialNode(start);
        mpg.setFinalNode(end);
        mpg.setAtomicPropositions(end, finalNodeAPs);

        final E e1 = mpg.connect(start, s1);
        final E e2 = mpg.connect(start, end);
        final E e3 = mpg.connect(s1, s2);
        final E e4 = mpg.connect(s2, end);

        mpg.getEdgeProperty(e1).setMust();
        mpg.setEdgeLabel(e1, "a");

        mpg.getEdgeProperty(e2).setMust();
        mpg.setEdgeLabel(e2, "e");

        mpg.getEdgeProperty(e3).setMust();
        mpg.getEdgeProperty(e3).setProcess();
        mpg.setEdgeLabel(e3, "P");

        mpg.getEdgeProperty(e4).setMust();
        mpg.setEdgeLabel(e4, "b");

        return mpg;
    }

    @Test
    void testSolve() throws ParseException {
        M3CSolver<String> solver = getSolver(mcfps);

        String formula = "mu X.(<b><b>true || <>X)";
        assertSolve(solver, formula, true);

        String negatedFormula = "!(" + formula + ")";
        assertSolve(solver, negatedFormula, false);
    }

    public abstract M3CSolver<String> getSolver(ModalContextFreeProcessSystem<String, String> mcfps);

    protected <P> void assertSolve(M3CSolver<P> solver, P property, boolean expectedIsSat) throws ParseException {
        Assert.assertEquals(solver.solve(property), expectedIsSat);
    }

    @Test
    void testSolveWithAPs() throws ParseException {
        ModalContextFreeProcessSystem<String, String> mcfps = getMcfps(new HashSet<>(Arrays.asList("a", "b")));
        String formula = "mu X.(<>X || 'a,b')";
        M3CSolver<String> solver = getSolver(mcfps);
        assertSolve(solver, formula, true);

        mcfps = getMcfps(new HashSet<>(Collections.singletonList("a")));
        solver = getSolver(mcfps);
        assertSolve(solver, formula, false);
    }

}
