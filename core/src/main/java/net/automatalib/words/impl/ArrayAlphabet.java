/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.words.impl;

import java.util.Objects;

import net.automatalib.words.abstractimpl.AbstractAlphabet;

public class ArrayAlphabet<I> extends AbstractAlphabet<I> {

	protected final I[] symbols;
	
	@SafeVarargs
	public ArrayAlphabet(I ...symbols) {
		this.symbols = symbols;
	}

	@Override
	public I getSymbol(int index) throws IllegalArgumentException {
		return symbols[index];
	}

	@Override
	public int getSymbolIndex(I symbol) throws IllegalArgumentException {
		for(int i = 0; i < symbols.length; i++) {
			if(Objects.equals(symbols[i], symbol))
				return i;
		}
		return -1;
	}

	@Override
	public int size() {
		return symbols.length;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.words.abstractimpl.AbstractAlphabet#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		System.arraycopy(symbols, offset, array, tgtOfs, num);
	}
	
	@Override
	public boolean containsSymbol(I symbol) {
		return getSymbolIndex(symbol) != -1;
	}
	

}
