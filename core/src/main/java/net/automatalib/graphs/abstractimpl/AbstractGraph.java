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
package net.automatalib.graphs.abstractimpl;

import java.util.Iterator;

import net.automatalib.graphs.Graph;


public abstract class AbstractGraph<N, E> extends AbstractIndefiniteGraph<N,E> implements Graph<N, E> {
	
	/**
	 * Provides a realization for {@link Graph#iterator()} relying on
	 * {@link Graph#getNodes()}.
	 * @see Graph#iterator()
	 */
	public static <N,E> Iterator<N> iterator(Graph<N,E> $this) {
		return $this.getNodes().iterator();
	}
	
	/**
	 * Provides a realization for {@link Graph#size()} relying on
	 * {@link Graph#getNodes()}.
	 * @see Graph#size()
	 */
	public static <N,E> int size(Graph<N,E> $this) {
		return $this.getNodes().size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<N> iterator() {
		return iterator(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.Graph#size()
	 */
	@Override
	public int size() {
		return size(this);
	}


}
