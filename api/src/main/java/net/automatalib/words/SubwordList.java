/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.words;

import java.util.AbstractList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class SubwordList<I> extends AbstractList<Word<I>> {
	
	private final Word<I> word;
	private final boolean reverse;
	private final boolean prefix;
	
	public SubwordList(Word<I> word, boolean prefix, boolean reverse) {
		this.word = word;
		this.prefix = prefix;
		this.reverse = reverse;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	@Nonnull
	public Word<I> get(int index) {
		if(index < 0 || index > word.length())
			throw new IndexOutOfBoundsException();
		if(reverse)
			index = word.length() - index;
		if(prefix)
			return word.prefix(index);
		return word.suffix(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return word.length() + 1;
	}

}
