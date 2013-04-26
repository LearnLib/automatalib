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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.util.automata.asgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.commons.util.Pair;



public class AGHelper {

	
	public static <S,I,T> Collection<Pair<I,T>> outgoingEdges(Automaton<S,I,T> aut, S state, Collection<? extends I> inputAlphabet) {
		List<Pair<I,T>> result
			= new ArrayList<Pair<I,T>>();
		
		
		for(I input : inputAlphabet) {
			Collection<T> transitions = aut.getTransitions(state, input);
			if(transitions == null)
				continue;
			for(T t : transitions)
				result.add(Pair.make(input, t));
		}
		
		return result;
	}
	
	
}
