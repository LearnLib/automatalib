/* Copyright (C) 2014 TU Dortmund
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
