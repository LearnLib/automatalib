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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.abstractimpl.AbstractVPDAlphabet;

/**
 * A list-based, fixed size implementation of a {@link net.automatalib.words.VPDAlphabet}.
 *
 * @author frohme
 */
public class DefaultVPDAlphabet<I> extends AbstractVPDAlphabet<I> {

    private final int internalStart, internalEnd;
    private final int callStart, callEnd;
    private final int returnStart, returnEnd;

    private final List<I> symbols;

    public DefaultVPDAlphabet(final Collection<I> internalSymbols,
                              final Collection<I> callSymbols,
                              final Collection<I> returnSymbols) {

        validateDisjointness(internalSymbols, SymbolType.INTERNAL, callSymbols, returnSymbols);
        validateDisjointness(callSymbols, SymbolType.CALL, returnSymbols);

        internalStart = 0;
        internalEnd = internalSymbols.size();

        callStart = internalEnd;
        callEnd = callStart + callSymbols.size();

        returnStart = callEnd;
        returnEnd = returnStart + returnSymbols.size();

        final List<I> tmp = new ArrayList<>(returnEnd);
        tmp.addAll(internalSymbols);
        tmp.addAll(callSymbols);
        tmp.addAll(returnSymbols);

        this.symbols = Collections.unmodifiableList(tmp);
    }

    @Override
    public I getCallSymbol(int index) throws IllegalArgumentException {
        final int idx = index + callStart;

        validateIndex(idx, callStart, callEnd, "Index not within its expected bounds");

        return this.getSymbol(idx);
    }

    @Override
    public int getCallSymbolIndex(final I symbol) throws IllegalArgumentException {
        final int idx = this.getSymbolIndex(symbol);

        validateIndex(idx, callStart, callEnd, "Call alphabet does not contain the queried symbol");

        return idx - callStart;
    }

    @Override
    public Collection<I> getCallSymbols() {
        return this.symbols.subList(callStart, callEnd);
    }

    @Override
    public I getInternalSymbol(final int index) throws IllegalArgumentException {
        final int idx = index + internalStart;

        validateIndex(idx, internalStart, internalEnd, "Index not within its expected bounds");

        return this.getSymbol(idx);
    }

    @Override
    public int getInternalSymbolIndex(final I symbol) throws IllegalArgumentException {
        final int idx = this.getSymbolIndex(symbol);

        validateIndex(idx, internalStart, internalEnd, "Internal alphabet does not contain the queried symbol");

        return idx - internalStart;
    }

    @Override
    public Collection<I> getInternalSymbols() {
        return this.symbols.subList(internalStart, internalEnd);
    }

    @Override
    public I getReturnSymbol(final int index) throws IllegalArgumentException {
        final int idx = index + returnStart;

        validateIndex(idx, returnStart, returnEnd, "Index not within its expected bounds");

        return this.getSymbol(idx);
    }

    @Override
    public int getReturnSymbolIndex(final I symbol) throws IllegalArgumentException {
        final int idx = this.getSymbolIndex(symbol);

        validateIndex(idx, returnStart, returnEnd, "Return alphabet does not contain the queried symbol");

        return idx - returnStart;
    }

    @Override
    public Collection<I> getReturnSymbols() {
        return this.symbols.subList(returnStart, returnEnd);
    }

    @Override
    public int getNumCalls() {
        return callEnd - callStart;
    }

    @Override
    public int getNumInternals() {
        return internalEnd - internalStart;
    }

    @Override
    public int getNumReturns() {
        return returnEnd - returnStart;
    }

    @Override
    public SymbolType getSymbolType(I symbol) {
        final int idx = this.getSymbolIndex(symbol);

        if (idx < internalEnd) {
            return SymbolType.INTERNAL;
        } else if (idx < callEnd) {
            return SymbolType.CALL;
        } else {
            return SymbolType.RETURN;
        }
    }

    @Nullable
    @Override
    public I getSymbol(final int index) throws IllegalArgumentException {
        validateIndex(index, 0, symbols.size(), "Index not within its expected bounds");
        return this.symbols.get(index);
    }

    @Override
    public int getSymbolIndex(@Nullable final I symbol) throws IllegalArgumentException {
        final int localIdx = this.symbols.indexOf(symbol);

        if (localIdx < 0) {
            throw new IllegalArgumentException("Alphabet does not contain the queried symbol");
        }

        return localIdx;
    }

    @Override
    public int size() {
        return this.symbols.size();
    }

    private void validateIndex(int idx, int lowerBound, int upperBound, String errorMessage) {
        if (idx < lowerBound || idx >= upperBound) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @SafeVarargs
    private static <I> void validateDisjointness(Collection<I> source,
                                                 VPDAlphabet.SymbolType type,
                                                 Collection<I>... rest) {
        final Set<I> sourceAsSet = new HashSet<>(source);
        final int initialSize = sourceAsSet.size();

        for (Collection<I> c : rest) {
            sourceAsSet.removeAll(c);
        }

        if (sourceAsSet.size() < initialSize) {
            throw new IllegalArgumentException(
                    "The set of " + type + " symbols is not disjoint with the sets of other symbols.");
        }
    }
}
