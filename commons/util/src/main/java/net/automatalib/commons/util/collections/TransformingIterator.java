package net.automatalib.commons.util.collections;

import java.util.Iterator;

public abstract class TransformingIterator<I, E> implements Iterator<E> {
	
	private final Iterator<? extends I> iterator;
	
	protected abstract E transform(I internal);

	public TransformingIterator(Iterator<? extends I> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		I internal = iterator.next();
		return transform(internal);
	}

	@Override
	public void remove() {
		iterator.remove();
	}

}
