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
package net.automatalib.util.minimizer;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class that maps a {@link Collection} of states to a collection of
 * the respective original states.
 *  
 * @author Malte Isberner
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
class OriginalStateCollection<S> extends AbstractCollection<S> {
	
	private final Collection<? extends State<S,?>> stateColl;
	
	/**
	 * Constructor.
	 * @param stateColl the backing state collection.
	 */
	public OriginalStateCollection(Collection<? extends State<S,?>> stateColl) {
		this.stateColl = stateColl;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<S> iterator() {
		final Iterator<? extends State<S,?>> stateIt = stateColl.iterator();
		return new Iterator<S>() {
			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return stateIt.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public S next() {
				return stateIt.next().getOriginalState();
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not "
						+ "allowed on this collection!");
			}
			
		};
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return stateColl.size();
	}
}
