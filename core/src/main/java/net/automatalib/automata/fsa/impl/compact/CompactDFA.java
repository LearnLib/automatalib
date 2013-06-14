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
package net.automatalib.automata.fsa.impl.compact;

import java.util.BitSet;

import net.automatalib.automata.base.compact.AbstractCompactSimpleDet;
import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;

public class CompactDFA<I> extends AbstractCompactSimpleDet<I, Boolean> implements MutableDFA<Integer,I>, DOTPlottableAutomaton<Integer, I, Integer> {
	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	private final BitSet acceptance = new BitSet();
	
	public CompactDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
	}
	
	public CompactDFA(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
	}
	
	@Override
	public void ensureCapacity(int oldCap, int newCap) {
		acceptance.set(newCap);
	}
	
	


	@Override
	public void flipAcceptance() {
		acceptance.flip(0, size());
	}

	

	@Override
	public void clear() {
		acceptance.clear();
		super.clear();
	}


	public void setAccepting(int state, boolean accepting) {
		acceptance.set(state, accepting);
	}

	@Override
	public void setAccepting(Integer state, boolean accepting) {
		setAccepting(state.intValue(), accepting);
	}

	
	@Override
	public Integer addState(boolean accepting) {
		return addState(Boolean.valueOf(accepting));
	}


	public boolean isAccepting(int stateId) {
		return acceptance.get(stateId);
	}
	
	@Override
	public boolean isAccepting(Integer state) {
		return isAccepting(state.intValue());
	}

	@Override
	public boolean accepts(Iterable<? extends I> input) {
		return AbstractDFA.accepts(this, input);
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
	public Boolean getStateProperty(int stateId) {
		return isAccepting(stateId);
	}

	@Override
	public void initState(int stateId, Boolean property) {
		boolean bval = (property == null) ? false : property.booleanValue();
		setAccepting(stateId, bval);
	}

	
	@Override
	public void setStateProperty(int stateId, Boolean property) {
		boolean bval = (property == null) ? false : property.booleanValue();
		setAccepting(stateId, bval);
	}

	@Override
	public Integer addInitialState(boolean accepting) {
		return addInitialState(Boolean.valueOf(accepting));
	}

	@Override
	public GraphDOTHelper<Integer, TransitionEdge<I, Integer>> getDOTHelper() {
		return new DOTHelperFSA<>(this);
	}

}
