package net.automatalib.words.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public class IntAlphabet implements Alphabet<Integer> {
	
	private final int low;  // INCLUSIVE
	private final int high; // EXCLUSIVE

	public IntAlphabet(int from, int to) {
		this.low = from;
		this.high = to + 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return (high - low);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return (high == low);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		if(o == null || o.getClass() == Integer.class)
			return false;
		int i = ((Integer)o).intValue();
		
		if(i < low || i >= high)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<Integer> iterator() {
		return CollectionsUtil.rangeList(low, high).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		Object[] arr = new Object[high - low];
		
		for(int i = 0, x = low; x < high; i++, x++)
			arr[i] = x;
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = high - low;
		if(a.length < size)
			a = Arrays.copyOf(a, size);
		
		for(int i = 0, x = low; x < high; i++, x++)
			a[i] = (T)Integer.valueOf(x);
		return a;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(Integer e) {
		throw new UnsupportedOperationException("IntAlphabet is immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("IntAlphabet is immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		for(Object o : c) {
			if(!contains(o))
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		throw new UnsupportedOperationException("IntAlphabet is immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("IntAlphabet is immutable"); 
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("IntAlphabet is immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("IntAlphabet is immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Integer o1, Integer o2) {
		return o1.compareTo(o2);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Alphabet#getSymbol(int)
	 */
	@Override
	public Integer getSymbol(int index) throws IllegalArgumentException {
		int val = low + index;
		if(val < low || val >= high)
			throw new IndexOutOfBoundsException("Invalid index " + index);
		return Integer.valueOf(low + index);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Alphabet#getSymbolIndex(java.lang.Object)
	 */
	@Override
	public int getSymbolIndex(Integer symbol) throws IllegalArgumentException {
		return symbol.intValue() - low;
	}

}
