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
package net.automatalib.automata.words.util;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.VPDSym;

/**
 * Utility class, aggregating definitions used for testing {@link net.automatalib.words.GrowingAlphabet}s.
 *
 * @author frohme
 */
public final class GrowingVPDAlphabetTestUtil {

    public static final List<VPDSym<Character>> INTERNAL_SYMBOLS = getInternalSymbols();
    public static final List<VPDSym<Character>> CALL_SYMBOLS = getCallSymbols();
    public static final List<VPDSym<Character>> RETURN_SYMBOLS = getReturnSymbols();
    public static final List<VPDSym<Character>> NON_CONTAINED_SYMBOLS = getNonAlphabetSymbols();

    public static final List<VPDSym<Character>> JOINED_SYMBOLS;

    static {
        JOINED_SYMBOLS = new ArrayList<>(DefaultVPDAlphabetTestUtil.JOINED_SYMBOLS.size());
        JOINED_SYMBOLS.addAll(INTERNAL_SYMBOLS);
        JOINED_SYMBOLS.addAll(CALL_SYMBOLS);
        JOINED_SYMBOLS.addAll(RETURN_SYMBOLS);
    }

    private GrowingVPDAlphabetTestUtil() {
        // prevent instantiation
    }

    private static List<VPDSym<Character>> getInternalSymbols() {
        return buildVPDSym(DefaultVPDAlphabetTestUtil.INTERNAL_SYMBOLS, VPDAlphabet.SymbolType.INTERNAL, 0);
    }

    private static <I> List<VPDSym<I>> buildVPDSym(List<I> source, VPDAlphabet.SymbolType type, int globalIndex) {
        final List<VPDSym<I>> result = new ArrayList<>(source.size());
        int idx = 0;

        for (I i : source) {
            result.add(new VPDSym<>(i, type, idx, globalIndex + idx));
            idx++;
        }

        return result;
    }

    private static List<VPDSym<Character>> getCallSymbols() {
        return buildVPDSym(DefaultVPDAlphabetTestUtil.CALL_SYMBOLS,
                           VPDAlphabet.SymbolType.CALL,
                           INTERNAL_SYMBOLS.size());
    }

    private static List<VPDSym<Character>> getReturnSymbols() {
        return buildVPDSym(DefaultVPDAlphabetTestUtil.RETURN_SYMBOLS,
                           VPDAlphabet.SymbolType.RETURN,
                           INTERNAL_SYMBOLS.size() + CALL_SYMBOLS.size());
    }

    private static List<VPDSym<Character>> getNonAlphabetSymbols() {
        return buildVPDSym(DefaultVPDAlphabetTestUtil.NON_CONTAINED_SYMBOLS,
                           VPDAlphabet.SymbolType.INTERNAL,
                           INTERNAL_SYMBOLS.size() + CALL_SYMBOLS.size() + RETURN_SYMBOLS.size() * 23);
    }
}
