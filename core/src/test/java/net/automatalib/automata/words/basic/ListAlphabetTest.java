/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.automata.words.basic;

import java.util.Arrays;
import java.util.List;

import net.automatalib.words.impl.ListAlphabet;

/**
 * @author frohme
 */
public class ListAlphabetTest extends AbstractAlphabetTest<Integer, ListAlphabet<Integer>> {

    private static final List<Integer> CONTAINED_SYMBOLS = Arrays.asList(1, 2, 3, 4, 5);
    private static final List<Integer> NON_CONTAINED_SYMBOLS = Arrays.asList(-1, 13, 42);

    @Override
    protected List<Integer> getAlphabetSymbols() {
        return CONTAINED_SYMBOLS;
    }

    @Override
    protected List<Integer> getNonAlphabetSymbols() {
        return NON_CONTAINED_SYMBOLS;
    }

    @Override
    protected ListAlphabet<Integer> getAlphabet() {
        return new ListAlphabet<>(CONTAINED_SYMBOLS);
    }
}
