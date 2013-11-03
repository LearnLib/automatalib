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
