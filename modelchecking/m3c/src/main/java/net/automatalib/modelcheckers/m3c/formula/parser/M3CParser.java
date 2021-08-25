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
package net.automatalib.modelcheckers.m3c.formula.parser;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;

/**
 * This class can be used to parse formulas in CTL and the mu-calculus.
 *
 * <p>
 * CTL grammar: f := AF f | AG f | A(f U f) | EF f | EG f | E(f U f) | f &amp;&amp; f | f || f | f -&gt; f | f &lt;-&gt;
 * f | !f | &lt;&gt;f | &lt;ID&gt;f | []f | [ID]f | AP | true | false | (f)
 * </p>
 * <p>
 * mu-calculus grammar: f := mu ID.(f) | nu ID.(f) | ID | f &amp;&amp; f | f || f | f -&gt; f | f &lt;-&gt; f | !f |
 * &lt;&gt;f | &lt;ID&gt;f | []f | [ID]f | AP | true | false | (f)
 * </p>
 * <p>
 * AP := "arbitrary string not containing double quotation marks" | 'arbitrary string not containing single quotation
 * marks'
 * </p>
 * <p>
 * ID := ["a"-"z","A"-"Z"] (["a"-"z","A"-"Z"] | ["0"-"9"] | "_")*
 * </p>
 */
public final class M3CParser {

    private M3CParser() {}

    /**
     * @param formula ctl or mu-calculus formula to be parsed
     * @return {@code formula}'s abstract syntax tree. Each label is transformed to a {@code String}. Each atomic
     * proposition is transformed to a {@code String}. A formula can represent a set of atomic propositions, i.e.\ a
     * conjunction of atomic propositions, through a comma-separated list of atomic propositions.
     * @throws ParseException if {@code formula} is not a valid formula.
     */
    public static FormulaNode<String, String> parse(String formula) throws ParseException {
        return parse(formula, Function.identity(), x -> new HashSet<>(Arrays.asList(x.split(","))));
    }

    /**
     * @param formula     ctl or mu-calculus formula to be parsed
     * @param labelParser used to parse the label within a diamond or box operator to an object of type {@code L}.
     * @param apParser    used to parse an atomic proposition to an object of type {@code AP}.
     * @param <L>         edge label type
     * @param <AP>        atomic proposition type
     * @return {@code formula}'s abstract syntax tree.
     * @throws ParseException if {@code formula} is not a valid formula.
     */
    public static <L, AP> FormulaNode<L, AP> parse(String formula,
                                                   Function<String, L> labelParser,
                                                   Function<String, Set<AP>> apParser) throws ParseException {

        final StringReader reader = new StringReader(formula);

        //TODO: test ctl formula having an atomic proposition containing the substrings mu and nu
        if (formula.contains("mu") || formula.contains("nu")) {
            return new InternalM3CParserMuCalc<L, AP>(reader).parse(labelParser, apParser);
        }

        return new InternalM3CParserCTL<L, AP>(reader).parse(labelParser, apParser);
    }

}
