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

public abstract class SimplifiedIterator<E> implements Iterator<E> {
	
	private static enum State {
		AWAIT_NEXT,
		HAS_NEXT,
		FINISHED
	}
	
	private State state = State.AWAIT_NEXT;
	protected E nextValue = null;
	
	protected abstract boolean calculateNext();
	
	private boolean advance() {
		boolean ret = calculateNext();
		if(!ret) {
			state = State.FINISHED;
		}
		else {
			state = State.HAS_NEXT;
		}
		return ret;
	}

	@Override
	public boolean hasNext() {
		switch(state) {
		case AWAIT_NEXT:
			return advance();
		case HAS_NEXT:
			return true;
		default: // case FINISHED:
			return false;
		}
	}

	@Override
	public E next() {
		if(!hasNext())
			throw new NoSuchElementException();
		state = State.AWAIT_NEXT;
		return nextValue;
	}

}
