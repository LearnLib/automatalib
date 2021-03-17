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

public final class ParserMuCalc {

    private ParserMuCalc() {}

    public static FormulaNode<String, String> parse(String ctlFormula) throws ParseException {
        return parse(ctlFormula, Function.identity(), x -> new HashSet<>(Arrays.asList(x.split(","))));
    }

    public static <L, AP> FormulaNode<L, AP> parse(String ctlFormula,
                                                   Function<String, L> labelParser,
                                                   Function<String, Set<AP>> apParser) throws ParseException {
        return new InternalM3CParserMuCalc<L, AP>(new StringReader(ctlFormula)).parse(labelParser, apParser);
    }

}
