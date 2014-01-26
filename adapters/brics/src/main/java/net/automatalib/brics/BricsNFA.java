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

import java.util.Collection;

import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractNFA;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

/**
 * Adapter class for wrapping a Brics automaton as an {@link NFA}.
 * <p>
 * This adapter is backed by the Brics automaton, so changes to the {@link Automaton}
 * are reflected.
 * <p>
 * As a DFA can be regarded as a special case of an NFA, using this class on a Brics
 * {@link Automaton} will always work. However, determining successor states for input
 * characters might be much less efficient than when using a {@link BricsDFA}.
 * 
 * @author Malte Isberner
 *
 */
public class BricsNFA extends AbstractBricsAutomaton implements
		NFA<State, Character> {

	/**
	 * Constructor.
	 * @param automaton the Brics automaton object
	 */
	public BricsNFA(Automaton automaton) {
		super(automaton);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.acceptors.AcceptorTS#accepts(java.lang.Iterable)
	 */
	@Override
	public boolean accepts(Iterable<? extends Character> input) {
		return AbstractNFA.accepts(this, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.fsa.NFA#isAccepting(java.util.Collection)
	 */
	@Override
	public boolean isAccepting(Collection<? extends State> states) {
		return AbstractNFA.isAccepting(this, states);
	}

}
