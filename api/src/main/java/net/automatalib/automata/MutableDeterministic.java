/* Copyright (C) 2013-2014 TU Dortmund
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

import java.util.Collection;
import java.util.function.IntFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Alphabet;

/**
 * Interface for a <i>mutable</i> deterministic automaton.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class.
 * @param <I> input symbol class.
 * @param <T> transition class.
 * @param <SP> state property.
 * @param <TP> transition property.
 */
@ParametersAreNonnullByDefault
public abstract interface MutableDeterministic<S,I,T,SP,TP> extends UniversalDeterministicAutomaton<S,I,T,SP,TP>,
	MutableAutomaton<S,I,T,SP,TP> {
	
	public static interface IntAbstraction<T,SP,TP> extends UniversalDeterministicAutomaton.IntAbstraction<T, SP, TP> {
		public void setStateProperty(int state, SP property);
		public void setTransitionProperty(T transition, TP property);
		public void setInitialState(int state);
		public T createTransition(int successor, TP property);
		
		default public int addIntState() {
			return addIntState(null);
		}
		
		public int addIntState(SP property);
		
		default public int addIntInitialState() {
			return addIntInitialState(null);
		}
		
		public int addIntInitialState(SP property);
	}
	
	public static interface StateIntAbstraction<I,T,SP,TP>
			extends IntAbstraction<T, SP, TP>, UniversalDeterministicAutomaton.StateIntAbstraction<I, T, SP, TP> {
		public void setTransition(int state, I input, T transition);
		public void setTransition(int state, I input, int successor, TP property);
		
		public static class DefaultAbstraction<S,I,T,SP,TP,A extends MutableDeterministic<S, I, T, SP, TP>>
				extends UniversalDeterministicAutomaton.StateIntAbstraction.DefaultAbstraction<S, I, T, SP, TP, A>
				implements StateIntAbstraction<I,T,SP,TP> {
			public DefaultAbstraction(A automaton) {
				super(automaton);
			}
			@Override
			public void setStateProperty(int state, SP property) {
				automaton.setStateProperty(intToState(state), property);
			}
			@Override
			public void setTransitionProperty(T transition, TP property) {
				automaton.setTransitionProperty(transition, property);
			}
			@Override
			public void setInitialState(int state) {
				automaton.setInitialState(intToState(state));
			}
			@Override
			public T createTransition(int successor, TP property) {
				return automaton.createTransition(intToState(successor), property);
			}
			@Override
			public int addIntState() {
				return stateToInt(automaton.addState());
			}
			@Override
			public int addIntState(SP property) {
				return stateToInt(automaton.addState(property));
			}
			@Override
			public int addIntInitialState() {
				return stateToInt(automaton.addInitialState());
			}
			@Override
			public int addIntInitialState(SP property) {
				return stateToInt(automaton.addInitialState(property));
			}
			@Override
			public void setTransition(int state, I input, T transition) {
				automaton.setTransition(intToState(state), input, transition);
			}
			@Override
			public void setTransition(int state, I input, int successor,
					TP property) {
				automaton.setTransition(intToState(state), input, intToState(successor), property);
			}
		}
	}
	
	public static interface FullIntAbstraction<T,SP,TP>
			extends IntAbstraction<T,SP,TP>, UniversalDeterministicAutomaton.FullIntAbstraction<T, SP, TP> {
		public void setTransition(int state, int input, T transition);
		public void setTransition(int state, int input, int successor, TP property);
		
		public static class DefaultAbstraction<I,T,SP,TP,A extends StateIntAbstraction<I, T, SP, TP>>
				extends UniversalDeterministicAutomaton.FullIntAbstraction.DefaultAbstraction<I, T, SP, TP, A>
				implements FullIntAbstraction<T, SP, TP> {
			public DefaultAbstraction(A stateAbstraction, int numInputs,
					IntFunction<? extends I> symMapping) {
				super(stateAbstraction, numInputs, symMapping);
			}
			@Override
			public void setStateProperty(int state, SP property) {
				stateAbstraction.setStateProperty(state, property);
			}
			@Override
			public void setTransitionProperty(T transition, TP property) {
				stateAbstraction.setTransitionProperty(transition, property);
			}
			@Override
			public void setInitialState(int state) {
				stateAbstraction.setInitialState(state);
			}
			@Override
			public T createTransition(int successor, TP property) {
				return stateAbstraction.createTransition(successor, property);
			}
			@Override
			public int addIntState() {
				return stateAbstraction.addIntState();
			}
			@Override
			public int addIntState(SP property) {
				return stateAbstraction.addIntState(property);
			}
			@Override
			public int addIntInitialState() {
				return stateAbstraction.addIntInitialState();
			}
			@Override
			public int addIntInitialState(SP property) {
				return stateAbstraction.addIntInitialState(property);
			}
			@Override
			public void setTransition(int state, int input, T transition) {
				stateAbstraction.setTransition(state, intToSym(input), transition);
			}
			@Override
			public void setTransition(int state, int input, int successor,
					TP property) {
				stateAbstraction.setTransition(state, intToSym(input), successor, property);
			}
		}
	}
	
	@Override
	default public void addTransition(S state, I input, T transition) {
		T currTrans = getTransition(state, input);
		if(currTrans != null)
			throw new IllegalStateException("Cannot add transition " + transition
					+ " to deterministic automaton: transition already defined for state "
					+ state + " and input " + input + ".");
		setTransition(state, input, transition);
	}
	
	@Override
	default public void removeTransition(S state, I input, T transition) {
		if(transition == null)
			return;
		T currTrans = getTransition(state, input);
		if(transition.equals(currTrans)) {
			setTransition(state, input, null);
		}
	}
	
	@Override
	default public void removeAllTransitions(S state, I input) {
		setTransition(state, input, null);
	}
	
	@Override
	default public void setTransitions(S state, I input, Collection<? extends T> transitions) {
		
		int num = transitions.size();
		if(num > 1) {
			throw new IllegalArgumentException("Deterministic automaton can not "
					+ "have multiple transitions for the same input symbol.");
		}
		
		T trans = (num > 0) ? transitions.iterator().next() : null;
		
		setTransition(state, input, trans);
	}
	
	@Override
	default public void setInitial(S state, boolean initial) {
		S currInitial = getInitialState();
		if(state.equals(currInitial)) {
			if(!initial)
				setInitialState(null);
		}
		else if(currInitial == null)
			setInitialState(state);
		else
			throw new IllegalStateException("Cannot set state '" + state + "' as "
					+ "additional initial state (current initial state: '"
					+ currInitial + "'.");
	}
	
	/**
	 * Sets the initial state to the given state. If the current initial state
	 * should be unset, {@code null} can be passed.
	 * @param state the new initial state, or {@code null}.
	 */
	public void setInitialState(@Nullable S state);
	
	/**
	 * Sets the transition for the given state and input symbol.
	 * @param state the source state
	 * @param input the triggering input symbol
	 * @param transition the transition
	 */
	public void setTransition(S state, @Nullable I input, @Nullable T transition);	
	
	
	/**
	 * Sets the transition for the given state and input symbol to a newly
	 * created one.
	 * @param state the source state
	 * @param input the triggering input symbol
	 * @param successor the target state
	 * @param property the transition's property
	 */
	default public void setTransition(S state, @Nullable I input, S successor, @Nullable TP property) {
		T trans = createTransition(successor, property);
		setTransition(state, input, trans);
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
