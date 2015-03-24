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

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.base.compact.AbstractCompactSimpleNondet;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.words.Alphabet;

public class CompactNFA<I> extends AbstractCompactSimpleNondet<I, Boolean> implements
		MutableNFA<Integer, I> {
	
	public static final class Creator<I> implements AutomatonCreator<CompactNFA<I>, I> {
		@Override
		public CompactNFA<I> createAutomaton(Alphabet<I> alphabet) {
			return new CompactNFA<I>(alphabet);
		}
		@Override
		public CompactNFA<I> createAutomaton(Alphabet<I> alphabet, int numStates) {
			return new CompactNFA<I>(alphabet, numStates);
		}
	}
	
	private final BitSet accepting;

	
	public CompactNFA(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
		this.accepting = new BitSet();
	}

	public CompactNFA(Alphabet<I> alphabet, int stateCapacity,
			float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
		this.accepting = new BitSet();
	}

	public CompactNFA(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
		this.accepting = new BitSet();
	}

	public CompactNFA(Alphabet<I> alphabet) {
		super(alphabet);
		this.accepting = new BitSet();
	}
	
	protected CompactNFA(Alphabet<I> alphabet, CompactNFA<?> other) {
		super(alphabet, other);
		this.accepting = (BitSet) other.accepting.clone();
	}
	
	public <I2> CompactNFA<I2> translate(Alphabet<I2> newAlphabet) {
		if (alphabet.size() != newAlphabet.size()) {
			throw new IllegalArgumentException("Can only translate automata with matching alphabet sizes, found: "
					+ newAlphabet.size() + " (new) vs. " + alphabetSize + " (old)");
		}
		return new CompactNFA<>(newAlphabet, this);
	}

	@Override
	public boolean isAccepting(Integer state) {
		return isAccepting(state.intValue());
	}
	
	public boolean isAccepting(int stateId) {
		return accepting.get(stateId);
	}

	@Override
	public Integer addState(boolean accepting) {
		return addState(Boolean.valueOf(accepting));
	}

	@Override
	public Integer addInitialState(boolean accepting) {
		return super.addInitialState(Boolean.valueOf(accepting));
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

}
