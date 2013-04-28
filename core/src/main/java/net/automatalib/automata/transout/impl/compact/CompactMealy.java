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
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractMealyMachine;
import net.automatalib.automata.transout.abstractimpl.AbstractTransOutAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class CompactMealy<I, O> extends
		AbstractCompactDeterministic<I, CompactMealyTransition<O>, Void, O> implements
		MutableMealyMachine<Integer, I, CompactMealyTransition<O>, O> {
	
	

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

	@Override
	public O getTransitionOutput(CompactMealyTransition<O> transition) {
		return transition.getOutput();
	}

	@Override
	public void setTransitionProperty(CompactMealyTransition<O> transition,
			O property) {
		transition.setOutput(property);
	}

	

	@Override
	public O getOutput(Integer state, I input) {
		return AbstractTransOutAutomaton.getOutput(this, state, input);
	}

	@Override
	public void trace(Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, input, output);
	}

	@Override
	public void trace(Integer state, Iterable<I> input, List<O> output) {
		AbstractTransOutAutomaton.trace(this, state, input, output);
	}

	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}

	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}

	@Override
	public O getTransitionProperty(CompactMealyTransition<O> transition) {
		return AbstractMealyMachine.getTransitionProperty(this, transition);
	}

	@Override
	public void setTransitionOutput(CompactMealyTransition<O> transition,
			O output) {
		transition.setOutput(output);
	}

	@Override
	public int getIntSuccessor(CompactMealyTransition<O> transition) {
		return transition.getSuccId();
	}

	@Override
	public Void getStateProperty(int stateId) {
		return null;
	}
	
	@Override
	public Void getStateProperty(Integer state) {
		return null;
	}

	

	@Override
	public CompactMealyTransition<O> createTransition(int succId, O property) {
		return new CompactMealyTransition<O>(succId, property);
	}

	@Override
	public CompactMealyTransition<O> copyTransition(
			CompactMealyTransition<O> trans, int succId) {
		return new CompactMealyTransition<O>(succId, trans.getOutput());
	}

	@Override
	public void setStateProperty(int state, Void property) {
	}
	
}
