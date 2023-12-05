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
package net.automatalib.alphabet.impl;

import java.util.AbstractList;

import net.automatalib.alphabet.Alphabet;

public abstract class AbstractAlphabet<I> extends AbstractList<I> implements Alphabet<I> {

    @Override
    public I get(int index) {
        return getSymbol(index);
    }

    /*
     * This alphabet-specific view is required by the SequencedCollection changes introduced in JDK21,
     * See https://openjdk.org/jeps/431 for more information.
     */
    @Override
    public AbstractAlphabet<I> reversed() {
        return new AbstractAlphabet<I>() {

            @Override
            public boolean containsSymbol(I symbol) {
                return AbstractAlphabet.this.containsSymbol(symbol);
            }

            @Override
            public I getSymbol(int index) {
                return AbstractAlphabet.this.getSymbol(AbstractAlphabet.this.size() - 1 - index);
            }

            @Override
            public int getSymbolIndex(I symbol) {
                return AbstractAlphabet.this.size() - 1 - AbstractAlphabet.this.getSymbolIndex(symbol);
            }

            @Override
            public int size() {
                return AbstractAlphabet.this.size();
            }

            @Override
            public int compare(I o1, I o2) {
                return AbstractAlphabet.this.compare(o2, o1);
            }
        };
    }
}
