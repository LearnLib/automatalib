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

import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.collections.ThreeLevelIterator;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class WMethodTestsIterator<I> extends ThreeLevelIterator<List<I>, Word<I>, Word<I>, Word<I>> {
	
	private final List<Word<I>> prefixes;
	private final List<Word<I>> suffixes;
	
	private final WordBuilder<I> wordBuilder = new WordBuilder<>();

	public WMethodTestsIterator(Alphabet<I> alphabet, UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton, int maxDepth) {
		super(CollectionsUtil.allTuples(alphabet, 1, maxDepth).iterator());
		this.prefixes = Automata.transitionCover(automaton, alphabet);
		this.suffixes = Automata.characterizingSet(automaton, alphabet);
	}

	@Override
	protected Iterator<Word<I>> l2Iterator(List<I> l1Object) {
		return prefixes.iterator();
	}

	@Override
	protected Iterator<Word<I>> l3Iterator(List<I> l1Object, Word<I> l2Object) {
		return suffixes.iterator();
	}

	@Override
	protected Word<I> combine(List<I> middle, Word<I> prefix,
			Word<I> suffix) {
		Word<I> word = wordBuilder.append(prefix).append(middle).append(suffix).toWord();
		wordBuilder.clear();
		return word;
	}
}
