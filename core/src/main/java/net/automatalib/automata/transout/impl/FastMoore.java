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

import java.util.List;

import net.automatalib.automata.base.fast.FastMutableDet;
import net.automatalib.automata.transout.MutableMooreMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractMooreMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;


/**
 * A fast implementation of a Moore automaton.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
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
	 * @see de.ls5.automata.transout.TransitionOutputAutomaton#trace(java.lang.Iterable)
	 */
	@Override
	public void trace(Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.transout.TransitionOutputAutomaton#trace(java.lang.Object, java.lang.Iterable)
	 */
	@Override
	public void trace(FastMooreState<O> state, Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, state, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.TransitionOutput#getTransitionOutput(java.lang.Object)
	 */
	@Override
	public O getTransitionOutput(FastMooreState<O> transition) {
		return AbstractMooreMachine.getTransitionOutput(this, transition);
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
	 * @see de.ls5.automata.MutableAutomaton#setStateProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateProperty(FastMooreState<O> state, O property) {
		AbstractMooreMachine.setStateProperty(this, state, property);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(FastMooreState<O> transition, Void property) {
		AbstractMooreMachine.setTransitionProperty(this, transition, property);
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
	 * @see de.ls5.automata.MutableAutomaton#copyTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FastMooreState<O> copyTransition(FastMooreState<O> trans, FastMooreState<O> succ) {
		return AbstractMooreMachine.copyTransition(this, trans, succ);
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

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getStateProperty(java.lang.Object)
	 */
	@Override
	public O getStateProperty(FastMooreState<O> state) {
		return AbstractMooreMachine.getStateProperty(this, state);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public Void getTransitionProperty(FastMooreState<O> transition) {
		return AbstractMooreMachine.getTransitionProperty(this, transition);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.SODetOutputAutomaton#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.OutputAutomaton#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}


	@Override
	public O getOutput(FastMooreState<O> state, I input) {
		return AbstractTransOutAutomaton.getOutput(this, state, input);
	}

}
