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
package net.automatalib.words.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.abstractimpl.AbstractAlphabet;

@ParametersAreNonnullByDefault
public class ListAlphabet<I> extends AbstractAlphabet<I> {

    @Nonnull
    private final List<? extends I> list;

    public ListAlphabet(List<? extends I> list) {
        this.list = list;
    }

    @Override
    public I getSymbol(int index) throws IllegalArgumentException {
        return list.get(index);
    }

    @Override
    public int getSymbolIndex(I symbol) throws IllegalArgumentException {
        int idx = list.indexOf(symbol);
        if (idx == -1) {
            throw new IllegalArgumentException("Symbol " + symbol + " is not contained in the alphabet");
        }
        return idx;
    }

    @Override
    public boolean containsSymbol(I symbol) {
        return list.contains(symbol);
    }

    @Override
    public I get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}
