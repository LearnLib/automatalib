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

import net.automatalib.words.Alphabet;

public class SharedSuffixTrie<I> extends SuffixTrie<I> {

	private final Alphabet<I> alphabet;
	
	public SharedSuffixTrie(Alphabet<I> alphabet) {
		super(new SharedSuffixTrieNode<I>());
		this.alphabet = alphabet;
	}
	public SharedSuffixTrie(Alphabet<I> alphabet, boolean graphRepresentable) {
		super(graphRepresentable, new SharedSuffixTrieNode<I>());
		this.alphabet = alphabet;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.util.tries.SuffixTrie#add(java.lang.Object, net.automatalib.util.tries.SuffixTrieNode)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SuffixTrieNode<I> add(I symbol, SuffixTrieNode<I> parent) {
		if(parent.getClass() != SharedSuffixTrieNode.class) {
			throw new IllegalArgumentException("Invalid suffix trie node");
		}
		
		int symbolIdx = alphabet.getSymbolIndex(symbol);
		SharedSuffixTrieNode<I> sparent = (SharedSuffixTrieNode<I>)parent;
		
		SharedSuffixTrieNode<I> child;
		SharedSuffixTrieNode<I>[] children = sparent.children;
		if(children == null) {
			children = sparent.children = new SharedSuffixTrieNode[alphabet.size()];
		}
		else if((child = children[symbolIdx]) != null) {
			return child;
		}
		child = new SharedSuffixTrieNode<>(symbol, sparent);
		children[symbolIdx] = child;
		return child;
	}

	

}
