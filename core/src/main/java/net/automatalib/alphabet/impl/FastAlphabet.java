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
package net.automatalib.alphabet.impl;

import java.util.Arrays;
import java.util.List;

import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.common.util.nid.DynamicList;
import net.automatalib.common.util.nid.MutableNumericID;

/**
 * A fast alphabet implementation, that assumes identifiers are stored directly in the input symbols.
 *
 * @param <I>
 *         input symbol class.
 */
public class FastAlphabet<I extends MutableNumericID> extends DynamicList<I> implements GrowingAlphabet<I> {

    public FastAlphabet() {}

    @SafeVarargs
    public FastAlphabet(I... symbols) {
        this(Arrays.asList(symbols));
    }

    public FastAlphabet(List<? extends I> symbols) {
        super(symbols);
    }

    @Override
    public int addSymbol(I a) {
        add(a);
        return a.getId();
    }

    @Override
    public I getSymbol(int index) {
        return get(index);
    }

    @Override
    public int getSymbolIndex(I symbol) {
        int id = symbol.getId();
        if (id < 0 || id >= size() || get(id) != symbol) {
            throw new IllegalArgumentException("Invalid symbol: " + symbol + " does not belong to this alphabet");
        }
        return id;
    }

    @Override
    public int compare(I o1, I o2) {
        return o1.getId() - o2.getId();
    }

    @Override
    public boolean containsSymbol(I symbol) {
        int index = symbol.getId();
        return index >= 0 && index < size() && get(index) == symbol;
    }

    /*
     * This alphabet-specific view is required by the SequencedCollection changes introduced in JDK21,
     * See https://openjdk.org/jeps/431 for more information.
     */
    @Override
    public FastAlphabet<I> reversed() {
        return new FastAlphabet<I>() {

            @Override
            public boolean containsSymbol(I symbol) {
                return FastAlphabet.this.containsSymbol(symbol);
            }

            @Override
            public I getSymbol(int index) {
                return FastAlphabet.this.getSymbol(FastAlphabet.this.size() - 1 - index);
            }

            @Override
            public int getSymbolIndex(I symbol) {
                return FastAlphabet.this.size() - 1 - FastAlphabet.this.getSymbolIndex(symbol);
            }

            @Override
            public int size() {
                return FastAlphabet.this.size();
            }

            @Override
            public int compare(I o1, I o2) {
                return FastAlphabet.this.compare(o2, o1);
            }
        };
    }

}
