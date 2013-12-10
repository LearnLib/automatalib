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
package net.automatalib.util.automata.random;

import java.util.Collection;
import java.util.Random;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;

public class RandomAutomata {
	
	private static final class InstanceHolder {
		public static final RandomAutomata INSTANCE = new RandomAutomata();
	}
	
	public static RandomAutomata getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	
	


	public static <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			Random rand,
			int numStates,
			Collection<? extends I> inputs,
			Collection<? extends SP> stateProps,
			Collection<? extends TP> transProps,
			A out) {
		return randomDeterministic(rand, numStates, inputs, stateProps, transProps, out, true);
	}
	
	public static <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			Random rand,
			int numStates,
			Collection<? extends I> inputs,
			Collection<? extends SP> stateProps,
			Collection<? extends TP> transProps,
			A out, boolean minimize) {
		
		RandomDeterministicAutomatonGenerator<S, I, T, SP, TP, A> gen
			= new RandomDeterministicAutomatonGenerator<>(rand, inputs, stateProps, transProps, out);
			
		gen.addStates(numStates);
		gen.addTransitions();
		gen.chooseInitial();
		
		
		if(minimize)
			Automata.invasiveMinimize(out, inputs);
		
		return out;
	}
	
	public static <I>
	DFA<?,I> randomDFA(
			Random rand,
			int numStates,
			Alphabet<I> inputs,
			boolean minimize) {
		return randomDeterministic(rand, numStates, inputs, DFA.STATE_PROPERTIES, DFA.TRANSITION_PROPERTIES, new CompactDFA<>(inputs), minimize);
	}
	
	public static <I>
	DFA<?,I> randomDFA(
			Random rand,
			int numStates,
			Alphabet<I> inputs) {
		return randomDFA(rand, numStates, inputs, true);
	}
	
	
	
	private final Random random;
	
	public RandomAutomata() {
		this(new Random());
	}
	
	public RandomAutomata(Random random) {
		this.random = random;
	}
	
	

	public <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			int numStates,
			Collection<? extends I> inputs,
			Collection<? extends SP> stateProps,
			Collection<? extends TP> transProps,
			A out) {
		return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out);
	}
	
	public <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			int numStates,
			Collection<? extends I> inputs,
			Collection<? extends SP> stateProps,
			Collection<? extends TP> transProps,
			A out, boolean minimize) {
		return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out, minimize);
	}
	
	public <I>
	DFA<?,I> randomDFA(
			int numStates,
			Alphabet<I> inputs,
			boolean minimize) {
		return randomDFA(this.random, numStates, inputs, minimize); 
	}
	
	public <I>
	DFA<?,I> randomDFA(
			int numStates,
			Alphabet<I> inputs) {
		return randomDFA(this.random, numStates, inputs);
	}
	

}
