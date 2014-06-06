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
import net.automatalib.automata.dot.DOTHelperMealy;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.abstractimpl.AbstractDeterministicTransOutTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;



/**
 * A fast implementation of a Mealy machine.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 * @param <O> output symbol class.
 */
public class FastMealy<I,O> extends FastMutableDet<FastMealyState<O>,I,MealyTransition<FastMealyState<O>,O>,Void,O>
        implements MutableMealyMachine<FastMealyState<O>, I, MealyTransition<FastMealyState<O>,O>, O>, DOTPlottableAutomaton<FastMealyState<O>,I,MealyTransition<FastMealyState<O>,O>> {


	/**
	 * Constructor. Initializes a new (empty) Mealy machine with
	 * the given input alphabet.
	 * @param alphabet the input alphabet.
	 */
	public FastMealy(Alphabet<I> alphabet) {
		super(alphabet);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public FastMealyState<O> getSuccessor(
			MealyTransition<FastMealyState<O>, O> transition) {
		return transition.getSuccessor();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.transout.TransitionOutputAutomaton#trace(java.lang.Iterable, java.util.List)
	 */
	@Override
	public boolean trace(Iterable<? extends I> input, List<? super O> output) {
		return AbstractDeterministicTransOutTS.trace(this, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.transout.TransitionOutputAutomaton#trace(java.lang.Object, java.lang.Iterable, java.util.List)
	 */
	@Override
	public boolean trace(FastMealyState<O> state, Iterable<? extends I> input, List<? super O> output) {
		return AbstractDeterministicTransOutTS.trace(this, state, input, output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.TransitionOutput#getTransitionOutput(java.lang.Object)
	 */
	@Override
	public O getTransitionOutput(
			MealyTransition<FastMealyState<O>, O> transition) {
		return transition.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getStateProperty(java.lang.Object)
	 */
	@Override
	public Void getStateProperty(FastMealyState<O> state) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public O getTransitionProperty(
			MealyTransition<FastMealyState<O>, O> transition) {
		return transition.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#setStateProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateProperty(FastMealyState<O> state, Void property) {
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(
			MealyTransition<FastMealyState<O>, O> transition, O property) {
		transition.setOutput(property);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#createTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public MealyTransition<FastMealyState<O>, O> createTransition(
			FastMealyState<O> successor, O properties) {
		return new MealyTransition<FastMealyState<O>, O>(successor, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#copyTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public MealyTransition<FastMealyState<O>, O> copyTransition(
			MealyTransition<FastMealyState<O>, O> trans, FastMealyState<O> succ) {
		return new MealyTransition<FastMealyState<O>,O>(succ, trans.getOutput());
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.MutableTransitionOutput#setTransitionOutput(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionOutput(
			MealyTransition<FastMealyState<O>, O> transition, O output) {
		transition.setOutput(output);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.base.fast.FastMutableDet#createState(java.lang.Object)
	 */
	@Override
	protected FastMealyState<O> createState(Void property) {
		return new FastMealyState<O>(inputAlphabet.size());
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.SODetOutputAutomaton#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Word<O> computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.features.OutputAutomaton#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Word<O> computeOutput(Iterable<? extends I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}
	
	@Override
	public FastMealyState<O> addInitialState() {
		return addInitialState(null);
	}
	
	@Override
	public FastMealyState<O> addState() {
		return addState(null);
	}

	@Override
	public O getOutput(FastMealyState<O> state, I input) {
		return AbstractDeterministicTransOutTS.getOutput(this, state, input);
	}

	@Override
	public GraphDOTHelper<FastMealyState<O>, TransitionEdge<I, MealyTransition<FastMealyState<O>, O>>> getDOTHelper() {
		return new DOTHelperMealy<FastMealyState<O>, I, MealyTransition<FastMealyState<O>,O>, O>(this);
	}
}
