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
package net.automatalib.words.impl;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAOutputAlphabet;

/**
 * @author frohme
 */
public class DefaultSPAOutputAlphabet<O> extends MapAlphabet<O> implements SPAOutputAlphabet<O> {

    private final Alphabet<O> internalOutputs;
    private final O errorSymbol;

    public DefaultSPAOutputAlphabet(Alphabet<O> internalOutputs, O errorSymbol) {
        super(buildCombinedAlphabet(internalOutputs, errorSymbol));
        this.internalOutputs = internalOutputs;
        this.errorSymbol = errorSymbol;
    }

    private static <O> List<O> buildCombinedAlphabet(Alphabet<O> internalOutputs, O errorSymbol) {
        final List<O> result = new ArrayList<>(internalOutputs.size() + 1);
        result.addAll(internalOutputs);
        result.add(errorSymbol);

        return result;
    }

    @Override
    public Alphabet<O> getInternalAlphabet() {
        return this.internalOutputs;
    }

    @Override
    public O getErrorSymbol() {
        return this.errorSymbol;
    }
}
