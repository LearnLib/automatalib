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
package net.automatalib.automata;

import java.util.function.IntFunction;

import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.words.Alphabet;



/**
 * A {@link DeterministicAutomaton} with state and transition properties.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class
 * @param <I> input symbol class
 * @param <T> transition class
 * @param <SP> state property class
 * @param <TP> transition property class
 * 
 * @see UniversalAutomaton
 */
public interface UniversalDeterministicAutomaton<S, I, T, SP, TP> extends
		DeterministicAutomaton<S, I, T>, UniversalDTS<S, I, T, SP, TP>, UniversalAutomaton<S,I,T,SP,TP> {
	
	/**
	 * Base interface for {@link SimpleDeterministicAutomaton.IntAbstraction integer abstractions} of a
	 * {@link UniversalDeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <T> transition type
	 * @param <SP> state property type
	 * @param <TP> transition property type
	 */
	public static interface IntAbstraction<T,SP,TP> extends DeterministicAutomaton.IntAbstraction<T> {
		/**
		 * Retrieves the state property of a given (abstracted) state.
		 * 
		 * @param state the integer representing the state of which to retrieve the property
		 * @return the property for the given state
		 */
		public SP getStateProperty(int state);
		
		/**
		 * Retrieves the transition property of a given transition.
		 * 
		 * @param transition the transition of which to retrieve the property
		 * @return the property for the given transition
		 */
		public TP getTransitionProperty(T transition);
	}
	
	
	/**
	 * Interface for {@link SimpleDeterministicAutomaton.StateIntAbstraction state integer abstractions} of
	 * a {@link UniversalDeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 * @param <T> transition type
	 * @param <SP> state property type
	 * @param <TP> transition property type
	 */
	public static interface StateIntAbstraction<I,T,SP,TP>
			extends IntAbstraction<T,SP,TP>, DeterministicAutomaton.StateIntAbstraction<I, T> {
		
		default public TP getTransitionProperty(int state, I input) {
			T trans = getTransition(state, input);
			if (trans != null) {
				return getTransitionProperty(trans);
			}
			return null;
		}
		
		public class DefaultAbstraction<S,I,T,SP,TP,A extends UniversalDeterministicAutomaton<S, I, T, SP, TP>>
				extends DeterministicAutomaton.StateIntAbstraction.DefaultAbstraction<S, I, T, A>
				implements StateIntAbstraction<I,T,SP,TP> {
			public DefaultAbstraction(A automaton) {
				super(automaton);
			}
			@Override
			public SP getStateProperty(int state) {
				return automaton.getStateProperty(intToState(state));
			}
			@Override
			public TP getTransitionProperty(T transition) {
				return automaton.getTransitionProperty(transition);
			}
		}
	}
	
	/**
	 * Interface for {@link SimpleDeterministicAutomaton.FullIntAbstraction full integer abstractions} of a
	 * {@link UniversalDeterministicAutomaton}.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <T> transition type
	 * @param <SP> state property type
	 * @param <TP> transition property type
	 */
	public static interface FullIntAbstraction<T,SP,TP>
			extends IntAbstraction<T,SP,TP>, DeterministicAutomaton.FullIntAbstraction<T> {
		
		default public TP getTransitionProperty(int state, int input) {
			T trans = getTransition(state, input);
			if (trans != null) {
				return getTransitionProperty(trans);
			}
			return null;
		}
		public class DefaultAbstraction<I,T,SP,TP,A extends StateIntAbstraction<I, T, SP, TP>>
				extends DeterministicAutomaton.FullIntAbstraction.DefaultAbstraction<I, T, A>
				implements FullIntAbstraction<T,SP,TP> {
			public DefaultAbstraction(A stateAbstraction, int numInputs,
					IntFunction<? extends I> symMapping) {
				super(stateAbstraction, numInputs, symMapping);
			}
			@Override
			public SP getStateProperty(int state) {
				return stateAbstraction.getStateProperty(state);
			}
			@Override
			public TP getTransitionProperty(T transition) {
				return stateAbstraction.getTransitionProperty(transition);
			}
		}
	}
	
	@Override
	default public StateIntAbstraction<I,T,SP,TP> stateIntAbstraction() {
		return new StateIntAbstraction.DefaultAbstraction<>(this);
	}
	
	@Override
	default public FullIntAbstraction<T,SP,TP> fullIntAbstraction(int numInputs, IntFunction<? extends I> symMapping) {
		return new FullIntAbstraction.DefaultAbstraction<>(stateIntAbstraction(), numInputs, symMapping);
	}
	
	@Override
	default public FullIntAbstraction<T,SP,TP> fullIntAbstraction(Alphabet<I> alphabet) {
		return fullIntAbstraction(alphabet.size(), alphabet);
	}
}

