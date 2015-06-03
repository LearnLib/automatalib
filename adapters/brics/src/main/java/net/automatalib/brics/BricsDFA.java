/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
