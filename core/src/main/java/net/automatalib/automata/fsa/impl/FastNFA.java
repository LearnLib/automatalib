/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
