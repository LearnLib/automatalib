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

import java.util.Collection;

import net.automatalib.automata.base.fast.FastMutableNondet;
import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.dot.DOTPlottableAutomaton;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.automata.fsa.abstractimpl.AbstractFSA;
import net.automatalib.automata.fsa.abstractimpl.AbstractMutableFSA;
import net.automatalib.automata.fsa.abstractimpl.AbstractNFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.words.Alphabet;


public class FastNFA<I> extends
		FastMutableNondet<FastNFAState, I, FastNFAState, Boolean, Void> implements
		MutableNFA<FastNFAState, I>, DOTPlottableAutomaton<FastNFAState, I, FastNFAState> {
	
	
	public FastNFA(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.fsa.NFA#isAccepting(java.util.Collection)
	 */
	@Override
	public boolean isAccepting(Collection<? extends FastNFAState> states) {
		return AbstractNFA.isAccepting(this, states);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public FastNFAState getSuccessor(FastNFAState transition) {
		return AbstractNFA.getSuccessor(this, transition);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.ts.acceptors.AcceptorTS#accepts(java.lang.Iterable)
	 */
	@Override
	public boolean accepts(Iterable<? extends I> input) {
		return AbstractNFA.accepts(this, input);
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
	 * @see de.ls5.automata.MutableAutomaton#setStateProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setStateProperty(FastNFAState state, Boolean property) {
		AbstractMutableFSA.setStateProperty(this, state, property);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#setTransitionProperty(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setTransitionProperty(FastNFAState transition, Void property) {
		AbstractMutableFSA.setTransitionProperty(this, transition, property);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#createTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FastNFAState createTransition(FastNFAState successor, Void properties) {
		return AbstractMutableFSA.createTransition(this, successor, properties);
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.automata.MutableAutomaton#copyTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public FastNFAState copyTransition(FastNFAState trans, FastNFAState succ) {
		return AbstractMutableFSA.copyTransition(this, trans, succ);
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
	 * @see net.automatalib.ts.UniversalTransitionSystem#getStateProperty(java.lang.Object)
	 */
	@Override
	public Boolean getStateProperty(FastNFAState state) {
		return AbstractFSA.getStateProperty(this, state);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.UniversalTransitionSystem#getTransitionProperty(java.lang.Object)
	 */
	@Override
	public Void getTransitionProperty(FastNFAState transition) {
		return AbstractFSA.getTransitionProperty(this, transition);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.fsa.MutableFSA#flipAcceptance()
	 */
	@Override
	public void flipAcceptance() {
		AbstractMutableFSA.flipAcceptance(this);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.SuffixOutput#computeSuffixOutput(java.lang.Iterable, java.lang.Iterable)
	 */
	@Override
	public Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		return AbstractFSA.computeSuffixOutput(this, prefix, suffix);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.Output#computeOutput(java.lang.Iterable)
	 */
	@Override
	public Boolean computeOutput(Iterable<? extends I> input) {
		return AbstractFSA.computeOutput(this, input);
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


	@Override
	public GraphDOTHelper<FastNFAState, TransitionEdge<I, FastNFAState>> getDOTHelper() {
		return new DOTHelperFSA<>(this);
	}


}
