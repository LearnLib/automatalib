/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.modelchecker.m3c;

import java.util.Collections;
import java.util.function.Function;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.exception.FormatException;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.solver.BDDSolver;
import net.automatalib.util.automaton.procedural.SBAs;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test-cases not necessarily related to M3C directly but to its application in external contexts.
 */
public class ExternalTest {

    @Test
    public void testRegressionSBA() throws FormatException {

        final ProceduralInputAlphabet<Character> alphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.singleton('a'), Alphabets.singleton('S'), 'R');

        final CompactDFA<Character> p = new CompactDFA<>(alphabet);
        final Integer s0 = p.addInitialState(true);
        final Integer s1 = p.addState(true);

        p.addTransition(s0, 'S', s0);
        p.addTransition(s0, 'a', s1);
        p.addTransition(s0, 'R', s1);

        final SBA<?, Character> sba = new StackSBA<>(alphabet, 'S', Collections.singletonMap('S', p));
        final ContextFreeModalProcessSystem<Character, Void> cfmps = SBAs.toCFMPS(sba);

        final Function<String, Character> labelParser = l -> l.charAt(0);
        final Function<String, Void> apParser = ap -> null;

        // the a-transition should not lead into a return state
        final FormulaNode<Character, Void> formulaFalse = M3CParser.parse("EF <S><a><a>true", labelParser, apParser);
        // only R-transitions should return
        final FormulaNode<Character, Void> formulaTrue = M3CParser.parse("EF <S><R><R>true", labelParser, apParser);

        final BDDSolver<Character, Void> solver = new BDDSolver<>(cfmps);

        Assert.assertFalse(solver.solve(formulaFalse));
        Assert.assertTrue(solver.solve(formulaTrue));
    }
}
