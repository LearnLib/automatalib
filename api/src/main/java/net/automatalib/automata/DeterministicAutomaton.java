/* Copyright (C) 2013-2015 TU Dortmund
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
package net.automatalib.automata;

import java.util.function.IntFunction;

import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.words.Alphabet;


/**
 * Basic interface for a deterministic automaton. A deterministic automaton is a
 * {@link DeterministicTransitionSystem} with a finite number of states.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state type
 * @param <I> input symbol type
 * @param <T> transition type
 */
public interface DeterministicAutomaton<S,I,T> extends Automaton<S,I,T>,
		SimpleDeterministicAutomaton<S,I>, DeterministicTransitionSystem<S, I, T> {
	
	/**
	 * Base interface for {@link SimpleDeterministicAutomaton.IntAbstraction integer abstractions}
	 * of a {@link DeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <T> transition type
	 */
	public static interface IntAbstraction<T> extends SimpleDeterministicAutomaton.IntAbstraction {
		/**
		 * Retrieves the (abstracted) successor of a transition object.
		 * 
		 * @param transition the transition object
		 * @return the integer representing the successor of the given transition
		 */
		public int getIntSuccessor(T transition);
	}
	
	/**
	 * Interface for {@link SimpleDeterministicAutomaton.StateIntAbstraction state integer abstractions}
	 * of a {@link DeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 * @param <T> transition type
	 */
	public static interface StateIntAbstraction<I,T>
			extends IntAbstraction<T>, SimpleDeterministicAutomaton.StateIntAbstraction<I> {
		/**
		 * Retrieves the outgoing transition for an (abstracted) source state and
		 * input symbol, or returns {@code null} if the automaton has no transition for this
		 * state and input.
		 * 
		 * @param state the integer representing the source state
		 * @param input the input symbol
		 * @return the outgoing transition, or {@code null}
		 */
		public T getTransition(int state, I input);
		
		@Override
		default public int getSuccessor(int state, I input) {
			T trans = getTransition(state, input);
			if (trans == null) {
				return INVALID_STATE;
			}
			return getIntSuccessor(trans);
		}
		
		public static class DefaultAbstraction<S,I,T,A extends DeterministicAutomaton<S, I, T>>
				extends SimpleDeterministicAutomaton.StateIntAbstraction.DefaultAbstraction<S, I, A>
				implements StateIntAbstraction<I,T> {
			public DefaultAbstraction(A automaton) {
				super(automaton);
			}
			@Override
			public int getIntSuccessor(T transition) {
				return stateToInt(automaton.getSuccessor(transition));
			}
			@Override
			public T getTransition(int state, I input) {
				return automaton.getTransition(intToState(state), input);
			}
		}
	}
	
	/**
	 * Interface for {@link SimpleDeterministicAutomaton.FullIntAbstraction full integer abstractions}
	 * of a {@link DeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <T> transition type
	 */
	public static interface FullIntAbstraction<T>
			extends IntAbstraction<T>, SimpleDeterministicAutomaton.FullIntAbstraction {
		/**
		 * Retrieves the outgoing transition for an (abstracted) source state and
		 * (abstracted) input symbol, or returns {@code null} if the automaton
		 * has no transition for this state and input.
		 * 
		 * @param state the integer representing the source state
		 * @param input the integer representing the input symbol
		 * @return the outgoing transition, or {@code null}
		 */
		public T getTransition(int state, int input);
		
		@Override
		default public int getSuccessor(int state, int input) {
			T trans = getTransition(state, input);
			if (trans == null) {
				return INVALID_STATE;
			}
			return getIntSuccessor(trans);
		}
		
		public static class DefaultAbstraction<I,T,A extends StateIntAbstraction<I, T>>
				extends SimpleDeterministicAutomaton.FullIntAbstraction.DefaultAbstraction<I,A>
				implements FullIntAbstraction<T> {
			public DefaultAbstraction(A stateAbstraction, int numInputs,
					IntFunction<? extends I> symMapping) {
				super(stateAbstraction, numInputs, symMapping);
			}
			@Override
			public int getIntSuccessor(T transition) {
				return stateAbstraction.getIntSuccessor(transition);
			}
			@Override
			public T getTransition(int state, int input) {
				return stateAbstraction.getTransition(state, intToSym(input));
			}
			
		}
	}
	
	@Override
	default public StateIntAbstraction<I,T> stateIntAbstraction() {
		return new StateIntAbstraction.DefaultAbstraction<>(this);
	}
	
	@Override
	default public FullIntAbstraction<T> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
		return new FullIntAbstraction.DefaultAbstraction<>(stateIntAbstraction(), numInputs, symMapping);
	}
	
	@Override
	default public FullIntAbstraction<T> fullIntAbstraction(Alphabet<I> alphabet) {
		return fullIntAbstraction(alphabet.size(), alphabet);
	}
}
