/* Copyright (C) 2013-2014 TU Dortmund
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
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Word;

class Covers {
	
	private static final class Record<S,I> {
		private final S state;
		private final Word<I> accessSequence;
		private final Set<I> coveredInputs;
		
		public Record(S state, Word<I> accessSequence) {
			this(state, accessSequence, null);
		}
		
		public Record(S state, Word<I> accessSequence, Set<I> coveredInputs) {
			this.state = state;
			this.accessSequence = accessSequence;
			this.coveredInputs = coveredInputs;
		}
	}
	
	public static <S,I,T> void cover(DeterministicAutomaton<S, I, T> automaton,
			Collection<? extends I> inputs, Collection<? super Word<I>> states, Collection<? super Word<I>> transitions) {
		
		MutableMapping<S,Word<I>> reach = automaton.createStaticStateMapping();
		
		Queue<S> bfsQueue = new ArrayDeque<S>();
		
		S init = automaton.getInitialState();
		
		reach.put(init, Word.<I>epsilon());
		bfsQueue.add(init);
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
					if(states != null) {
						states.add(succAs);
					}
					bfsQueue.add(succ);
				}
				else if(transitions != null)
					transitions.add(as.append(in));
			}
		}
	}
	
	public static <S,I,T> boolean incrementalStateCover(
			DeterministicAutomaton<S, I, T> automaton,
			Collection<? extends I> inputs,
			Collection<? extends Word<I>> oldStates,
			Collection<? super Word<I>> newStates) {
		
		MutableMapping<S,Record<S,I>> reach = automaton.createStaticStateMapping();
		
		boolean augmented = false;
		
		Queue<Record<S,I>> bfsQueue = new ArrayDeque<>();
		
		for(Word<I> oldStateAs : oldStates) {
			S state = automaton.getState(oldStateAs);
			if(state == null || reach.get(state) != null) {
				continue; // strange, but we'll ignore it
			}
			Record<S,I> rec = new Record<>(state, oldStateAs);
			reach.put(state, rec);
			bfsQueue.add(rec);
		}
		
		S init = automaton.getInitialState();
		if(reach.get(init) == null) {
			// apparently the initial state was not yet covered
			Record<S,I> rec = new Record<>(init, Word.<I>epsilon());
			reach.put(init, rec);
			bfsQueue.add(rec);
			newStates.add(Word.<I>epsilon());
			augmented = true;
		}
		
		Record<S,I> curr;
		while((curr = bfsQueue.poll()) != null) {
			S state = curr.state;
			Word<I> as = curr.accessSequence;
			
			for(I in : inputs) {
				S succ = automaton.getSuccessor(state, in);
				if(succ == null) {
					continue;
				}
				
				if(reach.get(succ) == null) {
					Word<I> succAs = as.append(in);
					Record<S,I> succRec = new Record<>(succ, succAs);
					reach.put(succ, succRec);
					bfsQueue.add(succRec);
					newStates.add(succAs);
					augmented = true;
				}
			}
		}
		
		return augmented;
	}
	
	public static <S,I,T> boolean incrementalCover(
			DeterministicAutomaton<S, I, T> automaton,
			Collection<? extends I> inputs,
			Collection<? extends Word<I>> oldStateCover,
			Collection<? extends Word<I>> oldTransCover,
			Collection<? super Word<I>> newStateCover,
			Collection<? super Word<I>> newTransCover) {
		
		MutableMapping<S,Record<S,I>> reach = automaton.createStaticStateMapping();
		
		boolean augmented = false;
		
		Queue<Record<S,I>> bfsQueue = new ArrayDeque<>();
		
		// We enforce that the initial state *always* is covered by the empty word,
		// regardless of whether other sequence in oldCover cover it
		S init = automaton.getInitialState();
		
		Record<S,I> initRec = new Record<S,I>(init, Word.<I>epsilon(), new HashSet<I>());
		bfsQueue.add(initRec);
		reach.put(init, initRec);
		
		boolean epsilonAdded = true;
		
		
		
		for(Word<I> oldStateAs : oldStateCover) {
			S state = automaton.getState(oldStateAs);
			if(state == null || reach.get(state) != null) {
				if(oldStateAs.isEmpty()) {
					epsilonAdded = false;
				}
				continue; // strange, but we'll ignore it
			}
			
			Record<S,I> rec = new Record<>(state, oldStateAs, new HashSet<I>());
			bfsQueue.add(rec);
			reach.put(state, rec);
		}
		
		if(epsilonAdded) {
			if(newStateCover != null) {
				newStateCover.add(Word.<I>epsilon());
				augmented = true;
			}
		}
		
		
		// Add transition cover information from *state covers*
		for(Word<I> oldStateAs : oldStateCover) {
			if(oldStateAs.isEmpty()) {
				continue;
			}
			
			Word<I> asPrefix = oldStateAs.prefix(oldStateAs.length() - 1);
			S pred = automaton.getState(asPrefix);
			assert pred != null;
			
			Record<S,I> predRec = reach.get(pred);
			if(predRec == null) {
				throw new IllegalArgumentException("State cover was not prefix-closed: prefix of " + oldStateAs + " not in set");
			}
			I lastSym = oldStateAs.lastSymbol();
			predRec.coveredInputs.add(lastSym);
		}
		
		// Add transition covers
		for(Word<I> oldTransAs : oldTransCover) {
			// Check if this transition now leads to a new state
			S state = automaton.getState(oldTransAs);
			if(state != null) {
				Record<S,I> rec = reach.get(state);
				if(rec == null) {
					// if so, add it to the state cover and to the queue
					rec = new Record<>(state, oldTransAs, new HashSet<I>());
					bfsQueue.add(rec);
					reach.put(state, rec);
					if(newStateCover != null) {
						newStateCover.add(oldTransAs);
						augmented = true;
					}
				}
			}
			
			// In any case, mark the transition as covered
			Word<I> predAs = oldTransAs.prefix(oldTransAs.length() - 1);
			S pred = automaton.getState(predAs);
			if(pred == null) {
				throw new IllegalArgumentException("Invalid transition: prefix of transition " + oldTransAs + " not covered by state cover");
			}
			I lastSym = oldTransAs.lastSymbol();
			Record<S,I> predRec = reach.get(pred);
			predRec.coveredInputs.add(lastSym);
		}
		
		Record<S,I> curr;
		while((curr = bfsQueue.poll()) != null) {
			for(I input : inputs) {
				if(curr.coveredInputs.add(input)) {				
					S succ = automaton.getSuccessor(curr.state, input);
					
					Word<I> newAs = curr.accessSequence.append(input);
					
					if(succ == null) {
						// undefined transition, but still needs to be covered
						if(newTransCover != null) {
							newTransCover.add(newAs);
							augmented = true;
						}
					}
					else {
						Record<S,I> succRec = reach.get(succ);
						
						if(succRec == null) {
							// new state!
							succRec = new Record<>(succ, newAs, new HashSet<I>());
							bfsQueue.add(succRec);
							reach.put(succ, succRec);
							
							if(newStateCover != null) {
								newStateCover.add(newAs);
								augmented = true;
							}
						}
						else {
							// new transition
							if(newTransCover != null) {
								newTransCover.add(newAs);
								augmented = true;
							}
						}
					}
				}
			}
		}
		
		return augmented;
	}
							
					
			

}
