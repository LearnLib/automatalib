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

import net.automatalib.automata.abstractimpl.AbstractMutableAutomaton;
import net.automatalib.automata.fsa.MutableFSA;

public abstract class AbstractMutableFSA<S, I> extends
		AbstractMutableAutomaton<S, I, S, Boolean, Void> implements
		MutableFSA<S, I> {
	

	public static <S, I> void setStateProperty(MutableFSA<S, I> $this, S state,
			Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		$this.setAccepting(state, acc);
	}

	public static <S, I> void setTransitionProperty(MutableFSA<S, I> $this,
			S transition, Void property) {
	}

	public static <S, I> void flipAcceptance(MutableFSA<S, I> $this) {
		for (S state : $this)
			$this.setAccepting(state, !$this.isAccepting(state));
	}
	
	public static <S,I> S addState(MutableFSA<S,I> $this) {
		return $this.addState(false);
	}
	
	public static <S,I> S addState(MutableFSA<S,I> $this, Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		return $this.addState(acc);
	}
	
	public static <S,I> S addInitialState(MutableFSA<S,I> $this) {
		return $this.addInitialState(false);
	}
	
	public static <S,I> S addInitialState(MutableFSA<S,I> $this, Boolean property) {
		boolean acc = (property != null) ? property.booleanValue() : false;
		return $this.addInitialState(acc);
	}
	

	public static <S,I> S createTransition(MutableFSA<S,I> $this, S successor, Void properties) {
		return successor;
	}

	public static <S,I> S copyTransition(MutableFSA<S,I> $this, S trans, S succ) {
		return succ;
	}
	
	
	public static <S,I> S addInitialState(MutableFSA<S,I> $this, boolean accepting) {
		S init = $this.addState(accepting);
		$this.setInitial(init, true);
		return init;
	}

	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setStateProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateProperty(S state, Boolean property) {
		setStateProperty(this, state, property);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(S transition, Void property) {
		setTransitionProperty(this, transition, property);
	}
	

	
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.fsa.MutableFSA#flipAcceptance()
	 */
	@Override
	public void flipAcceptance() {
		flipAcceptance(this);
	}
	
	@Override
	public S addState() {
		return addState(this);
	}
	
	@Override
	public S addState(Boolean property) {
		return addState(this, property);
	}
	
	@Override
	public S addInitialState() {
		return addInitialState(this);
	}
	
	@Override
	public S addInitialState(Boolean property) {
		return addInitialState(this, property);
	}
	
	@Override
	public S createTransition(S successor, Void properties) {
		return createTransition(this, successor, properties);
	}
	
	@Override
	public S copyTransition(S transition, S successor) {
		return copyTransition(this, transition, successor);
	}
	
	@Override
	public S addInitialState(boolean accepting) {
		return addInitialState(this, accepting);
	}
}
