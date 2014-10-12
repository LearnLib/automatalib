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
package net.automatalib.automata.fsa.impl;

import net.automatalib.automata.base.fast.FastMutableNondet;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.words.Alphabet;


public class FastNFA<I> extends
		FastMutableNondet<FastNFAState, I, FastNFAState, Boolean, Void> implements
		MutableNFA<FastNFAState, I> {
	
	
	public FastNFA(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.acceptors.AcceptorTS#isAccepting(java.lang.Object)
	 */
	@Override
	public boolean isAccepting(FastNFAState state) {
		return state.isAccepting();
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.fsa.MutableFiniteStateAcceptor#setAccepting(java.lang.Object, boolean)
	 */
	@Override
	public void setAccepting(FastNFAState state, boolean accepting) {
		state.setAccepting(accepting);
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.base.fast.FastMutableNondet#createState(java.lang.Object)
	 */
	@Override
	protected FastNFAState createState(Boolean property) {
		return new FastNFAState(inputAlphabet.size(),
				WrapperUtil.booleanValue(property));
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.fsa.MutableFSA#addState(boolean)
	 */
	@Override
	public FastNFAState addState(boolean accepting) {
		return addState(Boolean.valueOf(accepting));
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.fsa.MutableFSA#addInitialState(boolean)
	 */
	@Override
	public FastNFAState addInitialState(boolean accepting) {
		return addInitialState(Boolean.valueOf(accepting));
	}

}
