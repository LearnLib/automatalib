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
package net.automatalib.automata.fsa.abstractimpl;

import net.automatalib.automata.abstractimpl.AbstractAutomaton;
import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.commons.util.collections.IterableUtil;


public abstract class AbstractFSA<S, I> extends AbstractAutomaton<S,I,S> implements FiniteStateAcceptor<S, I> {
	
	public static <S,I> Boolean getStateProperty(FiniteStateAcceptor<S,I> $this, S state) {
		return Boolean.valueOf($this.isAccepting(state));
	}
	
	public static <S,I> Void getTransitionProperty(FiniteStateAcceptor<S,I> $this, S transition) {
		return null;
	}
	
	public static <S,I> S getSuccessor(FiniteStateAcceptor<S,I> $this, S transition) {
		return transition;
	}

	
	public static <S,I> Boolean computeOutput(FiniteStateAcceptor<S,I> $this, Iterable<? extends I> input) {
		return $this.accepts(input);
	}
	
	public static <S,I> Boolean computeSuffixOutput(FiniteStateAcceptor<S,I> $this, Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		Iterable<I> input = IterableUtil.concat(prefix, suffix);
		return $this.computeOutput(input);
	}
	
	@Override
	public Boolean getStateProperty(S state) {
		return getStateProperty(this, state);
	}
	
	@Override
	public Void getTransitionProperty(S transition) {
		return getTransitionProperty(this, transition);
	}
	
	@Override
	public S getSuccessor(S transition) {
		return getSuccessor(this, transition);
	}
	
	@Override
	public Boolean computeOutput(Iterable<? extends I> input) {
		return computeOutput(this, input);
	}
	
	@Override
	public Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		return computeSuffixOutput(this, prefix, suffix);
	}
	
}
