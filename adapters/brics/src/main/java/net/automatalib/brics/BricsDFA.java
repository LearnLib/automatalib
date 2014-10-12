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
package net.automatalib.brics;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

/**
 * Adapter class for wrapping a Brics automaton as a {@link DFA}.
 * <p>
 * This adapter is backed by the Brics automaton, so changes to the {@link Automaton}
 * are reflected. Please note that any changes which result in a loss of determinism
 * will result in incorrect behavior exposed by this class until determinism is restored.
 *  
 * @author Malte Isberner
 *
 */
@ParametersAreNonnullByDefault
public class BricsDFA extends AbstractBricsAutomaton implements
		DFA<State, Character> {
	
	private static Automaton requireDeterministic(Automaton aut, boolean mayDeterminize) {
		if(aut.isDeterministic()) {
			if(!mayDeterminize)
				throw new IllegalArgumentException("A BricsDFA expects a deterministic automaton");
			aut.determinize();
		}
		return aut;
	}

	/**
	 * Constructor. If the specified automaton is not deterministic, this will result
	 * in an {@link IllegalArgumentException}.
	 * @param automaton the Brics automaton to wrap.
	 */
	public BricsDFA(Automaton automaton) {
		this(automaton, false);
	}
	
	/**
	 * Constructor. If <tt>mayDeterminize</tt> is false, this constructor behaves as the above
	 * {@link #BricsDFA(Automaton)}. Otherwise, if the specified automaton is not deterministic,
	 * it is determinized beforehand by invoking {@link Automaton#determinize()}.
	 * @param automaton the Brics automaton to wrap.
	 * @param mayDeterminize whether or not a possible nondeterministic automaton may be
	 * determinized.
	 */
	public BricsDFA(Automaton automaton, boolean mayDeterminize) {
		super(requireDeterministic(automaton, mayDeterminize));
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleDTS#getInitialState()
	 */
	@Override
	public State getInitialState() {
		return automaton.getInitialState();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.simple.SimpleDTS#getSuccessor(java.lang.Object, java.lang.Object)
	 */
	@Override
	public State getSuccessor(State state, @Nonnull Character input) {
		return state.step(input.charValue());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public State getTransition(State state, @Nonnull Character input) {
		return state.step(input.charValue());
	}
	
}
