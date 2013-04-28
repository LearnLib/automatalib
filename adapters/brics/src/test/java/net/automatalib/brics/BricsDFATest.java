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
package net.automatalib.brics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

@Test
public class BricsDFATest {
	
	private Automaton bricsAutomaton;
	private BricsDFA dfa;
	
	@BeforeClass
	public void setUp() {
		RegExp re = new RegExp("a(b*|cc+)d?e");
		this.bricsAutomaton = re.toAutomaton();
		dfa = new BricsDFA(bricsAutomaton, true);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	@SuppressWarnings("unused")
	public void testNondetAutomaton() {
		RegExp nondetRe = new RegExp("ab|ac");
		new BricsDFA(nondetRe.toAutomaton());
	}
	
	@Test
	public void testWordAcceptance() {
		List<String> strings = Arrays.asList(
				"ae",
				"abbe",
				"abbde",
				"acce",
				"acde",
				"abcde");
		
		for(String s : strings)
			Assert.assertEquals(dfa.accepts(Word.fromString(s)), bricsAutomaton.run(s));
	}
	
	@Test
	public void testStructuralEquality() {
		Assert.assertEquals(dfa.getInitialState(), bricsAutomaton.getInitialState());
		
		Set<State> states1 = new HashSet<State>(bricsAutomaton.getStates());
		Set<State> states2 = new HashSet<State>(dfa.getStates());
		
		Assert.assertEquals(states1, states2);
		
		for(State s : dfa) {
			Assert.assertEquals(dfa.isAccepting(s), s.isAccept());
			
			Set<Transition> trans1 = new HashSet<Transition>(dfa.getOutgoingEdges(s));
			Set<Transition> trans2 = new HashSet<Transition>(s.getTransitions());
			
			Assert.assertEquals(trans1, trans2);
		}
	}
	
	

}
