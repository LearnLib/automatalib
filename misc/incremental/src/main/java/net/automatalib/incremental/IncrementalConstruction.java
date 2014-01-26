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
package net.automatalib.incremental;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.graphs.Graph;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Basic interface for incremental automata constructions. An incremental automaton construction
 * creates an (acyclic) automaton by iterated insertion of example words.
 *  
 * @author Malte Isberner
 *
 * @param <A> the automaton model which is constructed
 * @param <I> input symbol class
 */
@ParametersAreNonnullByDefault
public interface IncrementalConstruction<A, I> {
	
	/**
	 * Retrieves the input alphabet of this construction.
	 * @return the input alphabet
	 */
	@Nonnull
	public Alphabet<I> getInputAlphabet();
	
	/**
	 * Checks the current state of the construction against a given target model,
	 * and returns a word exposing a difference if there is one.
	 * @param target the target automaton model
	 * @param inputs the set of input symbols to consider
	 * @param omitUndefined if this is set to <tt>true</tt>, then undefined transitions in
	 * the <tt>target</tt> model will be interpreted as "unspecified/don't know" and omitted
	 * in the equivalence test. Otherwise, they will be interpreted in the usual manner
	 * (e.g., non-accepting sink in case of DFAs).
	 * @return a separating word, or <tt>null</tt> if no difference could be found.
	 */
	@Nullable
	public Word<I> findSeparatingWord(A target, Collection<? extends I> inputs, boolean omitUndefined);
	
	/**
	 * Checks whether this class has definitive information about a given word.
	 * @param word the word
	 * @return <tt>true</tt> if this class has definitive information about the word,
	 * <tt>false</tt> otherwise. 
	 */
	public boolean hasDefinitiveInformation(Word<I> word);

	/**
	 * Retrieves a <i>graph view</i> of the current state of the construction. The graph model should be
	 * backed by the construction, i.e., subsequent changes will be reflected in the graph model.
	 * @return a graph view on the current state of the construction
	 */
	@Nonnull
	public Graph<?,?> asGraph();
	
	/**
	 * Retrieves a <i>transition system view</i> of the current state of the construction. The transition
	 * system model should be backed by the construction, i.e., subsequent changes will be reflected in
	 * the transition system.
	 * @return a transition system view on the current state of the construction
	 */
	@Nonnull
	public DeterministicTransitionSystem<?, I, ?> asTransitionSystem();
}
