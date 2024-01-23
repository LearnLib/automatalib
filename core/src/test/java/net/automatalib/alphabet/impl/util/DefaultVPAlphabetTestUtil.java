/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.alphabet.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import net.automatalib.common.util.collection.CollectionUtil;

/**
 * Utility class, aggregating definitions used for testing {@link DefaultVPAlphabet}s.
 */
public final class DefaultVPAlphabetTestUtil {

    public static final List<Character> INTERNAL_SYMBOLS = CollectionUtil.charRange('1', '3');
    public static final List<Character> CALL_SYMBOLS = CollectionUtil.charRange('a', 'c');
    public static final List<Character> RETURN_SYMBOLS = CollectionUtil.charRange('r', 't');
    public static final List<Character> NON_CONTAINED_SYMBOLS = Arrays.asList('x', '8', ' ');

    public static final List<Character> JOINED_SYMBOLS;

    static {
        JOINED_SYMBOLS = new ArrayList<>(9);
        JOINED_SYMBOLS.addAll(INTERNAL_SYMBOLS);
        JOINED_SYMBOLS.addAll(CALL_SYMBOLS);
        JOINED_SYMBOLS.addAll(RETURN_SYMBOLS);
    }

    private DefaultVPAlphabetTestUtil() {
        // prevent instantiation
    }
}
