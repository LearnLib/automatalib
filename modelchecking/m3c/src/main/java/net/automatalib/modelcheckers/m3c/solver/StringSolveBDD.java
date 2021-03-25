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

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserCTL;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserMuCalc;

public class StringSolveBDD extends AbstractSolveBDD<String, String> implements Solver<String> {

    public StringSolveBDD(ModalContextFreeProcessSystem<String, String> mcfps) {
        super(mcfps);
    }

    @Override
    public boolean solve(String formula, boolean formulaIsCtl) throws ParseException {
        return super.solve(formulaIsCtl ? ParserCTL.parse(formula) : ParserMuCalc.parse(formula), formulaIsCtl);
    }

}
