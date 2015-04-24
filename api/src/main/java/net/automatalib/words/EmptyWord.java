/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.words;

import com.google.common.base.Function;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The empty word.
 * <p>
 * This class has no type parameter, as there are no non-<tt>null</tt> instances
 * of the symbol class involved. Hence, Java's generic mechanism allows to maintain
 * only a single instance of this class. 
 * 
 * @author Malte Isberner
 *
 * @see Collections#emptyList()
 */
@ParametersAreNonnullByDefault
final class EmptyWord extends Word<Object> {
	
	public static final EmptyWord INSTANCE
		= new EmptyWord();
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#getSymbol(int)
	 */
	@Override
	public Object getSymbol(int index) {
		throw new IndexOutOfBoundsException(Integer.toString(index));
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#length()
	 */
	@Override
	public int length() {
		return 0;
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#subWord(int, int)
	 */
	@Override
	public Word<Object> _subWord(int fromIndex, int toIndex) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOffset,
			int length) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#asList()
	 */
	@Override
	public List<Object> asList() {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#canonicalNext(net.automatalib.words.Alphabet)
	 */
	@Override
	public Word<Object> canonicalNext(Alphabet<Object> sigma) {
		return new LetterWord<>(sigma.getSymbol(0));
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#append(java.lang.Object)
	 */
	@Override
	public Word<Object> append(Object symbol) {
		return new LetterWord<>(symbol);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#prepend(I[])
	 */
	@Override
	public Word<Object> prepend(Object symbol) {
		return append(symbol);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#isPrefixOf(net.automatalib.words.Word)
	 */
	@Override
	public boolean isPrefixOf(Word<?> other) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#longestCommonPrefix(net.automatalib.words.Word)
	 */
	@Override
	public Word<Object> longestCommonPrefix(Word<?> other) {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#isSuffixOf(net.automatalib.words.Word)
	 */
	@Override
	public boolean isSuffixOf(Word<?> other) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#longestCommonSuffix(net.automatalib.words.Word)
	 */
	@Override
	public Word<Object> longestCommonSuffix(Word<?> other) {
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#lastSymbol()
	 */
	@Override
	public Object lastSymbol() {
		throw new NoSuchElementException();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#flatten()
	 */
	@Override
	public Word<Object> flatten() {
		return this;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.words.Word#trimmed()
	 */
	@Override
	public Word<Object> trimmed() {
		return this;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> Word<T> transform(Function<? super Object,? extends T> transformer) {
		return (Word<T>)this;
	}
}
