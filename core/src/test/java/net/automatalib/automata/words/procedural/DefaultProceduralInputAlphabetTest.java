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
package net.automatalib.automata.words.procedural;

import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.DefaultProceduralInputAlphabet;

/**
 * @author frohme
 */
public class DefaultProceduralInputAlphabetTest extends AbstractProceduralInputAlphabetTest<DefaultProceduralInputAlphabet<Character>> {

    @Override
    protected DefaultProceduralInputAlphabet<Character> getAlphabet(Alphabet<Character> internalAlphabet,
                                                                    Alphabet<Character> callAlphabet,
                                                                    char returnSymbol) {
        return new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    }
}