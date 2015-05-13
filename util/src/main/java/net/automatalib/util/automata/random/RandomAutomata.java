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
import java.util.Collections;
import java.util.Random;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;

@ParametersAreNonnullByDefault
public class RandomAutomata {
	
	private static final class InstanceHolder {
		@Nonnull
		public static final RandomAutomata INSTANCE = new RandomAutomata();
	}
	
	@Nonnull
	public static RandomAutomata getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	
	
	@Nonnull
	public static <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			Random rand,
			@Nonnegative
			int numStates,
			Collection<? extends I> inputs,
			@Nullable
			Collection<? extends SP> stateProps,
			@Nullable
			Collection<? extends TP> transProps,
			A out) {
		return randomDeterministic(rand, numStates, inputs, stateProps, transProps, out, true);
	}
	
	@Nonnull
	public static <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			Random rand,
			@Nonnegative
			int numStates,
			Collection<? extends I> inputs,
			@Nullable
			Collection<? extends SP> stateProps,
			@Nullable
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
	
	/**
	 * Randomly generates an initially connected DFA (ICDFA), i.e., a DFA where all states are reachable from the initial state.
	 * 
	 * @param rand the randomness source
	 * @param numStates the number of states of the generated automaton
	 * @param inputs the input alphabet
	 * @param minimize determines whether or not the DFA will be minimized before being returned. Note that if {@code true} is passed
	 * for this parameter, the resulting automaton might have a {@link Automaton#size() size} less than {@code numStates}
	 * @return a randomly generated ICDFA
	 */
	@Nonnull
	public static <I>
	CompactDFA<I> randomICDFA(
			Random rand,
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			boolean minimize) {
		CompactDFA<I> dfa = new RandomICAutomatonGenerator<Boolean,Void>()
			.withStateProperties(r -> r.nextBoolean())
			.generateICDeterministicAutomaton(numStates, inputs, new CompactDFA.Creator<I>(), rand);
		if (minimize) {
			dfa = DFAs.minimize(dfa);
		}
		return dfa;
	}
	
	@Nonnull
	public static <I>
	CompactDFA<I> randomDFA(
			Random rand,
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			boolean minimize) {
		return randomDeterministic(rand, numStates, inputs, DFA.STATE_PROPERTIES, DFA.TRANSITION_PROPERTIES, new CompactDFA<>(inputs), minimize);
	}
	
	@Nonnull
	public static <I>
	CompactDFA<I> randomDFA(
			Random rand,
			@Nonnegative
			int numStates,
			Alphabet<I> inputs) {
		return randomDFA(rand, numStates, inputs, true);
	}
	
	
	@Nonnull
	public static <I,O>
	CompactMealy<I, O> randomMealy(
			Random rand,
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			Collection<? extends O> outputs,
			boolean minimize) {
		return randomDeterministic(rand, numStates, inputs, Collections.<Void>singleton(null), outputs, new CompactMealy<I,O>(inputs), minimize);
	}
	
	@Nonnull
	public static <I,O>
	CompactMealy<I,O> randomMealy(
			Random rand,
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			Collection<? extends O> outputs) {
		return randomMealy(rand, numStates, inputs, outputs, true);
	}
	
	
	@Nonnull
	private final Random random;
	
	public RandomAutomata() {
		this(new Random());
	}
	
	public RandomAutomata(Random random) {
		this.random = random;
	}
	
	@Nonnull
	public <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			@Nonnegative
			int numStates,
			Collection<? extends I> inputs,
			@Nullable
			Collection<? extends SP> stateProps,
			@Nullable
			Collection<? extends TP> transProps,
			A out) {
		return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out);
	}
	
	@Nonnull
	public <S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
	A randomDeterministic(
			@Nonnegative
			int numStates,
			Collection<? extends I> inputs,
			@Nullable
			Collection<? extends SP> stateProps,
			@Nullable
			Collection<? extends TP> transProps,
			A out, boolean minimize) {
		return randomDeterministic(this.random, numStates, inputs, stateProps, transProps, out, minimize);
	}
	
	@Nonnull
	public <I>
	CompactDFA<I> randomDFA(
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			boolean minimize) {
		return randomDFA(this.random, numStates, inputs, minimize); 
	}
	
	@Nonnull
	public <I>
	CompactDFA<I> randomDFA(
			@Nonnegative
			int numStates,
			Alphabet<I> inputs) {
		return randomDFA(this.random, numStates, inputs);
	}
	
	@Nonnull
	public <I,O>
	CompactMealy<I,O> randomMealy(
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			Collection<? extends O> outputs,
			boolean minimize) {
		return randomMealy(this.random, numStates, inputs, outputs, minimize);
	}
	
	@Nonnull
	public <I,O>
	CompactMealy<I,O> randomMealy(
			@Nonnegative
			int numStates,
			Alphabet<I> inputs,
			Collection<? extends O> outputs) {
		return randomMealy(this.random, numStates, inputs, outputs);
	}
	

}
