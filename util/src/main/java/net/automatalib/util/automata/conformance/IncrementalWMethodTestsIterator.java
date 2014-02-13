/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.automata.conformance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * An iterator that enumerates the test cases as obtained through the <i>W method</i>
 * conformance test in an incremental fashion.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class IncrementalWMethodTestsIterator<I> implements Iterator<Word<I>> {
	
	private final static class Item<I> {
		private int prefixIdx;
		private int suffixIdx;
		private Word<I> middle;
		
		private int minSuffix;
		private int minPrefix;
		private int maxPrefix;
		
		@Override
		public String toString() {
			return Integer.toString(prefixIdx) + " | " + middle + " | " + Integer.toString(suffixIdx);
		}
	}
	
	private static final class ItemMerge<I> implements StrictPriorityQueue.MergeOperation<Item<I>> {

		@Override
		public Item<I> merge(Item<I> oldObject, Item<I> newObject) {
			oldObject.minSuffix = Math.min(oldObject.minSuffix, newObject.minSuffix);
			oldObject.minPrefix = Math.min(oldObject.minPrefix, newObject.minPrefix);
			oldObject.maxPrefix = Math.max(oldObject.maxPrefix, newObject.maxPrefix);
			return oldObject;
		}
		
	}
	
	private static final class ItemComparator<I> implements Comparator<Item<? extends I>> {
		private final Comparator<? super Word<? extends I>> canonicalCmp;
		
		public ItemComparator(Comparator<? super I> symComparator) {
			this.canonicalCmp = Word.canonicalComparator(symComparator);
		}

		@Override
		public int compare(Item<? extends I> o1, Item<? extends I> o2) {
			int cmp = canonicalCmp.compare(o1.middle, o2.middle);
			if(cmp != 0) {
				return cmp;
			}
			
			cmp = o1.prefixIdx - o2.prefixIdx;
			if(cmp != 0) {
				return cmp;
			}
			
			return o1.suffixIdx - o2.suffixIdx;
		}
	}
	
	private final Alphabet<I> alphabet;
	private final StrictPriorityQueue<Item<I>> itemQueue;
	
	private int maxDepth;
	
	private final List<Word<I>> prefixes = new ArrayList<>();
	private final List<Word<I>> suffixes = new ArrayList<>();

	public IncrementalWMethodTestsIterator(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
		this.itemQueue = new StrictPriorityQueue<>(new ItemComparator<>(alphabet), new ItemMerge<I>());
		this.suffixes.add(Word.<I>epsilon()); // *always* assume the empty word as a suffix
	}
	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	private Word<I> startMiddleWord() {
		return Word.fromLetter(alphabet.getSymbol(0));
	}
	
	public void update(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton) {
		int oldNumPrefixes = prefixes.size();
		boolean newPrefixes = Automata.incrementalStructuralCover(automaton, alphabet, prefixes, prefixes);
		
		int oldNumSuffixes = suffixes.size();
		boolean newSuffixes = Automata.incrementalCharacterizingSet(automaton, alphabet, suffixes, suffixes);
		
		// old prefixes with all *new* suffixes
		if(newSuffixes && oldNumPrefixes > 0) {
			System.err.println("New suffixes: " + suffixes.subList(oldNumSuffixes, suffixes.size()));
			Item<I> item = new Item<>();
			item.prefixIdx = 0;
			item.minPrefix = 0;
			item.maxPrefix = oldNumPrefixes;
			item.suffixIdx = oldNumSuffixes;
			item.minSuffix = oldNumSuffixes;
			item.middle = startMiddleWord();
			itemQueue.insert(item);
		}
		// new prefixes with *all* suffixes
		if(newPrefixes) {
			System.err.println("New prefixes: " + prefixes.subList(oldNumPrefixes, prefixes.size()));
			Item<I> item = new Item<>();
			item.prefixIdx = oldNumPrefixes;
			item.minPrefix = oldNumPrefixes;
			item.maxPrefix = prefixes.size();
			item.suffixIdx = 0;
			item.minSuffix = 0;
			item.middle = startMiddleWord();
			itemQueue.insert(item);
		}
	}
	
	@Override
	public boolean hasNext() {
		return !itemQueue.isEmpty();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Word<I> next() {
		Item<I> nextItem = itemQueue.extractMin();
		
		Word<I> result = assembleWord(nextItem);
		Item<I> inc = increment(nextItem);
		if(inc != null) {
			itemQueue.insert(inc);
		}
		return result;
	}
	
	private Item<I> increment(Item<I> item) {
		item.suffixIdx++;
		if(item.suffixIdx >= suffixes.size()) {
			item.suffixIdx = item.minSuffix;
			
			item.prefixIdx++;
			if(item.prefixIdx >= item.maxPrefix) {
				item.prefixIdx = item.minPrefix;
				
				item.middle = item.middle.canonicalNext(alphabet);
				if(item.middle.length() > maxDepth) {
					return null;
				}
			}
		}
		
		return item;
	}
	
	private Word<I> assembleWord(Item<I> item) {
		Word<I> prefix = prefixes.get(item.prefixIdx);
		Word<I> suffix = suffixes.get(item.suffixIdx);
		return prefix.concat(item.middle, suffix);
	}
	
	
	
}
