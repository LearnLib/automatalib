/* Copyright (C) 2017 TU Dortmund
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
import java.util.List;

import javax.annotation.Nullable;

import net.automatalib.words.abstractimpl.AbstractVPDAlphabet;

/**
 * A list-based, fixed size implementation of a {@link net.automatalib.words.VPDAlphabet}.
 *
 * @author frohme
 */
public class DefaultVPDAlphabet<I> extends AbstractVPDAlphabet<I> {

	final int internalStart, internalEnd;
	final int callStart, callEnd;
	final int returnStart, returnEnd;

	final List<I> symbols;

	public DefaultVPDAlphabet(final Collection<I> internalSymbols,
							  final Collection<I> callSymbols,
							  final Collection<I> returnSymbols) {

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
	public SymbolType getSymbolType(I symbol) {
		final int idx = this.getSymbolIndex(symbol);

		if (idx < internalEnd) {
			return SymbolType.INTERNAL;
		}
		else if (idx < callEnd) {
			return SymbolType.CALL;
		}
		else {
			return SymbolType.RETURN;
		}
	}

	@Override
	public Collection<? extends I> getInternalSymbols() {
		return this.symbols.subList(internalStart, internalEnd);
	}

	@Override
	public Collection<? extends I> getCallSymbols() {
		return this.symbols.subList(callStart, callEnd);
	}

	@Override
	public Collection<? extends I> getReturnSymbols() {
		return this.symbols.subList(returnStart, returnEnd);
	}

	@Override
	public int getNumCalls() {
		return callEnd - callStart;
	}

	@Override
	public int getNumReturns() {
		return returnEnd - returnStart;
	}

	@Override
	public int getNumInternals() {
		return internalEnd - internalStart;
	}

	@Override
	public I getCallSymbol(int index) {
		return this.getSymbol(index + callStart);
	}

	@Override
	public int getCallSymbolIndex(final I symbol) {
		return this.getSymbolIndex(symbol) - callStart;
	}

	@Override
	public I getReturnSymbol(final int index) {
		return this.getSymbol(index + returnStart);
	}

	@Override
	public int getReturnSymbolIndex(final I symbol) {
		return this.getSymbolIndex(symbol) - returnStart;
	}

	@Override
	public I getInternalSymbol(final int index) {
		return this.getSymbol(index + internalStart);
	}

	@Override
	public int getInternalSymbolIndex(final I symbol) {
		return this.getSymbolIndex(symbol) - internalStart;
	}

	@Override
	public int size() {
		return this.symbols.size();
	}

	@Nullable
	@Override
	public I getSymbol(final int index) throws IllegalArgumentException {
		return this.symbols.get(index);
	}

	@Override
	public int getSymbolIndex(@Nullable final I symbol) throws IllegalArgumentException {
		return this.symbols.indexOf(symbol);
	}
}
