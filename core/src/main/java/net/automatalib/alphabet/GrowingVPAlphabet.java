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
package net.automatalib.alphabet;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link VPAlphabet} implementation that allows to add new symbols after its construction. Wraps input symbols in a
 * {@link VPSym} instance to allow faster mappings from symbols to indexes.
 *
 * @param <I>
 *         input symbol type
 */
public class GrowingVPAlphabet<I> extends AbstractVPAlphabet<VPSym<I>> implements VPAlphabet<VPSym<I>> {

    private final List<VPSym<I>> allSyms;
    private final List<VPSym<I>> callSyms;
    private final List<VPSym<I>> internalSyms;
    private final List<VPSym<I>> returnSyms;

    public GrowingVPAlphabet() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private GrowingVPAlphabet(List<VPSym<I>> internalSyms, List<VPSym<I>> callSyms, List<VPSym<I>> returnSyms) {
        super(new AlphabetView<>(internalSyms), new AlphabetView<>(callSyms), new AlphabetView<>(returnSyms));
        this.internalSyms = internalSyms;
        this.callSyms = callSyms;
        this.returnSyms = returnSyms;
        this.allSyms = new ArrayList<>();
    }

    public VPSym<I> addNewSymbol(I userObject, SymbolType type) {
        final List<VPSym<I>> localList;
        switch (type) {
            case CALL:
                localList = callSyms;
                break;
            case RETURN:
                localList = returnSyms;
                break;
            default:
                localList = internalSyms;
                break;
        }

        final VPSym<I> vpSym = new VPSym<>(userObject, type, localList.size(), allSyms.size());
        allSyms.add(vpSym);
        localList.add(vpSym);

        return vpSym;
    }

    @Override
    public SymbolType getSymbolType(VPSym<I> symbol) {
        return symbol.getType();
    }

    @Override
    public int size() {
        return allSyms.size();
    }

    @Override
    public VPSym<I> getSymbol(int index) {
        return allSyms.get(index);
    }

    @Override
    public int getSymbolIndex(VPSym<I> symbol) {
        if (!containsSymbol(symbol)) {
            throw new IllegalArgumentException();
        }

        return symbol.getGlobalIndex();
    }

    @Override
    public boolean containsSymbol(VPSym<I> symbol) {
        final int idx = symbol.getGlobalIndex();
        return idx < allSyms.size() && allSyms.get(idx) == symbol;
    }

    private static class AlphabetView<I> extends AbstractAlphabet<VPSym<I>> implements Alphabet<VPSym<I>> {

        private final List<VPSym<I>> list;

        AlphabetView(List<VPSym<I>> list) {
            this.list = list;
        }

        @Override
        public VPSym<I> getSymbol(int index) {
            return list.get(index);
        }

        @Override
        public int getSymbolIndex(VPSym<I> symbol) {
            if (!containsSymbol(symbol)) {
                throw new IllegalArgumentException();
            }

            return symbol.getLocalIndex();
        }

        @Override
        public boolean containsSymbol(VPSym<I> symbol) {
            final int idx = symbol.getLocalIndex();
            return idx < list.size() && list.get(idx) == symbol;
        }

        @Override
        public int size() {
            return list.size();
        }
    }
}
