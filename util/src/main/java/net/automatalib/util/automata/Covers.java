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
package net.automatalib.util.automata;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Word;

class Covers {
	
	public static <S,I,T> void cover(DeterministicAutomaton<S, I, T> automaton,
			Collection<? extends I> inputs, Collection<? super Word<I>> states, Collection<? super Word<I>> transitions) {
		
		MutableMapping<S,Word<I>> reach = automaton.createStaticStateMapping();
		
		Queue<S> bfsQueue = new ArrayDeque<S>();
		
		S init = automaton.getInitialState();
		
		reach.put(init, Word.<I>epsilon());
		bfsQueue.offer(init);
		if(states != null)
			states.add(Word.<I>epsilon());
		
		S curr;
		
		while((curr = bfsQueue.poll()) != null) {
			Word<I> as = reach.get(curr);
			
			for(I in : inputs) {
				S succ = automaton.getSuccessor(curr, in);
				if(succ == null)
					continue;
				
				if(reach.get(succ) == null) {
					Word<I> succAs = as.append(in);
					reach.put(succ, succAs);
					if(states != null)
						states.add(succAs);
					bfsQueue.offer(succ);
				}
				else if(transitions != null)
					transitions.add(as.append(in));
			}
		}
	}

}
