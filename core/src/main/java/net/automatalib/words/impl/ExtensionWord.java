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
package net.automatalib.words.impl;

import java.util.NoSuchElementException;
import java.util.Objects;

import net.automatalib.words.Word;

public class ExtensionWord<I> extends Word<I> {
	
	private static final class Iterator<I> implements java.util.Iterator<I> {
		private final java.util.Iterator<I> wordIt;
		private final I letter;
		private boolean next = true;
		
		public Iterator(java.util.Iterator<I> wordIt, I letter) {
			this.wordIt = wordIt;
			this.letter = letter;
		}
		
		@Override
		public boolean hasNext() {
			return next;
		}
		@Override
		public I next() {
			if(wordIt.hasNext())
				return wordIt.next();
			if(!next)
				throw new NoSuchElementException();
			next = false;
			return letter;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		
	}
	
	private final Word<I> word;
	private final I letter;
	
	public ExtensionWord(Word<I> word, I letter) {
		this.word = word;
		this.letter = letter;
	}

	@Override
	public I getSymbol(int index) {
		if(index == word.length())
			return letter;
		return word.getSymbol(index);
	}

	@Override
	public int length() {
		return word.length() + 1;
	}

	@Override
	protected Word<I> _subWord(int fromIndex, int toIndex) {
		int wLen = word.length();
		if(fromIndex < wLen) {
			if(toIndex <= wLen) {
				return word.subWord(fromIndex, toIndex);
			}
			return new ExtensionWord<>(word.subWord(fromIndex, wLen), letter);
		}
		else if(fromIndex == wLen) {
			return Word.fromLetter(letter);
		}
		return Word.epsilon();
	}

	@Override
	public java.util.Iterator<I> iterator() {
		return new Iterator<I>(word.iterator(), letter);
	}

	@Override
	public void writeToArray(int offset, Object[] array, int tgtOffset,
			int length) {
		int wordLen = word.length();
		boolean writeLetter = (offset + length > wordLen);
		if(offset < wordLen) {
			if(writeLetter)
				length--;
			word.writeToArray(offset, array, tgtOffset, length);
		}
		if(writeLetter)
			array[tgtOffset+length] = letter;
	}

	@Override
	public Word<I> prepend(I symbol) {
		return new ExtensionWord<>(word.prepend(symbol), letter);
	}

	@Override
	public boolean isPrefixOf(Word<?> other) {
		int wordLen = word.length();
		if(wordLen >= other.length())
			return false;
		
		if(!word.isPrefixOf(other))
			return false;
		return Objects.equals(other.getSymbol(wordLen), letter);
	}

}
