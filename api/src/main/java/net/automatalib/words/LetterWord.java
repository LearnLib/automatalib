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
package net.automatalib.words;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * A word consisting of a single letter only.
 * 
 * @author Malte Isberner 
 *
 * @param <I> symbol class
 * @see Collections#singletonList(Object)
 */
final class LetterWord<I> extends Word<I> {
	
	/*
	 * Iterator
	 */
	private static final class Iterator<I> implements java.util.Iterator<I> {
		
		private final I letter;
		private boolean next = true;
		
		public Iterator(I letter) {
			this.letter = letter;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return next;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public I next() {
			if(next) {
				next = false;
				return letter;
			}
			throw new NoSuchElementException();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private final I letter;
	
	/**
	 * Constructor.
	 * @param letter the letter to represent as a word
	 */
	public LetterWord(I letter) {
		this.letter = letter;
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#getSymbol(int)
	 */
	@Override
	public I getSymbol(int index) {
		if(index != 0)
			throw new IndexOutOfBoundsException();
		return letter;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#length()
	 */
	@Override
	public int length() {
		return 1;
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#subWord(int, int)
	 */
	@Override
	public Word<I> _subWord(int fromIndex, int toIndex) {
		if(fromIndex > 0 || toIndex == 0)
			return Word.epsilon();
		return this;
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#iterator()
	 */
	@Override
	public java.util.Iterator<I> iterator() {
		return new Iterator<>(letter);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#writeToArray(int, java.lang.Object[], int, int)
	 */
	@Override
	public void writeToArray(int offset, Object[] array, int tgtOffset,
			int length) {
		if(offset == 0 && length > 0)
			array[tgtOffset] = letter;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#lastSymbol()
	 */
	@Override
	public I lastSymbol() {
		return letter;
	}




	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#asList()
	 */
	@Override
	public List<I> asList() {
		return Collections.singletonList(letter);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#prepend(java.lang.Object)
	 */
	@Override
	public Word<I> prepend(I symbol) {
		Object[] array = new Object[]{symbol, letter};
		return new SharedWord<>(array);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#append(java.lang.Object)
	 */
	@Override
	public Word<I> append(I symbol) {
		Object[] array = new Object[]{letter, symbol};
		return new SharedWord<>(array);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#isPrefixOf(net.automatalib.words.Word)
	 */
	@Override
	public boolean isPrefixOf(Word<?> other) {
		if(other.isEmpty())
			return false;
		return Objects.equals(letter, other.getSymbol(0));
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#longestCommonPrefix(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> longestCommonPrefix(Word<?> other) {
		if(isPrefixOf(other)) {
			return this;
		}
		return Word.epsilon();
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#isSuffixOf(net.automatalib.words.Word)
	 */
	@Override
	public boolean isSuffixOf(Word<?> other) {
		if(other.isEmpty())
			return false;
		return Objects.equals(letter, other.lastSymbol());
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#longestCommonSuffix(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> longestCommonSuffix(Word<?> other) {
		if(isSuffixOf(other))
			return this;
		return Word.epsilon();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.words.Word#flatten()
	 */
	@Override
	public Word<I> flatten() {
		return this;
	}


	/* (non-Javadoc)
	 * @see net.automatalib.words.Word#trimmed()
	 */
	@Override
	public Word<I> trimmed() {
		return this;
	}

	@Nonnull
	@Override
	public <T> Word<T> transform(Function<? super I, ? extends T> transformer) {
		T transformed = transformer.apply(letter);
		return new LetterWord<>(transformed);
	}
	
	
	@Override
	@Nonnull
	public Spliterator<I> spliterator() {
		return Collections.singleton(letter).spliterator();
	}
	
}
