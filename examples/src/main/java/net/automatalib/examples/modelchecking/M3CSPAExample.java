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
package net.automatalib.examples.modelchecking;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.automata.spa.MCFPSView;
import net.automatalib.automata.spa.SPA;
import net.automatalib.examples.spa.PalindromeExample;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.M3CParser;
import net.automatalib.modelcheckers.m3c.solver.M3CSolver.TypedM3CSolver;
import net.automatalib.modelcheckers.m3c.solver.M3CSolvers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class M3CSPAExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(M3CSPAExample.class);

    private M3CSPAExample() {
        // prevent initialization
    }

    public static void main(String[] args) throws ParseException {
        final SPA<?, Character> spa = PalindromeExample.buildSPA();
        final MCFPSView<Character> view = new MCFPSView<>(spa);

        //@formatter:off
        final String[] formulae = {"mu X.(<>X || <b><b>true)", // there exists a path with "bb"
                                   "mu X.(<>X || <b><S><b>true)", // there exists a path with "bSb"
                                   "mu X.(<>X || <T><b>true)", // there exists a path with "bTb"
                                   // "nu X. ([]X && mu Y. (<>Y || (\"fin\" && [] false)))", // on all paths there exists a path to the final state
                                   "nu X. ([]X || (\"fin\" && [] false))", // on all paths there exists a path to the final state
                                   "mu X. ([]X || (\"fin\" && [] false))", // on all paths there exists a path to the final state
                                   // "nu X. ([]X && (<S>true -> mu Y. (<S>Y || <R>true)))", // globally, if there exists and S, it must be followed by an R eventually
                                   "<S><T><a>true", // There exists a starting sequence of STa
                                   "<S><T><b>true", // There exists a starting sequence of STb
                                   "<S><T><c>true", // There exists a starting sequence of STc
                                   "<S><a><T><c>true", // There exists a starting sequence of SaTc
                                   "<S><a><S><T><c>true", // There exists a starting sequence of SaSTc
        };
        //@formatter:on

        final Function<String, Character> labelParser = s -> s.charAt(0);
        final Function<String, Set<Void>> apParser = s -> Collections.emptySet();
        final TypedM3CSolver<FormulaNode<Character, Void>> solver = M3CSolvers.typedBDDSolver(view);

        for (String f : formulae) {
            final FormulaNode<Character, Void> formula = M3CParser.parse(f, labelParser, apParser);
            LOGGER.info("Is '{}' satisfied? {}", f, solver.solve(formula));
        }

    }
}
