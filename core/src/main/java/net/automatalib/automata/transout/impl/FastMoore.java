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
package net.automatalib.automata.transout.impl;

import net.automatalib.automata.base.fast.FastMutableDet;
import net.automatalib.automata.transout.MutableMooreMachine;
import net.automatalib.words.Alphabet;


/**
 * A fast implementation of a Moore automaton.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 * @param <O> output symbol class.
 */
public final class FastMoore<I, O> extends FastMutableDet<FastMooreState<O>, I, FastMooreState<O>, O, Void>
		implements MutableMooreMachine<FastMooreState<O>, I, FastMooreState<O>, O> {

	public FastMoore(Alphabet<I> alphabet) {
		super(alphabet);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public FastMooreState<O> getSuccessor(FastMooreState<O> transition) {
		return transition;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.StateOutput#getStateOutput(java.lang.Object)
	 */
	@Override
	public O getStateOutput(FastMooreState<O> state) {
		return state.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#createTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FastMooreState<O> createTransition(FastMooreState<O> successor,
			Void properties) {
		return successor;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.MutableStateOutput#setStateOutput(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateOutput(FastMooreState<O> state, O output) {
		state.setOutput(output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.base.fast.FastMutableDet#createState(java.lang.Object)
	 */
	@Override
	protected FastMooreState<O> createState(O property) {
		return new FastMooreState<O>(inputAlphabet.size(), property);
	}

}
