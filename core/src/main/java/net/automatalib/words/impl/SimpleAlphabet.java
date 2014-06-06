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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.abstractimpl.AbstractAlphabet;


/**
 * A simple alphabet implementation, that does not impose any restriction on the input
 * symbol class. However, the id lookup for a symbol might be slightly slower.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 */
public class SimpleAlphabet<I> extends AbstractAlphabet<I> implements GrowingAlphabet<I> {
	
	@Nonnull
	private final List<I> symbols = new ArrayList<I>();
	
	@Nonnull
	private final Map<I,Integer> indexMap = new HashMap<I,Integer>();
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(I a) {
		int s = size();
		int idx = addSymbol(a);
		if(idx != s)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.GrowingAlphabet#addSymbol(java.lang.Object)
	 */
	@Override
	public int addSymbol(I a) {
		Integer idx = indexMap.get(a);
		if(idx != null)
			return idx;
		idx = size();
		symbols.add(a);
		indexMap.put(a, idx);
		return idx;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public Iterator<I> iterator() {
		return Collections.unmodifiableList(symbols).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return symbols.size();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbol(int)
	 */
	@Override
	public I getSymbol(int index) {
		return symbols.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.words.Alphabet#getSymbolIndex(java.lang.Object)
	 */
	@Override
	public int getSymbolIndex(I symbol) {
		return indexMap.get(symbol);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public I get(int index) {
		return getSymbol(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(I o1, I o2) {
		return indexMap.get(o1) - indexMap.get(o2);
	}

}
