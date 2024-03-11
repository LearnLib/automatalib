/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.data;

import java.util.Iterator;

import net.automatalib.data.SymbolicDataValue.Parameter;
import net.automatalib.data.SymbolicDataValueGenerator.ParameterGenerator;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.word.Word;

/**
 * A valuation of parameters.
 *
 * @author falk
 */
public class ParValuation extends Mapping<Parameter, DataValue<?>> {

    public ParValuation() {

    }

    public ParValuation(PSymbolInstance psi) {
        ParameterGenerator pgen = new ParameterGenerator();
        for (DataValue dv : psi.getParameterValues()) {
            this.put(pgen.next(dv.getType()), dv);
        }
    }

    public ParValuation(Word<PSymbolInstance> dw) {
    	ParameterGenerator pgen = new ParameterGenerator();
    	Iterator<PSymbolInstance> it = dw.iterator();
    	while (it.hasNext()) {
    		PSymbolInstance psi = it.next();
    		for (DataValue dv : psi.getParameterValues()) {
    			put(pgen.next(dv.getType()), dv);
    		}
    	}
    }
}
