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
package net.automatalib.automata.fsa.impl;

import net.automatalib.automata.base.fast.FastMutableDet;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.automata.fsa.abstractimpl.AbstractMutableFSA;
import net.automatalib.automata.graphs.AbstractAutomatonGraph;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Alphabet;

public final class FastDFA<I> extends FastMutableDet<FastDFAState, I, FastDFAState, Boolean, Void>
		implements MutableDFA<FastDFAState,I> {

	public FastDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}

	

	@Override
	public FastDFAState getSuccessor(FastDFAState transition) {
		return AbstractFSA.getSuccessor(this, transition);
	}

	@Override
	public <V> MutableMapping<FastDFAState, V> createStaticNodeMapping() {
		return AbstractAutomatonGraph.createStaticNodeMapping(this);
	}

	@Override
	public <V> MutableMapping<FastDFAState, V> createDynamicNodeMapping() {
		return AbstractAutomatonGraph.createDynamicNodeMapping(this);
	}

	@Override
	public Boolean getStateProperty(FastDFAState state) {
		return AbstractFSA.getStateProperty(this, state);
	}

	@Override
	public Void getTransitionProperty(FastDFAState transition) {
		return AbstractFSA.getTransitionProperty(this, transition);
	}

	@Override
	public void setStateProperty(FastDFAState state, Boolean property) {
		AbstractMutableFSA.setStateProperty(this, state, property);
	}

	@Override
	public void setTransitionProperty(FastDFAState transition, Void property) {
		AbstractMutableFSA.setTransitionProperty(this, transition, property);
	}

	@Override
	public FastDFAState createTransition(FastDFAState successor, Void properties) {
		return AbstractMutableFSA.createTransition(this, successor, properties);
	}

	@Override
	public FastDFAState copyTransition(FastDFAState trans, FastDFAState succ) {
		return AbstractMutableFSA.copyTransition(this, trans, succ);
	}

	@Override
	protected FastDFAState createState(Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		return createState(acc);
	}
	
	protected FastDFAState createState(boolean accepting) {
		FastDFAState s = new FastDFAState(inputAlphabet.size(),
				accepting);
		return s;
	}



	@Override
	public boolean isAccepting(FastDFAState state) {
		return state.isAccepting();
	}



	@Override
	public boolean accepts(Iterable<I> input) {
		return AbstractDFA.accepts(this, input);
	}



	@Override
	public Boolean computeSuffixOutput(Iterable<I> prefix, Iterable<I> suffix) {
		return AbstractFSA.computeSuffixOutput(this, prefix, suffix);
	}



	@Override
	public Boolean computeOutput(Iterable<I> input) {
		return AbstractFSA.computeOutput(this, input);
	}



	@Override
	public FastDFAState addState(boolean accepting) {
		FastDFAState s = addState(Boolean.valueOf(accepting));
		return s;
	}



	@Override
	public FastDFAState addInitialState(boolean accepting) {
		return addInitialState(Boolean.valueOf(accepting));
	}



	@Override
	public void setAccepting(FastDFAState state, boolean accepting) {
		state.setAccepting(accepting);
	}



	@Override
	public void flipAcceptance() {
		AbstractMutableFSA.flipAcceptance(this);
	}


}
