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
package net.automatalib.commons.util.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class TwoLevelIterator<L1, L2, O> implements Iterator<O> {
	
	private final Iterator<L1> l1Iterator;
	private L1 l1Object;
	private Iterator<L2> l2Iterator;
	
	public TwoLevelIterator(Iterator<L1> l1Iterator) {
		this.l1Iterator = l1Iterator;
		this.l2Iterator = null;
	}
	
	protected abstract Iterator<L2> l2Iterator(L1 l1Object);
	
	protected abstract O combine(L1 l1Object, L2 l2Object);
	
	protected void nextL1() {
		this.l2Iterator = null;
	}
	
	protected boolean advance() {
		while(l2Iterator == null || !l2Iterator.hasNext()) {
			if(!l1Iterator.hasNext())
				return false;
			this.l1Object = l1Iterator.next();
			this.l2Iterator = l2Iterator(this.l1Object);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		if(l2Iterator != null && l2Iterator.hasNext())
			return true;
		return advance();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public O next() {
		if(l2Iterator == null || !l2Iterator.hasNext()) {
			if(!advance())
				throw new NoSuchElementException();
		}
		return combine(l1Object, l2Iterator.next());	
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		l2Iterator.remove();
	}
	
	
	
}
