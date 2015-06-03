/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
