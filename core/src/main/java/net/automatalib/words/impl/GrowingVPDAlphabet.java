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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.words.abstractimpl.AbstractVPDAlphabet;

/**
 * A {@link net.automatalib.words.VPDAlphabet} implementation that allows to add new symbols after its construction.
 * Wraps input symbols in a {@link VPDSym} instance to allow faster mappings from symbols to indexes.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class GrowingVPDAlphabet<I> extends AbstractVPDAlphabet<VPDSym<I>> {

    private final List<VPDSym<I>> allSyms = new ArrayList<>();
    private final List<VPDSym<I>> callSyms = new ArrayList<>();
    private final List<VPDSym<I>> returnSyms = new ArrayList<>();
    private final List<VPDSym<I>> internalSyms = new ArrayList<>();

    @Override
    public VPDSym<I> getSymbol(final int index) throws IllegalArgumentException {
        return allSyms.get(index);
    }

    @Override
    public int getSymbolIndex(final VPDSym<I> symbol) throws IllegalArgumentException {
        if (!hasValidIndex(symbol, allSyms)) {
            throw new IllegalArgumentException();
        }
        return symbol.getGlobalIndex();
    }

    @Override
    public VPDSym<I> getCallSymbol(final int index) throws IllegalArgumentException {
        return callSyms.get(index);
    }

    @Override
    public int getCallSymbolIndex(final VPDSym<I> symbol) throws IllegalArgumentException {
        if (symbol.getType() != SymbolType.CALL || !hasValidIndex(symbol, callSyms)) {
            throw new IllegalArgumentException();
        }
        return symbol.getLocalIndex();
    }

    @Override
    public Collection<VPDSym<I>> getCallSymbols() {
        return callSyms;
    }

    @Override
    public VPDSym<I> getInternalSymbol(final int index) throws IllegalArgumentException {
        return internalSyms.get(index);
    }

    @Override
    public int getInternalSymbolIndex(final VPDSym<I> symbol) throws IllegalArgumentException {
        if (symbol.getType() != SymbolType.INTERNAL || !hasValidIndex(symbol, internalSyms)) {
            throw new IllegalArgumentException();
        }
        return symbol.getLocalIndex();
    }

    @Override
    public Collection<VPDSym<I>> getInternalSymbols() {
        return internalSyms;
    }

    @Override
    public VPDSym<I> getReturnSymbol(final int index) throws IllegalArgumentException {
        return returnSyms.get(index);
    }

    @Override
    public int getReturnSymbolIndex(final VPDSym<I> symbol) throws IllegalArgumentException {
        if (symbol.getType() != SymbolType.RETURN || !hasValidIndex(symbol, returnSyms)) {
            throw new IllegalArgumentException();
        }
        return symbol.getLocalIndex();
    }

    @Override
    public Collection<VPDSym<I>> getReturnSymbols() {
        return returnSyms;
    }

    @Override
    public int getNumCalls() {
        return callSyms.size();
    }

    @Override
    public int getNumInternals() {
        return internalSyms.size();
    }

    @Override
    public int getNumReturns() {
        return returnSyms.size();
    }

    @Override
    public SymbolType getSymbolType(final VPDSym<I> symbol) {
        return symbol.getType();
    }

    @Override
    public int size() {
        return allSyms.size();
    }

    public VPDSym<I> addNewSymbol(final I userObject, final SymbolType type) {
        final List<VPDSym<I>> localList;
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

        final VPDSym<I> vpdSym = new VPDSym<>(userObject, type, localList.size(), allSyms.size());
        allSyms.add(vpdSym);
        localList.add(vpdSym);

        return vpdSym;
    }

    private boolean hasValidIndex(VPDSym<I> symbol, List<VPDSym<I>> localSymbols) {
        final int localIdx = symbol.getLocalIndex();
        final int globalIdx = symbol.getGlobalIndex();

        return localIdx >= 0 && localIdx < localSymbols.size() && globalIdx >= 0 && globalIdx < allSyms.size();

    }

}
