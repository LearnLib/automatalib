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
package net.automatalib.util.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import org.testng.annotations.Test;

/**
 * FIXME: Turn this into a real test
 * 
 * @author Malte Isberner
 *
 */
@Test
public class CoversTest {

	@Test
	public void testIncrementalCover() {
		Alphabet<Integer> alphabet = Alphabets.integers(0, 2);
		CompactDFA<Integer> dfa = new CompactDFA<>(alphabet);
		
		List<Word<Integer>> stateCover = new ArrayList<>(), transCover = new ArrayList<>();
		List<Word<Integer>> structuralCover = new ArrayList<>();
		
		List<Word<Integer>> newStates = new ArrayList<>(), newTransitions = new ArrayList<>();
		List<Word<Integer>> newStructural = new ArrayList<>();
		
		List<Word<Integer>> empty = Collections.emptyList();
		
		int q0 = dfa.addInitialState();
		dfa.addTransition(q0, 0, q0);
		dfa.addTransition(q0, 1, q0);
		dfa.addTransition(q0, 2, q0);
		
		Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
		System.err.println("New states: " + newStates + ", new transitions: " + newTransitions);
		stateCover.addAll(newStates);
		transCover.addAll(newTransitions);
		newStates.clear();
		newTransitions.clear();
		
		Covers.incrementalCover(dfa, alphabet, structuralCover, empty, newStructural, newStructural);
		System.err.println("New structural: " + newStructural);
		structuralCover.addAll(newStructural);
		newStructural.clear();
		
		int q1 = dfa.addState();
		dfa.addTransition(q1, 0, q1);
		dfa.addTransition(q1, 1, q0);
		dfa.addTransition(q1, 2, q1);
		
		dfa.setTransition(q0, 0, q1);
		
		Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
		System.err.println("New states: " + newStates + ", new transitions: " + newTransitions);
		stateCover.addAll(newStates);
		transCover.addAll(newTransitions);
		newStates.clear();
		newTransitions.clear();
		
		Covers.incrementalCover(dfa, alphabet, structuralCover, empty, newStructural, newStructural);
		System.err.println("New structural: " + newStructural);
		structuralCover.addAll(newStructural);
		newStructural.clear();
		
		int q2 = dfa.addState();
		dfa.addTransition(q2, 0, q2);
		dfa.addTransition(q2, 1, q2);
		dfa.addTransition(q2, 2, q2);
		
		dfa.setTransition(q1, 2, q2);
		dfa.setTransition(q0, 2, q2);
		
		Covers.incrementalCover(dfa, alphabet, stateCover, transCover, newStates, newTransitions);
		System.err.println("New states: " + newStates + ", new transitions: " + newTransitions);
		stateCover.addAll(newStates);
		transCover.addAll(newTransitions);
		newStates.clear();
		newTransitions.clear();
		
		Covers.incrementalCover(dfa, alphabet, structuralCover, empty, newStructural, newStructural);
		System.err.println("New structural: " + newStructural);
		structuralCover.addAll(newStructural);
		newStructural.clear();
	}

}
