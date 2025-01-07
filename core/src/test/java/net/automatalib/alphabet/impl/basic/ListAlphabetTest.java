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

import java.util.List;

import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.alphabet.impl.util.AlphabetTestUtil;

public class ListAlphabetTest extends AbstractAlphabetTest<Integer, ListAlphabet<Integer>> {

    @Override
    protected List<Integer> getAlphabetSymbols() {
        return AlphabetTestUtil.CONTAINED_SYMBOLS_LIST;
    }

    @Override
    protected List<Integer> getNonAlphabetSymbols() {
        return AlphabetTestUtil.NON_CONTAINED_SYMBOLS_LIST;
    }

    @Override
    protected ListAlphabet<Integer> getAlphabet() {
        return new ListAlphabet<>(AlphabetTestUtil.CONTAINED_SYMBOLS_LIST);
    }
}
