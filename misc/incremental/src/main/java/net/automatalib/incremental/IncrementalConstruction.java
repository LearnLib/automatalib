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

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Basic interface for incremental automata constructions. An incremental automaton construction
 * creates an (acyclic) automaton by iterated insertion of example words.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <A> the automaton model which is constructed
 * @param <I> input symbol class
 */
public interface IncrementalConstruction<A, I> {
	
	/**
	 * Retrieves the input alphabet of this construction.
	 * @return the input alphabet
	 */
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
	public Word<I> findSeparatingWord(A target, Collection<? extends I> inputs, boolean omitUndefined);
	
	/**
	 * Creates an automaton model from the current state of the construction.
	 * @return an automaton model
	 */
	public A toAutomaton();
	
	/**
	 * Checks whether this class has definitive information about a given word.
	 * @param word the word
	 * @return <tt>true</tt> if this class has definitive information about the word,
	 * <tt>false</tt> otherwise. 
	 */
	public boolean hasDefinitiveInformation(Word<I> word);
}
