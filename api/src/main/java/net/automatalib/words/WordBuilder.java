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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.words;

import java.util.AbstractList;
import java.util.List;

import net.automatalib.commons.util.array.SimpleResizingArray;

/**
 * A class for dynamically building {@link Word}s.
 * 
 * As {@link Word}s are - like strings - immutable objects, constructing them by subsequent
 * invocations of {@link Word#concat(Word...)} etc. is highly inefficient. This class provides an
 * efficient means of construction by operating on an internal storage during construction,
 * only creating a {@link Word} (and thus requiring to ensure immutability) when the method {@link #toWord()}
 * (or {@link #toWord(int, int)} is invoked.
 * 
 * Note that due to the specifics of the underlying word implementation, even after an invocation
 * of {@link #toWord()} the storage does not have to be duplicated unless it either is required
 * due to capacity adjustment <i>or</i> a non-appending change (such as {@link #setSymbol(int, Object)}
 * or {@link #truncate(int)}) is made.
 * 
 * Nearly all modification methods of this class return a <tt>this</tt>-reference, allowing constructs
 * such as
 * <pre>builder.append(foo).append(bar).append(baz);</pre>
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> symbol class.
 */
public final class WordBuilder<I> extends AbstractList<I> {
	
	private final SimpleResizingArray storage;
	private int length;
	private boolean lock = true;
	
	/**
	 * Constructor. Initializes the builder with a default capacity.
	 */
	public WordBuilder() {
		this.storage = new SimpleResizingArray();
	}
	
	/**
	 * Constructor. Initializes the builder with the specified initial capacity.
	 * @param initialCapacity the initial capacity of the internal storage.
	 */
	public WordBuilder(int initialCapacity) {
		this.storage = new SimpleResizingArray(initialCapacity);
	}
	
	/**
	 * Constructor. Initializes the builder with a sequence of <tt>count</tt>
	 * times the specified symbol. Note that this constructor runs in constant time
	 * if <tt>initSym</tt> is <tt>null</tt>. 
	 * 
	 * @param initSym the initial symbol
	 * @param count the initial symbol count
	 */
	public WordBuilder(I initSym, int count) {
		this.storage = new SimpleResizingArray(count);
		if(initSym != null) {
			for(int i = 0; i < count; i++)
				storage.array[i] = initSym;
		}
		length = count;
	}
	
	/**
	 * Constructor. Initializes the builder with a sequence of <tt>count</tt>
	 * times the specified symbol, while allocating the specified initial capacity.
	 * @param capacity the initial capacity of the internal storage.
	 * @param initSym the initial symbol
	 * @param count the initial symbol count
	 */
	public WordBuilder(int capacity, I initSym, int count) {
		if(capacity < count)
			capacity = count;
		this.storage = new SimpleResizingArray(capacity);
		if(initSym != null) {
			for(int i = 0; i < count; i++)
				storage.array[i] = initSym;
		}
		length = count;
	}
	
	/**
	 * Constructor. Initializes the builder with a given word.
	 * @param init the word to initialize the builder with.
	 */
	public WordBuilder(Word<I> init) {
		int wLen = init.length();
		this.storage = new SimpleResizingArray(wLen);
		init.writeToArray(0, storage.array, 0, wLen);
		length = wLen;
	}
	
	/**
	 * Constructor. Initializes the builder with a given word, while allocating
	 * the specified initial capacity.
	 * @param capacity the initial capacity to use.
	 * @param init the initial word
	 */
	public WordBuilder(int capacity, Word<I> init) {
		int wLen = init.length();
		if(capacity < wLen)
			capacity = wLen;
		this.storage = new SimpleResizingArray(capacity);
		init.writeToArray(0, storage.array, 0, wLen);
		length = wLen;
	}
	
	public WordBuilder<I> append(List<? extends I> symList) {
		int lLen = symList.size();
		ensureAdditionalCapacity(lLen);
		for(I sym : symList)
			storage.array[length++] = sym;
		return this;
	}
	
	
	/**
	 * Appends a word to the contents of the internal storage.
	 * @param word the word to append.
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> append(Word<? extends I> word) {
		int wLen = word.length();
		ensureAdditionalCapacity(wLen);
		word.writeToArray(0, storage.array, length, wLen);
		length += wLen;
		return this;
	}
	
	/**
	 * Appends several words to the contents of the internal storage.
	 * @param words the words to append
	 * @return <tt>this</tt>
	 */
	@SafeVarargs
	public final WordBuilder<I> append(Word<? extends I> ...words) {
		if(words.length == 0)
			return this;
		
		int allLen = 0;
		for(int i = 0; i < words.length; i++)
			allLen += words[i].length();
		
		ensureAdditionalCapacity(allLen);
		
		for(int i = 0; i < words.length; i++) {
			Word<? extends I> word = words[i];
			int wLen = word.length();
			word.writeToArray(0, storage.array, length, wLen);
			length += wLen;
		}
		
		return this;
	}
	
	/**
	 * Appends <tt>num</tt> copies of the given word to the contents
	 * of the initial storage.
	 * @param num the number of copies
	 * @param word the word
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> repeatAppend(int num, Word<I> word) {
		if(num == 0)
			return this;
		
		int wLen = word.length();
		int allLen = wLen * num;
		
		ensureAdditionalCapacity(allLen);
		
		while(num-- > 0) {
			word.writeToArray(0, storage.array, length, wLen);
			length += wLen;
		}
		
		return this;
	}
	
	/**
	 * Appends a symbol to the contents of the internal storage.
	 * @param symbol the symbol to append
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> append(I symbol) {
		ensureAdditionalCapacity(1);
		storage.array[length++] = symbol;
		return this;
	}
	
	/**
	 * Appends <tt>num</tt> copies of a symbol to the contents of the
	 * internal storage.
	 * @param num the number of copies
	 * @param symbol the symbol
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> repeatAppend(int num, I symbol) {
		if(num == 0)
			return this;
		
		ensureAdditionalCapacity(num);
		if(symbol == null)
			length += num;
		else {
			while(num-- > 0)
				storage.array[length++] = symbol;
		}
		return this;
	}
	
	/**
	 * Appends several symbols to the contents of the internal storage.
	 * @param symbols the symbols to append
	 * @return <tt>this</tt>
	 */
	@SafeVarargs
	public final WordBuilder<I> append(I ...symbols) {
		if(symbols.length == 0)
			return this;
		ensureAdditionalCapacity(symbols.length);
		System.arraycopy(symbols, 0, storage.array, length, symbols.length);
		length += symbols.length;
		return this;
	}
	
	/**
	 * Ensures that the internal storage has in total the given capacity
	 * @param cap the minimum capacity to ensure
	 */
	public void ensureCapacity(int cap) {
		if(storage.ensureCapacity(cap))
			lock = false;
	}
	
	/**
	 * Ensures that the internal storage has <b>additionally</b> the given
	 * capacity.
	 * @param add the additional capacity to ensure
	 */
	public void ensureAdditionalCapacity(int add) {
		if(storage.ensureCapacity(length + add))
			lock = false;
	}
	
	/*
	 * Ensure that non-appending modifications may be made
	 */
	private void ensureUnlocked() {
		if(lock) {
			storage.array = storage.array.clone();
			lock = false;
		}
	}
	
	/**
	 * Retrieves the symbol at the given index
	 * @param index the index to retrieve
	 * @return the symbol at the given index
	 */
	@SuppressWarnings("unchecked")
	public I getSymbol(int index) {
		return (I)storage.array[index];
	}
	
	/**
	 * Sets the symbol at the given index. Note that this index must exist.
	 * @param index the index to manipulate
	 * @param symbol the symbol to set
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> setSymbol(int index, I symbol) {
		ensureUnlocked();
		storage.array[index] = symbol;
		return this;
	}
	
	/**
	 * Truncates the contents of the initial storage to the given length.
	 * @param truncLen the length to truncate to
	 * @return <tt>this</tt>
	 */
	public WordBuilder<I> truncate(int truncLen) {
		if(truncLen >= length)
			return this;
		
		ensureUnlocked();
		for(int i = truncLen; i < length; i++)
			storage.array[i] = null;
		
		length = truncLen;
		
		return this;
	}
	
	/**
	 * Creates a word from the given range of the contents of the internal storage.
	 * Note that the storage management mechanisms of this class guarantee that
	 * the returned word will not change regardless of what further operations are invoked
	 * on this {@link WordBuilder}.
	 * @param fromIndex the starting index
	 * @param toIndex the end index
	 * @return the word for the specified subrange
	 */
	public Word<I> toWord(int fromIndex, int toIndex) {
		if(fromIndex < 0 || toIndex > length)
			throw new IndexOutOfBoundsException();
		int len = toIndex - fromIndex;
		
		lock = true;
		return new SharedWord<>(storage.array, fromIndex, len);
	}
	
	/**
	 * Creates a word from the contents of the internal storage.
	 * Note that the storage management mechanisms of this class guarantee that
	 * the returned word will not change regardless of what further operations are performed
	 * on this {@link WordBuilder}.
	 * @return the internal contents as a word
	 */
	public Word<I> toWord() {
		lock = true;
		return new SharedWord<>(storage.array, 0, length);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#add(java.lang.Object)
	 */
	@Override
	public boolean add(I e) {
		append(e);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public I get(int index) {
		return getSymbol(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 */
	@Override
	public I set(int index, I element) {
		I old = getSymbol(index);
		setSymbol(index, element);
		return old;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractList#clear()
	 */
	@Override
	public void clear() {
		ensureUnlocked();
		for(int i = 0; i < length; i++)
			storage.array[i] = null;
		length = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return length;
	}
	
	
}
