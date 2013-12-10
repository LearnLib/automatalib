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
package net.automatalib.automata.transout.impl.compact;

import java.util.List;

import net.automatalib.automata.base.compact.AbstractCompactDeterministic;
import net.automatalib.automata.dot.DOTHelperMealy;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractMealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class CompactMealy<I, O> extends
		AbstractCompactDeterministic<I, CompactMealyTransition<O>, Void, O> implements
		MutableMealyMachine<Integer, I, CompactMealyTransition<O>, O>, DOTPlottableAutomaton<Integer,I,CompactMealyTransition<O>>,
		DOTPlottableGraph<Integer, TransitionEdge<I, CompactMealyTransition<O>>>{
	
	

	public CompactMealy(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
	}

	public CompactMealy(Alphabet<I> alphabet, int stateCapacity,
			float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
	}

	public CompactMealy(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
	}

	public CompactMealy(Alphabet<I> alphabet) {
		super(alphabet);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.TransitionOutput#getTransitionOutput(java.lang.Object)
	 */
	@Override
	public O getTransitionOutput(CompactMealyTransition<O> transition) {
		return transition.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(CompactMealyTransition<O> transition,
			O property) {
		transition.setOutput(property);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.transout.TransitionOutputAutomaton#getOutput(java.lang.Object, java.lang.Object)
	 */
	@Override
	public O getOutput(Integer state, I input) {
		return AbstractTransOutAutomaton.getOutput(this, state, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.transout.TransitionOutputAutomaton#trace(java.lang.Iterable, java.util.List)
	 */
	@Override
	public void trace(Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.transout.TransitionOutputAutomaton#trace(java.lang.Object, java.lang.Iterable, java.util.List)
	 */
	@Override
	public void trace(Integer state, Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, state, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.SuffixOutput#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.Output#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public O getTransitionProperty(CompactMealyTransition<O> transition) {
		return AbstractMealyMachine.getTransitionProperty(this, transition);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.MutableTransitionOutput#setTransitionOutput(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionOutput(CompactMealyTransition<O> transition,
			O output) {
		transition.setOutput(output);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#getIntSuccessor(java.lang.Object)
	 */
	@Override
	public int getIntSuccessor(CompactMealyTransition<O> transition) {
		return transition.getSuccId();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#getStateProperty(int)
	 */
	@Override
	public Void getStateProperty(int stateId) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#getStateProperty(java.lang.Integer)
	 */
	@Override
	public Void getStateProperty(Integer state) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#createTransition(int, java.lang.Object)
	 */
	@Override
	public CompactMealyTransition<O> createTransition(int succId, O property) {
		return new CompactMealyTransition<O>(succId, property);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#copyTransition(java.lang.Object, int)
	 */
	@Override
	public CompactMealyTransition<O> copyTransition(
			CompactMealyTransition<O> trans, int succId) {
		return new CompactMealyTransition<O>(succId, trans.getOutput());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.base.compact.AbstractCompactDeterministic#setStateProperty(int, java.lang.Object)
	 */
	@Override
	public void setStateProperty(int state, Void property) {
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.dot.DOTPlottableAutomaton#getDOTHelper()
	 */
	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, CompactMealyTransition<O>>> getDOTHelper() {
		return new DOTHelperMealy<>(this);
	}
	
	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, CompactMealyTransition<O>>> getGraphDOTHelper() {
		return new DOTHelperMealy<>(this);
	}
	
}
