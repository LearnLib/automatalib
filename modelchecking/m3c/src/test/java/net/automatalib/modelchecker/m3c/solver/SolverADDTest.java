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
package net.automatalib.modelchecker.m3c.solver;

import java.util.Collections;

import net.automatalib.alphabet.Alphabets;
import net.automatalib.alphabet.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.StackSPA;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.formula.parser.ParseException;
import net.automatalib.modelchecker.m3c.solver.M3CSolver.TypedM3CSolver;
import net.automatalib.modelchecker.m3c.transformer.ADDTransformer;
import net.automatalib.util.automaton.procedural.SPAs;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SolverADDTest extends AbstractSolverTest<ADDTransformer<String, String>> {

    public M3CSolver<String> getSolver(ContextFreeModalProcessSystem<String, String> cfmps) {
        return M3CSolvers.addSolver(cfmps);
    }

    public <L, AP> TypedM3CSolver<FormulaNode<L, AP>> getTypedSolver(ContextFreeModalProcessSystem<L, AP> cfmps) {
        return M3CSolvers.typedADDSolver(cfmps);
    }

    /**
     * This test-case resulted from an external application of the M3C model-checker.
     */
    @Test
    public void testRegression() throws ParseException {

        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('a'), Alphabets.singleton('S'), 'R');

        final CompactDFA<Character> p = new CompactDFA<>(alphabet.getProceduralAlphabet());
        final Integer s0 = p.addInitialState(true);
        final Integer s1 = p.addState(false);

        p.addTransition(s0, 'a', s0);
        p.addTransition(s0, 'S', s1);
        p.addTransition(s1, 'a', s1);
        p.addTransition(s1, 'S', s1);

        final SPA<?, Character> spa = new StackSPA<>(alphabet, 'S', Collections.singletonMap('S', p));
        final ContextFreeModalProcessSystem<Character, Void> cfmps = SPAs.toCFMPS(spa);

        final FormulaNode<Character, Void> formula = M3CParser.parse("[S][a][R]false", l -> l.charAt(0), ap -> null);
        final ADDSolver<Character, Void> solver = new ADDSolver<>(cfmps);

        Assert.assertFalse(solver.solve(formula));
    }
}
