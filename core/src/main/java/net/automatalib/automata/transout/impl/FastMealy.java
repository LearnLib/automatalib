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
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.words.Alphabet;



/**
 * A fast implementation of a Mealy machine.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class.
 * @param <O> output symbol class.
 */
public class FastMealy<I,O> extends FastMutableDet<FastMealyState<O>,I,MealyTransition<FastMealyState<O>,O>,Void,O>
        implements MutableMealyMachine<FastMealyState<O>, I, MealyTransition<FastMealyState<O>,O>, O> {


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
	 * @see de.ls5.automata.features.TransitionOutput#getTransitionOutput(java.lang.Object)
	 */
	@Override
	public O getTransitionOutput(
			MealyTransition<FastMealyState<O>, O> transition) {
		return transition.getOutput();
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
}
