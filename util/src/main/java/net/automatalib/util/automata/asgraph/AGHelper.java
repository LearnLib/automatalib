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
package net.automatalib.util.automata.asgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.graphs.TransitionEdge;



public class AGHelper {

	
	public static <S,I,T> Collection<TransitionEdge<I,T>> outgoingEdges(Automaton<S,I,T> aut, S state, Collection<? extends I> inputAlphabet) {
		List<TransitionEdge<I,T>> result
			= new ArrayList<>();
		
		
		for(I input : inputAlphabet) {
			Collection<? extends T> transitions = aut.getTransitions(state, input);
			if(transitions == null)
				continue;
			for(T t : transitions)
				result.add(new TransitionEdge<>(input, t));
		}
		
		return result;
	}
	
	
}
