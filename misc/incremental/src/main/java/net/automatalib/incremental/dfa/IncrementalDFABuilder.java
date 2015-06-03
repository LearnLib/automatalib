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
package net.automatalib.incremental.dfa;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.words.Word;

/**
 * General interface for incremental DFA builders.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
@ParametersAreNonnullByDefault
public interface IncrementalDFABuilder<I> extends
		IncrementalConstruction<DFA<?, I>, I> {
	
	public static interface GraphView<I,N,E>
		extends Graph<N,E> {
		@Nullable
		public I getInputSymbol(@Nonnull E edge);
		@Nonnull
		public Acceptance getAcceptance(@Nonnull N node);
		@Nonnull
		public N getInitialNode();
	}
	
	public static interface TransitionSystemView<S,I,T>
		extends UniversalDTS<S, I, T, Acceptance, Void> {

		@Nonnull
		public Acceptance getAcceptance(@Nonnull S state);
	}
	
	/**
	 * Looks up the tri-state acceptance value for a given word.
	 * 
	 * @param inputWord the word
	 * @return the tri-state acceptance value for this word.
	 */
	@Nonnull
	public Acceptance lookup(Word<? extends I> inputWord);
	
	/**
	 * Inserts a new word into the automaton, with a given acceptance value.
	 * 
	 * @param word the word to insert
	 * @param accepting whether or not this word should be marked as accepting
	 * @throws ConflictException if the newly provided information conflicts with existing
	 * information
	 */
	public void insert(Word<? extends I> word, boolean accepting) throws ConflictException;
	
	/**
	 * Inserts a new word into the automaton.
	 * This is a convenience method equivalent to invoking {@code insert(word, true)}.
	 * 
	 * @param word the word to insert
	 * @throws ConflictException if the newly provided information conflicts with existing
	 * information
	 * 
	 * @see #insert(Word, boolean)
	 */
	public void insert(Word<? extends I> word) throws ConflictException;
	
	@Override
	@Nonnull
	public GraphView<I,?,?> asGraph();
	
	@Override
	@Nonnull
	public TransitionSystemView<?,I,?> asTransitionSystem();
}
