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
package net.automatalib.alphabet.impl.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.automatalib.alphabet.impl.EnumAlphabet;
import net.automatalib.alphabet.impl.basic.EnumAlphabetTest.Symbol;

public class EnumAlphabetTest extends AbstractAlphabetTest<Symbol, EnumAlphabet<Symbol>> {

    @Override
    protected List<Symbol> getAlphabetSymbols() {
        return Arrays.asList(Symbol.values());
    }

    @Override
    protected List<Symbol> getNonAlphabetSymbols() {
        return Collections.singletonList(null);
    }

    @Override
    protected EnumAlphabet<Symbol> getAlphabet() {
        return new EnumAlphabet<>(Symbol.class, false);
    }

    enum Symbol {
        S1,
        S2,
        S3,
    }

}
