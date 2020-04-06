/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.automata.words.util;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Utilities for regular {@link net.automatalib.words.Alphabet} tests.
 *
 * @author frohme
 */
public final class AlphabetTestUtil {

    public static final Integer[] CONTAINED_SYMBOLS_ARR = new Integer[] {1, 2, 3, 4, 5};
    public static final Integer[] NON_CONTAINED_SYMBOLS_ARR = new Integer[] {-1, 13, 42};
    public static final List<Integer> CONTAINED_SYMBOLS_LIST = Lists.newArrayList(CONTAINED_SYMBOLS_ARR);
    public static final List<Integer> NON_CONTAINED_SYMBOLS_LIST = Lists.newArrayList(NON_CONTAINED_SYMBOLS_ARR);

    private AlphabetTestUtil() {
        // prevent instantiation
    }
}
