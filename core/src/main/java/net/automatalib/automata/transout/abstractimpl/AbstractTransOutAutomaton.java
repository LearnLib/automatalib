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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.automata.transout.abstractimpl;

import java.util.Collection;
import java.util.List;

import net.automatalib.automata.transout.TransitionOutputAutomaton;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;


public abstract class AbstractTransOutAutomaton {
	
	public static <S,I,T,O> Word<O> computeOutput(TransitionOutputAutomaton<S,I,T,O> $this,
			Iterable<I> input) {
		WordBuilder<O> result;
		if(input instanceof Collection)
			result = new WordBuilder<O>(((Collection<I>)input).size());
		else
			result = new WordBuilder<>();
		$this.trace(input, result);
		return result.toWord();
	}
	
	public static <S,I,T,O> Word<O> computeSuffixOutput(TransitionOutputAutomaton<S,I,T,O> $this,
			Iterable<I> prefix, Iterable<I> suffix) {
		WordBuilder<O> result;
		if(suffix instanceof Collection)
			result = new WordBuilder<O>(((Collection<I>)suffix).size());
		else
			result = new WordBuilder<>();
		S state = $this.getState(prefix);
		$this.trace(state, suffix, result);
		return result.toWord();
	}
	
	
	public static <S,I,T,O> void trace(TransitionOutputAutomaton<S,I,T,O> $this,
			Iterable<I> input, List<O> output) {
		$this.trace($this.getInitialState(), input, output);
	}
	
	public static <S,I,T,O> void trace(TransitionOutputAutomaton<S, I, T, O> $this,
			S state, Iterable<I> input, List<O> output) {
		
		for(I sym : input) {
			T trans = $this.getTransition(state, sym);
			O out = $this.getTransitionOutput(trans);
			output.add(out);
			state = $this.getSuccessor(trans);
		}
	}
	
	public static <S,I,T,O> O getOutput(TransitionOutputAutomaton<S, I, T, O> $this,
			S state, I input) {
		T trans = $this.getTransition(state, input);
		if(trans == null)
			return null;
		return $this.getTransitionOutput(trans);
	}
}

