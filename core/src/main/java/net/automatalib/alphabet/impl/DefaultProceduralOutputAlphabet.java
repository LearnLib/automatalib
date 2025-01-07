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
package net.automatalib.alphabet.impl;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralOutputAlphabet;

/**
 * Default implementation of a {@link ProceduralOutputAlphabet}.
 *
 * @param <O>
 *         input symbol type
 */
public class DefaultProceduralOutputAlphabet<O> extends MapAlphabet<O> implements ProceduralOutputAlphabet<O> {

    private final Alphabet<O> regularAlphabet;
    private final O errorSymbol;

    public DefaultProceduralOutputAlphabet(Alphabet<O> regularAlphabet, O errorSymbol) {
        super(buildCombinedAlphabet(regularAlphabet, errorSymbol));
        this.regularAlphabet = regularAlphabet;
        this.errorSymbol = errorSymbol;
    }

    private static <O> List<O> buildCombinedAlphabet(Alphabet<O> internalOutputs, O errorSymbol) {
        final List<O> result = new ArrayList<>(internalOutputs.size() + 1);
        result.addAll(internalOutputs);
        result.add(errorSymbol);

        return result;
    }

    @Override
    public Alphabet<O> getRegularAlphabet() {
        return this.regularAlphabet;
    }

    @Override
    public O getErrorSymbol() {
        return this.errorSymbol;
    }
}
