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
package net.automatalib.automata.transout.abstractimpl;

import java.util.List;

import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.ts.abstractimpl.AbstractDeterministicTransOutTS;
import net.automatalib.words.Word;

public abstract class AbstractMealyMachine<S,I,T,O> extends AbstractDeterministicAutomaton<S, I, T>
		implements MealyMachine<S, I, T, O> {
	
	public static <S,I,T,O> Void getStateProperty(MealyMachine<S, I, T, O> $this, S state) {
		return null;
	}
	
	public static <S,I,T,O> O getTransitionProperty(MealyMachine<S,I,T,O> $this, T transition) {
		return $this.getTransitionOutput(transition);
	}
	
	public static <S,I,T,O> void setStateProperty(MutableMealyMachine<S, I, T, O> $this, S state, Void property) {
		
	}
	
	public static <S,I,T,O> void setTransitionProperty(MutableMealyMachine<S, I, T, O> $this, T transition, O property) {
		$this.setTransitionOutput(transition, property);
	}
	
	public static <S,I,T,O> T copyTransition(MutableMealyMachine<S, I, T, O> $this, T transition, S succ) {
		O output = $this.getTransitionOutput(transition);
		T newTrans = $this.createTransition(succ, output);
		return newTrans;
	}

	@Override
	public O getOutput(S state, I input) {
		return AbstractDeterministicTransOutTS.getOutput(this, state, input);
	}

	@Override
	public boolean trace(Iterable<? extends I> input, List<? super O> output) {
		return AbstractDeterministicTransOutTS.trace(this, input, output);
	}

	@Override
	public boolean trace(S state, Iterable<? extends I> input, List<? super O> output) {
		return AbstractDeterministicTransOutTS.trace(this, state, input, output);
	}

	@Override
	public Word<O> computeOutput(Iterable<I> input) {
		return AbstractTransOutAutomaton.computeOutput(this, input);
	}

	@Override
	public Void getStateProperty(S state) {
		return getStateProperty(this, state);
	}

	@Override
	public O getTransitionProperty(T transition) {
		return getTransitionProperty(this, transition);
	}

	@Override
	public Word<O> computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractTransOutAutomaton.computeSuffixOutput(this, prefix, suffix);
	}
}
