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

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompactSimpleDet;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.words.Alphabet;

public class CompactDFA<I> extends AbstractCompactSimpleDet<I, Boolean> implements
		MutableDFA<Integer,I> {
	public static final class Creator<I> implements AutomatonCreator<CompactDFA<I>, I> {
		@Override
		public CompactDFA<I> createAutomaton(Alphabet<I> alphabet) {
			return new CompactDFA<>(alphabet);
		}
		@Override
		public CompactDFA<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
			return new CompactDFA<>(alphabet, numStates);
		}
	}
	
	public static final float DEFAULT_RESIZE_FACTOR = 1.5f;
	public static final int DEFAULT_INIT_CAPACITY = 11;
	
	private final BitSet acceptance;
	
	public CompactDFA(Alphabet<I> alphabet) {
		super(alphabet);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
		this.acceptance = new BitSet();
	}
	
	public CompactDFA(CompactDFA<I> other) {
		this(other.getInputAlphabet(), other);
	}
	
	protected CompactDFA(Alphabet<I> alphabet, CompactDFA<?> other) {
		super(alphabet, other);
		this.acceptance = (BitSet)other.acceptance.clone();
	}
	
	public <I2> CompactDFA<I2> translate(Alphabet<I2> newAlphabet) {
		if (newAlphabet.size() != alphabetSize) {
			throw new IllegalArgumentException("Alphabet sizes must match, but they do not (old/new): " +
					alphabetSize + " vs. " + newAlphabet.size());
		}
		return new CompactDFA<I2>(newAlphabet, this);
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
		return super.addInitialState(Boolean.valueOf(accepting));
	}

	
}
