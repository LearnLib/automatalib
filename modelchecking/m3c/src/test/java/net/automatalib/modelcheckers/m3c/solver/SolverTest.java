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

import java.util.Collections;
import java.util.Map;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.graphs.base.compact.CompactMPG;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class SolverTest<T extends PropertyTransformer<T>> {

    protected static ModalContextFreeProcessSystem<Character, String> mcfps;

    @BeforeClass
    public static void setup() {
        final CompactMPG<Character, String> mpg = buildMPG(new CompactMPG<>());

        mcfps = new ModalContextFreeProcessSystem<Character, String>() {

            @Override
            public Alphabet<Character> getTerminalAlphabet() {
                return Alphabets.fromArray('a', 'b', 'e');
            }

            @Override
            public Alphabet<Character> getProcessAlphabet() {
                return Alphabets.singleton('P');
            }

            @Override
            public Map<Character, ModalProcessGraph<?, Character, ?, String, ?>> getMPGs() {
                return Collections.singletonMap('P', mpg);
            }

            @Override
            public Character getMainProcess() {
                return 'P';
            }
        };
    }

    private static <N, E, AP, MMPG extends MutableModalProcessGraph<N, Character, E, AP, ?>> MMPG buildMPG(MMPG mpg) {

        final N start = mpg.addNode();
        final N end = mpg.addNode();
        final N s1 = mpg.addNode();
        final N s2 = mpg.addNode();

        mpg.setInitialNode(start);
        mpg.setFinalNode(end);

        final E e1 = mpg.connect(start, s1);
        final E e2 = mpg.connect(start, end);
        final E e3 = mpg.connect(s1, s2);
        final E e4 = mpg.connect(s2, end);

        mpg.getEdgeProperty(e1).setMust();
        mpg.setEdgeLabel(e1, 'a');

        mpg.getEdgeProperty(e2).setMust();
        mpg.setEdgeLabel(e2, 'e');

        mpg.getEdgeProperty(e3).setMust();
        mpg.setEdgeLabel(e3, 'P');

        mpg.getEdgeProperty(e4).setMust();
        mpg.setEdgeLabel(e4, 'b');

        return mpg;
    }

    @Test
    void testSolve() throws ParseException {
        String formula = "mu X.(<b><b>true || <>X)";
        SolveDD<T, Character, String> solver = getSolver(mcfps, formula, false);
        assertSolve(solver, true);

        String negatedFormula = "!(" + formula + ")";
        solver = getSolver(mcfps, negatedFormula, false);
        assertSolve(solver, false);
    }

    public abstract SolveDD<T, Character, String> getSolver(ModalContextFreeProcessSystem<Character, String> mcfps,
                                                            String formula,
                                                            boolean formulaIsCtl) throws ParseException;

    protected void assertSolve(SolveDD<T, Character, String> solver, boolean expectedIsSat) {
        solver.solve();
        Assert.assertEquals(expectedIsSat, solver.isSat());
    }

}
