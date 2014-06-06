/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.automata.fsa.impl.compact;

import java.util.BitSet;
import java.util.Collection;

import net.automatalib.automata.base.compact.AbstractCompactSimpleNondet;
import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.automata.fsa.abstractimpl.AbstractNFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;

public class CompactNFA<I> extends AbstractCompactSimpleNondet<I, Boolean> implements
		MutableNFA<Integer, I>, DOTPlottableAutomaton<Integer, I, Integer> {
	
	private final BitSet accepting = new BitSet();

	
	public CompactNFA(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
	}

	public CompactNFA(Alphabet<I> alphabet, int stateCapacity,
			float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
	}

	public CompactNFA(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
	}

	public CompactNFA(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public boolean isAccepting(Collection<? extends Integer> states) {
		return AbstractNFA.isAccepting(this, states);
	}

	@Override
	public boolean isAccepting(Integer state) {
		return isAccepting(state.intValue());
	}
	
	public boolean isAccepting(int stateId) {
		return accepting.get(stateId);
	}

	@Override
	public boolean accepts(Iterable<? extends I> input) {
		return AbstractNFA.accepts(this, input);
	}


	@Override
	public Boolean computeSuffixOutput(Iterable<? extends I> prefix,
			Iterable<? extends I> suffix) {
		return AbstractFSA.computeSuffixOutput(this, prefix, suffix);
	}

	@Override
	public Boolean computeOutput(Iterable<? extends I> input) {
		return AbstractFSA.computeOutput(this, input);
	}

	@Override
	public Integer addState(boolean accepting) {
		return addState(Boolean.valueOf(accepting));
	}

	@Override
	public Integer addInitialState(boolean accepting) {
		return addInitialState(Boolean.valueOf(accepting));
	}

	@Override
	public void setAccepting(Integer state, boolean accepting) {
		setAccepting(state, accepting);
	}
	
	public void setAccepting(int stateId, boolean accepting) {
		this.accepting.set(stateId);
	}

	@Override
	public void flipAcceptance() {
		this.accepting.flip(0, size());
	}

	@Override
	public Boolean getStateProperty(int stateId) {
		return isAccepting(stateId);
	}

	@Override
	protected void initState(int stateId, Boolean property) {
		boolean bval = (property != null) ? property.booleanValue() : false;
		this.accepting.set(stateId, bval);
	}

	@Override
	public void setStateProperty(int stateId, Boolean property) {
		setAccepting(stateId, (property != null) ? property.booleanValue() : false); 
	}
	
	@Override
	public void clear() {
		accepting.clear(0, size());
		super.clear();
	}
	
	
	
	
	public CompactDFA<I> determinize() {
		return null;
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, Integer>> getDOTHelper() {
		return new DOTHelperFSA<>(this);
	}
}
