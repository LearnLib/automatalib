/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import net.automatalib.automaton.mmlt.SymbolCombiner;

/**
 * Combines multiple string outputs by concatenating them and using {@code |} as separator.
 */
public final class StringSymbolCombiner implements SymbolCombiner<String> {

    private static final StringSymbolCombiner INSTANCE = new StringSymbolCombiner();

    private StringSymbolCombiner() {
        // prevent instantiation
    }

    public static StringSymbolCombiner getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isCombinedSymbol(String symbol) {
        return symbol.contains("|");
    }

    @Override
    public String combineSymbols(List<String> symbols) {
        final List<String> sorted = new ArrayList<>(symbols);
        sorted.sort(Comparator.naturalOrder());

        final StringJoiner sj = new StringJoiner("|");
        for (String s : sorted) {
            sj.add(s);
        }
        return sj.toString();
    }

    @Override
    public List<String> separateSymbols(String symbol) {
        if (!this.isCombinedSymbol(symbol)) {
            return Collections.singletonList(symbol);
        }

        return Arrays.asList(symbol.split("\\|"));
    }
}
