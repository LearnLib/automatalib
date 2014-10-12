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

import net.automatalib.automata.base.fast.FastMutableDet;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.words.Alphabet;

public final class FastDFA<I> extends FastMutableDet<FastDFAState, I, FastDFAState, Boolean, Void>
		implements MutableDFA<FastDFAState,I> {

	public FastDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}
	
	protected FastDFAState createState(boolean accepting) {
		FastDFAState s = new FastDFAState(inputAlphabet.size(),
				accepting);
		return s;
	}

	@Override
	protected FastDFAState createState(Boolean accepting) {
		boolean acc = (accepting != null) ? accepting.booleanValue() : false;
		return createState(acc);
	}

	@Override
	public boolean isAccepting(FastDFAState state) {
		return state.isAccepting();
	}


	@Override
	public FastDFAState addState(boolean accepting) {
		FastDFAState s = addState(Boolean.valueOf(accepting));
		return s;
	}

	@Override
	public void setAccepting(FastDFAState state, boolean accepting) {
		state.setAccepting(accepting);
	}

}
