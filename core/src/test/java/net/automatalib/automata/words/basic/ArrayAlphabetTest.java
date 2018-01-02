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

import net.automatalib.words.impl.ArrayAlphabet;

/**
 * @author frohme
 */
public class ArrayAlphabetTest extends AbstractAlphabetTest<Integer, ArrayAlphabet<Integer>> {

    private static final Integer[] CONTAINED_SYMBOLS = new Integer[] {1, 2, 3, 4, 5};
    private static final Integer[] NON_CONTAINED_SYMBOLS = new Integer[] {-1, 13, 42};

    @Override
    protected List<Integer> getAlphabetSymbols() {
        return Arrays.asList(CONTAINED_SYMBOLS);
    }

    @Override
    protected List<Integer> getNonAlphabetSymbols() {
        return Arrays.asList(NON_CONTAINED_SYMBOLS);
    }

    @Override
    protected ArrayAlphabet<Integer> getAlphabet() {
        return new ArrayAlphabet<>(CONTAINED_SYMBOLS);
    }
}