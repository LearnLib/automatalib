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
package net.automatalib.word.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.VPAlphabet.SymbolType;
import net.automatalib.alphabet.impl.GrowingVPAlphabet;
import net.automatalib.alphabet.impl.VPSym;

/**
 * Utility class, aggregating definitions used for testing {@link GrowingAlphabet}s.
 */
public final class GrowingVPAlphabetTestUtil {

    public static final GrowingVPAlphabet<Character> ALPHABET;

    public static final List<VPSym<Character>> INTERNAL_SYMBOLS;
    public static final List<VPSym<Character>> CALL_SYMBOLS;
    public static final List<VPSym<Character>> RETURN_SYMBOLS;
    public static final List<VPSym<Character>> NON_CONTAINED_SYMBOLS;

    public static final List<VPSym<Character>> JOINED_SYMBOLS;

    static {
        ALPHABET = new GrowingVPAlphabet<>();

        INTERNAL_SYMBOLS = DefaultVPAlphabetTestUtil.INTERNAL_SYMBOLS.stream()
                                                                     .map(s -> ALPHABET.addNewSymbol(s,
                                                                                                     SymbolType.INTERNAL))
                                                                     .collect(Collectors.toList());

        CALL_SYMBOLS = DefaultVPAlphabetTestUtil.CALL_SYMBOLS.stream()
                                                             .map(s -> ALPHABET.addNewSymbol(s, SymbolType.CALL))
                                                             .collect(Collectors.toList());

        RETURN_SYMBOLS = DefaultVPAlphabetTestUtil.RETURN_SYMBOLS.stream()
                                                                 .map(s -> ALPHABET.addNewSymbol(s, SymbolType.RETURN))
                                                                 .collect(Collectors.toList());

        final GrowingVPAlphabet<Character> dummyAlphabet = new GrowingVPAlphabet<>();
        NON_CONTAINED_SYMBOLS = DefaultVPAlphabetTestUtil.NON_CONTAINED_SYMBOLS.stream()
                                                                               .map(s -> dummyAlphabet.addNewSymbol(s,
                                                                                                                    SymbolType.INTERNAL))
                                                                               .collect(Collectors.toList());

        JOINED_SYMBOLS = new ArrayList<>(DefaultVPAlphabetTestUtil.JOINED_SYMBOLS.size());
        JOINED_SYMBOLS.addAll(INTERNAL_SYMBOLS);
        JOINED_SYMBOLS.addAll(CALL_SYMBOLS);
        JOINED_SYMBOLS.addAll(RETURN_SYMBOLS);
    }

    private GrowingVPAlphabetTestUtil() {
        // prevent instantiation
    }
}
