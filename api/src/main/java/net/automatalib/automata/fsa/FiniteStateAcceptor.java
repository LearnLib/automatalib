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
package net.automatalib.automata.fsa;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.dot.DOTHelperFSA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.commons.util.collections.IterableUtil;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.ts.acceptors.AcceptorTS;

/**
 * <code>FiniteStateAcceptor</code>s accept regular languages.
 *
 */
public interface FiniteStateAcceptor<S,I> extends AcceptorTS<S, I>,
		UniversalAutomaton<S,I,S,Boolean,Void>, SuffixOutput<I, Boolean> {
	public static final List<Boolean> STATE_PROPERTIES = Arrays.asList(Boolean.FALSE, Boolean.TRUE);
	public static final List<Void> TRANSITION_PROPERTIES = Collections.singletonList(null);
	
	public static class FSAGraphView<S,I,A extends FiniteStateAcceptor<S,I>>
			extends UniversalAutomatonGraphView<S, I, S, Boolean, Void, A> {
		
		public FSAGraphView(A automaton, Collection<? extends I> inputs) {
			super(automaton, inputs);
		}

		@Override
		public GraphDOTHelper<S,? super TransitionEdge<I,S>> getGraphDOTHelper() {
			return new DOTHelperFSA<>(automaton);
		}
	}
	
	@Override
	default public Boolean computeOutput(Iterable<? extends I> input) {
		return accepts(input);
	}
	
	@Override
	default public Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
		Iterable<I> input = IterableUtil.concat(prefix, suffix);
		return computeOutput(input);
	}
	
	@Override
	default public UniversalGraph<S, TransitionEdge<I,S>, Boolean, TransitionEdge.Property<I,Void>>
	transitionGraphView(Collection<? extends I> inputs) {
		return new FSAGraphView<S,I,FiniteStateAcceptor<S,I>>(this, inputs);
	}
}
