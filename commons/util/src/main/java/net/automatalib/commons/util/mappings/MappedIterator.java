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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.commons.util.mappings;

import java.util.Iterator;

/**
 * A transparent iterator wrapper that applies a {@link Mapping} to each
 * element returned by an underlying iterator.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 * @param <D> domain (original iterator type) class.
 * @param <R> range (resulting iterator type) class.
 */
final class MappedIterator<D, R> implements Iterator<R> {
	
	// the mapping to apply
	private final Mapping<? super D,R> mapping;
	// the underlying iterator
	private final Iterator<? extends D> baseIt;
	
	/**
	 * Constructor.
	 * @param mapping the mapping to apply.
	 * @param baseIt the underlying iterator.
	 */
	public MappedIterator(Mapping<? super D,R> mapping, Iterator<? extends D> baseIt) {
		this.mapping = mapping;
		this.baseIt = baseIt;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return baseIt.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public R next() {
		return mapping.get(baseIt.next());
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		baseIt.remove();
	}

}
