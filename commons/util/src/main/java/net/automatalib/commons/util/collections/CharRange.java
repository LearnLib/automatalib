package net.automatalib.commons.util.collections;

import java.util.AbstractList;
import java.util.RandomAccess;

import net.automatalib.commons.util.array.ArrayWritable;

public class CharRange extends AbstractList<Character> implements
		ArrayWritable<Character>, RandomAccess {
	
	private final IntRange delegate;

	public CharRange(char low, char high) {
		this(low, 1, high);
	}
	
	public CharRange(char low, int step, char high) {
		this(new IntRange(low, step, high));
	}
	
	public CharRange(IntRange delegate) {
		this.delegate = delegate;
	}

	public char charGet(int index) {
		int i = delegate.intGet(index);
		return (char)i;
	}
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public Character get(int index) {
		return Character.valueOf(charGet(index));
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/* (non-Javadoc)
	 * @see net.automatalib.commons.util.array.ArrayWritable#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOfs, int num) {
		int si = offset;
		int ti = tgtOfs;
		for(int i = 0; i < num; i++)
			array[ti++] = charGet(si++);
	}
	
	public int indexOf(char c) {
		return delegate.indexOf(c);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		if(o == null || o.getClass() != Character.class)
			return -1;
		return indexOf(((Character)o).charValue());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#iterator()
	 */
	@Override
	public CharRangeIterator iterator() {
		return new CharRangeIterator(delegate.iterator());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator()
	 */
	@Override
	public CharRangeIterator listIterator() {
		return new CharRangeIterator(delegate.listIterator());
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#listIterator(int)
	 */
	@Override
	public CharRangeIterator listIterator(int index) {
		return new CharRangeIterator(delegate.listIterator(index));
	}

	
	public char charValue(int i) {
		return (char)delegate.intValue(i);
	}

}
