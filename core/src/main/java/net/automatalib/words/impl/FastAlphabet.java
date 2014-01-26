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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.automatalib.commons.util.nid.DynamicList;
import net.automatalib.commons.util.nid.MutableNumericID;
import net.automatalib.words.GrowingAlphabet;

/**
 * A fast alphabet implementation, that assumes identifiers are stored directly in the
 * input symbols.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <I> input symbol class.
 */
public class FastAlphabet<I extends MutableNumericID> extends DynamicList<I>
		implements GrowingAlphabet<I> {

	
	public FastAlphabet() {
		
	}
	
	public FastAlphabet(List<? extends I> symbols) {
		for(I sym : symbols)
			addSymbol(sym);
	}
	
	@SafeVarargs
	public FastAlphabet(I ...symbols) {
		this(Arrays.asList(symbols));
	}
	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbol(int)
	 */
	@Override
	@Nonnull
	public I getSymbol(int index) {
		return get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbolIndex(java.lang.Object)
	 */
	@Override
	public int getSymbolIndex(@Nonnull I symbol) {
		return symbol.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.GrowingAlphabet#addSymbol(java.lang.Object)
	 */
	@Override
	public int addSymbol(@Nonnull I a) {
		add(a);
		return a.getId();
	}

	@Override
	public int compare(@Nonnull I o1, @Nonnull I o2) {
		return o1.getId() - o2.getId();
	}


}
