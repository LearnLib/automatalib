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
package net.automatalib.util.tries;

import java.util.List;
import java.util.NoSuchElementException;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A node in a {@link SuffixTrie}.
 * 
 * @author Malte Isberner 
 *
 * @param <I> symbol class.
 */
public class SuffixTrieNode<I> extends Word<I> {
	
	/**
	 * Optimized iterator for the implicit word representation.
	 * 
	 * @author Malte Isberner 
	 *
	 * @param <I> symbol class
	 */
	private static final class Iterator<I> implements java.util.Iterator<I> {
		
		private SuffixTrieNode<I> current;

		public Iterator(SuffixTrieNode<I> node) {
			this.current = node;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return !current.isRoot();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public I next() {
			if(current.isRoot()) {
				throw new NoSuchElementException();
			}
			I sym = current.symbol;
			current = current.parent;
			return sym;
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
	
	public static <I> void appendSuffix(SuffixTrieNode<I> node, List<? super I> symList) {
		while(node.parent != null) {
			symList.add(node.symbol);
			node = node.parent;
		}
	}
	
	public static <I> Word<I> toExplicitWord(SuffixTrieNode<I> node) {
		WordBuilder<I> wb = new WordBuilder<I>(node.depth());
		appendSuffix(node, wb);
		return wb.toWord();
	}
	
	public static <I> int depth(SuffixTrieNode<I> node) {
		int d = 0;
		while(node.parent != null) {
			d++;
			node = node.parent;
		}
		return d;
	}
	
	public static <I> I getSymbol(SuffixTrieNode<I> node, int index) {
		while(index-- > 0)
			node = node.parent;
		return node.symbol;
	}
	
	
	
	
	
	private final I symbol;
	private final SuffixTrieNode<I> parent;
	

	/**
	 * Root constructor.
	 */
	public SuffixTrieNode() {
		this.symbol = null;
		this.parent = null;
	}
	
	
	public SuffixTrieNode(I symbol, SuffixTrieNode<I> parent) {
		this.symbol = symbol;
		this.parent = parent;
	}
	
	// TODO: replace by getter/attribute?
	public int depth() {
		return depth(this);
	}
		
	public I getSymbol() {
		return symbol;
	}
	
	public SuffixTrieNode<I> getParent() {
		return parent;
	}
	
	
	public boolean isRoot() {
		return (parent == null);
	}
	
	public void appendSuffix(List<? super I> symList) {
		appendSuffix(this, symList);
	}
	
	public Word<I> getSuffix() {
		if(parent == null)
			return Word.epsilon();
		WordBuilder<I> wb = new WordBuilder<>(depth());
		appendSuffix(wb);
		return wb.toWord();
	}

	@Override
	public I getSymbol(int index) {
		return getSymbol(this, index);
	}

	@Override
	public int length() {
		return depth();
	}
	
	@Override
	public Iterator<I> iterator() {
		return new Iterator<I>(this);
	}
}
