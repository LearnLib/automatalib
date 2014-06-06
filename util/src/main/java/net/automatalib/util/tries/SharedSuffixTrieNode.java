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

/**
 * A node in a {@link SharedSuffixTrie}. This class maintains an
 * array containing all children, in order to avoid inserting
 * duplicates.
 * 
 * @author Malte Isberner 
 *
 * @param <I> symbol class.
 */
final class SharedSuffixTrieNode<I> extends SuffixTrieNode<I> {
	
	SharedSuffixTrieNode<I>[] children;

	/**
	 * Root constructor.
	 */
	public SharedSuffixTrieNode() {
	}

	/**
	 * Constructor.
	 * 
	 * @param symbol the symbol to prepend.
	 * @param parent the trie node representing the remaining suffix.
	 */
	public SharedSuffixTrieNode(I symbol, SuffixTrieNode<I> parent) {
		super(symbol, parent);
	}

}
