/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.automata.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.dot.DefaultDOTHelperAutomaton;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class AutomatonGraphView<S, I, T, A extends Automaton<S,I,T>> extends AbstractAutomatonGraphView<S,A,TransitionEdge<I,T>> {
	
	public static <S,I,T> Collection<TransitionEdge<I,T>> createTransitionEdges(Automaton<S,I,T> automaton, Collection<? extends I> inputs, S state) {
		List<TransitionEdge<I,T>> result
			= new ArrayList<TransitionEdge<I,T>>();
	
		
		for(I input : inputs) {
			Collection<? extends T> transitions = automaton.getTransitions(state, input);
			for(T t : transitions) {
				result.add(new TransitionEdge<>(input, t));
			}
		}
		
		return result;
	}
	
	public static <S,I,T,A extends Automaton<S,I,T>>
	AutomatonGraphView<S,I,T,A> create(A automaton, Collection<? extends I> inputs) {
		return new AutomatonGraphView<>(automaton, inputs);
	}
	
	public static <S,I,T,A extends Automaton<S,I,T> & InputAlphabetHolder<I>>
	AutomatonGraphView<S,I,T,A> create(A automaton) {
		return new AutomatonGraphView<>(automaton, automaton.getInputAlphabet());
	}
	
	
	protected final Collection<? extends I> inputs;
	
	public AutomatonGraphView(A automaton, Collection<? extends I> inputs) {
		super(automaton);
		this.inputs = inputs;
	}

	@Override
	public Collection<? extends TransitionEdge<I, T>> getOutgoingEdges(S node) {
		return createTransitionEdges(automaton, inputs, node);
	}

	@Override
	public S getTarget(TransitionEdge<I, T> edge) {
		return automaton.getSuccessor(edge.getTransition());
	}

	@Override
	public GraphDOTHelper<S, ? super TransitionEdge<I,T>> getGraphDOTHelper() {
		return new DefaultDOTHelperAutomaton<>(automaton);
	}
}
