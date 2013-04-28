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
package net.automatalib.util.automata.equivalence;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;



public class DeterministicEquivalenceTest<I> {
	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
	
	public DeterministicEquivalenceTest(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference) {
		this.reference = reference;
	}
	
	public Word<I> findSeparatingWord(UniversalDeterministicAutomaton<?,I,?,?,?> other,
			Collection<? extends I> inputs) {
		return findSeparatingWord(reference, other, inputs);
	}
	
	@SuppressWarnings("unchecked")
	public static <I,S,T,S2,T2>
	Word<I> findSeparatingWord(UniversalDeterministicAutomaton<S,I,T,?,?> reference,
			UniversalDeterministicAutomaton<S2,I,T2,?,?> other,
			Collection<? extends I> inputs) {
		Queue<Pair<S,S2>> bfsQueue = new ArrayDeque<Pair<S,S2>>();
		
		S refInit = reference.getInitialState();
		S2 otherInit = other.getInitialState();
		
		Object refStateProp = reference.getStateProperty(refInit),
				otherStateProp = other.getStateProperty(otherInit);
		
		if(!Objects.equals(refStateProp, otherStateProp))
			return Word.epsilon();
		
		bfsQueue.offer(Pair.make(refInit, otherInit));
		
		int refSize = reference.size();
		int totalStates = refSize * other.size();
		
		StateIDs<S> refStateIds = reference.stateIDs();
		StateIDs<S2> otherStateIds = other.stateIDs();
		
		Pair<S,S2> currPair = null;
		int lastId = otherStateIds.getStateId(otherInit) * refSize + refStateIds.getStateId(refInit);
		
		Pair<Integer,I>[] preds = (Pair<Integer,I>[])new Pair<?,?>[totalStates];
		preds[lastId] = Pair.make(-1, null);
		
		int currDepth = 0;
		int inCurrDepth = 1;
		int inNextDepth = 0;
		
		I lastSym = null;
		
bfs:	while((currPair = bfsQueue.poll()) != null) {
			S refState = currPair.getFirst();
			S2 otherState = currPair.getSecond();
			
			
			int currId = otherStateIds.getStateId(otherState) * refSize + refStateIds.getStateId(refState);
			lastId = currId;
			
			
			for(I in : inputs) {
				lastSym = in;
				T refTrans = reference.getTransition(refState, in);
				T2 otherTrans = other.getTransition(otherState, in);
				
				if((refTrans == null || otherTrans == null) && refTrans != otherTrans)
					break bfs;
				
				Object refProp = reference.getTransitionProperty(refTrans);
				Object otherProp = other.getTransitionProperty(otherTrans);
				if(!Objects.equals(refProp, otherProp))
					break bfs;
				
				
				S refSucc = reference.getSuccessor(refTrans);
				S2 otherSucc = other.getSuccessor(otherTrans);
				
				int succId = otherStateIds.getStateId(otherSucc) * refSize + refStateIds.getStateId(refSucc);
				
				if(preds[succId] == null) {
					refStateProp = reference.getStateProperty(refSucc);
					otherStateProp = other.getStateProperty(otherSucc);
					
					if(!Objects.equals(refStateProp, otherStateProp))
						break bfs;
					
					preds[succId] = Pair.make(currId, in);
					bfsQueue.offer(Pair.make(refSucc, otherSucc));
					inNextDepth++;
				}
			}
			
			lastSym = null;
			
			
			// Next level in BFS reached
			if(--inCurrDepth == 0) {
				inCurrDepth = inNextDepth;
				inNextDepth = 0;
				currDepth++;
			}
		}
		
		if(lastSym == null)
			return null;
		
		WordBuilder<I> sep = new WordBuilder<I>(null, currDepth+1);
		int index = currDepth;
		sep.setSymbol(index--, lastSym);
		
		Pair<Integer,I> pred = preds[lastId];
		I sym = pred.getSecond();
		while(sym != null) {
			sep.setSymbol(index--, sym);
			pred = preds[pred.getFirst()];
			sym = pred.getSecond();
		}
		
		
		return sep.toWord();
	} 
}
