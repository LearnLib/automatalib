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
package net.automatalib.commons.util.mappings;

/**
 * Mapping realized by the composition of two mappings.
 * 
 * @author Malte Isberner 
 *
 * @param <D> domain class of the first (and final) mapping.
 * @param <I> intermediate object class, i.e. range of the first
 * and domain of the second.
 * @param <R> range class of the second (and final) mapping.
 */
final class MappingComposition<D, I, R> implements Mapping<D,R> {
	private final Mapping<D, ? extends I> first;
	private final Mapping<? super I, R> second;
	
	/**
	 * Constructor.
	 * @param first first mapping (defines domain).
	 * @param second second mapping (defines range).
	 */
	public MappingComposition(Mapping<D, ? extends I> first, Mapping<? super I, R> second) {
		this.first = first;
		this.second = second;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.misc.util.Mapping#get(java.lang.Object)
	 */
	@Override
	public R get(D elem) {
		I i = first.get(elem);
		return second.get(i);
	}
	
}
