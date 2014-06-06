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
package net.automatalib.util.automata.equivalence;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;


/**
 * Operations for calculating <i>characterizing sets</i>.
 * <p>
 * A characterizing set for a whole automaton is a set <i>W</i> of words such that for every two states
 * <i>s<sub>1</sub></i> and <i>s<sub>2</sub></i>, there exists a word <i>w &isin; W</i> such that
 * <i>w</i> exposes a difference between <i>s<sub>1</sub></i> and <i>s<sub>2</sub></i> (i.e.,
 * either covers a transition with differing property (or not defined in only one case),
 * or reaching a successor state with differing properties), or there exists no such word at all.
 * <p>
 * A characterizing set for a single state <i>s</i> is a set <i>W</i> of words such that
 * for every state <i>t</i>, there exists a word <i>w &isin; W</i> such that <i>w</i> exposes
 * a difference between <i>s</i> and <i>t</i>, or there exists no such word at all.
 * 
 * @author Malte Isberner
 *
 */
public class CharacterizingSets {
	
	private static <S,I,T> List<Object> buildTrace(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			S state,
			Word<I> suffix) {
		if(suffix.isEmpty()) {
			Object prop = automaton.getStateProperty(state);
			return Collections.singletonList(prop);
		}
		List<Object> trace = new ArrayList<Object>(2*suffix.length());
		
		S curr = state;
		
		for(I sym : suffix) {
			T trans = automaton.getTransition(curr, sym);
			
			if(trans == null)
				break;
			
			Object prop = automaton.getTransitionProperty(trans);
			trace.add(prop);
			
			curr = automaton.getSuccessor(trans);
			prop = automaton.getStateProperty(curr);
			trace.add(prop);
		}
		
		return trace;
	}
	
	private static <S,I,T> boolean checkTrace(UniversalDeterministicAutomaton<S, I, T, ?, ?>  automaton,
			S state,
			Word<I> suffix,
			List<Object> trace) {
		
		Iterator<Object> it = trace.iterator();
		S curr = state;
		
		for(I sym : suffix) {
			T trans = automaton.getTransition(curr, sym);
			
			if(!it.hasNext())
				return (trans == null);
			
			Object prop = automaton.getTransitionProperty(trans);
			
			if(!Objects.equals(prop, it.next()))
				return false;
			
			curr = automaton.getSuccessor(trans);
			prop = automaton.getStateProperty(curr);
			
			if(!Objects.equals(prop, it.next()))
				return false;
		}
		
		return true;
	}
	
	private static <S,I,T> List<List<Object>> buildSignature(UniversalDeterministicAutomaton<S,I,T,?,?> automaton,
			List<? extends Word<I>> suffixes,
			S state) {
		List<List<Object>> signature = new ArrayList<>(suffixes.size());
		
		for(Word<I> suffix : suffixes) {
			List<Object> trace = buildTrace(automaton, state, suffix);
			signature.add(trace);
		}
		
		return signature;
	}
	
	private static <S,I,T> void cluster(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			Word<I> suffix,
			Iterator<S> stateIt,
			Map<List<Object>,List<S>> bucketMap) {
		
		while(stateIt.hasNext()) {
			S state = stateIt.next();
			List<Object> trace = buildTrace(automaton, state, suffix);
			List<S> bucket = bucketMap.get(trace);
			if(bucket == null) {
				bucket = new ArrayList<S>();
				bucketMap.put(trace, bucket);
			}
			bucket.add(state);
		}
	}
	
	
	/**
	 * Computes a characterizing set for a specified state in the given automaton. 
	 * @param automaton the automaton containing the state
	 * @param inputs the input alphabets to consider
	 * @param state the state for which to determine the characterizing set
	 * @param result the collection in which to store the characterizing words
	 */
	public static <S,I,T> void findCharacterizingSet(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			Collection<? extends I> inputs,
			S state, Collection<? super Word<I>> result) {
		
		Object prop = automaton.getStateProperty(state);
		
		List<S> currentBlock = new ArrayList<S>();
		
		boolean multipleStateProps = false;
		
		for(S s : automaton) {
			if(Objects.equals(s, state))
				continue;
			
			Object sProp = automaton.getStateProperty(s);
			if(!Objects.equals(sProp, prop))
				multipleStateProps = true;
			else
				currentBlock.add(s);
		}
		
		if(multipleStateProps)
			result.add(Word.<I>epsilon());
		
		while(!currentBlock.isEmpty()) {
			List<S> nextBlock = new ArrayList<S>();
			
			Iterator<S> it = currentBlock.iterator();
			
			Word<I> suffix = null;
			while(it.hasNext() && suffix == null) {
				S s = it.next();
				suffix = Automata.findSeparatingWord(automaton, state, s, inputs);
			}
			
			if(suffix == null)
				return;
			
			
			result.add(suffix);
			
			List<Object> trace = buildTrace(automaton, state, suffix);
			
			while(it.hasNext()) {
				S s = it.next();
				if(checkTrace(automaton, s, suffix, trace))
					nextBlock.add(s);
			}
			
			currentBlock = nextBlock;
		}
	}
	
	/**
	 * Computes a characterizing set for the given automaton. 
	 * @param automaton the automaton for which to determine the characterizing set.
	 * @param inputs the input alphabets to consider
	 * @param result the collection in which to store the characterizing words
	 */
	public static <S,I,T> void findCharacterizingSet(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			Collection<? extends I> inputs,
			Collection<? super Word<I>> result) {
		findIncrementalCharacterizingSet(automaton, inputs, Collections.<Word<I>>emptyList(), result);
	}
	
	private static <S,I,T> Map<Object,List<S>> clusterByProperty(
			UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			List<S> states) {
		Map<Object,List<S>> result = new HashMap<>();
		
		for(S state : states) {
			Object prop = automaton.getStateProperty(state);
			List<S> block = result.get(prop);
			if(block == null) {
				block = new ArrayList<>();
				result.put(prop, block);
			}
			block.add(state);
		}
		
		return result;
	}
	
	private static <S,I,T> boolean epsilonRefine(UniversalDeterministicAutomaton<S,I,T,?,?> automaton,
			Queue<List<S>> blockQueue) {
		
		
		int initialSize = blockQueue.size();
		
		boolean refined = false;
		
		for(int i = 0; i < initialSize; i++) {
			List<S> block = blockQueue.poll();
			if(block.size() <= 1) {
				continue;
			}
			Map<Object,List<S>> propCluster = clusterByProperty(automaton, block);
			if(propCluster.size() > 1) {
				refined = true;
			}
			blockQueue.addAll(propCluster.values());
		}
		
		return refined;
	}
	
	private static <S,I,T> Word<I> refine(
			UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			Collection<? extends I> inputs,
			Queue<List<S>> blockQueue) {
		
		List<S> currBlock;
		while((currBlock = blockQueue.poll()) != null) {
			if(currBlock.size() <= 1) {
				continue; // we cannot split further
			}
			
			Iterator<S> it = currBlock.iterator();
			
			S ref = it.next();
			
			Word<I> suffix = null;
			S state = null;
			while(it.hasNext() && suffix == null) {
				state = it.next();
				suffix = Automata.findSeparatingWord(automaton, ref, state, inputs);
			}
			
			if(suffix != null) {
				int otherBlocks = blockQueue.size();
				
				Map<List<Object>,List<S>> buckets = new HashMap<List<Object>,List<S>>();
				
				List<S> firstBucket = new ArrayList<S>();
				List<S> secondBucket = new ArrayList<S>();
				firstBucket.add(ref);
				buckets.put(buildTrace(automaton, ref, suffix), firstBucket);
				secondBucket.add(state);
				buckets.put(buildTrace(automaton, state, suffix), secondBucket);
				
				cluster(automaton, suffix, it, buckets);
				
				blockQueue.addAll(buckets.values());
				
				
				// Split all other blocks that were in the queue
				for(int i = 0; i < otherBlocks; i++) {
					List<S> otherBlock = blockQueue.poll();
					if(otherBlock.size() > 2) {
						buckets.clear();
						cluster(automaton, suffix, otherBlock.iterator(), buckets);
						blockQueue.addAll(buckets.values());
					}
				}
				
				return suffix;
			}
		}
		return null;
	}
	
	public static <S,I,T> boolean findIncrementalCharacterizingSet(UniversalDeterministicAutomaton<S, I, T, ?, ?> automaton,
			Collection<? extends I> inputs,
			Collection<? extends Word<I>> oldSuffixes,
			Collection<? super Word<I>> newSuffixes) {
		
		boolean refined = false;
		Map<List<List<Object>>,List<S>> initialPartitioning = new HashMap<>();
		
		// We need a list to ensure a stable iteration order
		List<? extends Word<I>> oldSuffixList;
		if(oldSuffixes instanceof List) {
			oldSuffixList = (List<? extends Word<I>>)oldSuffixes;
		}
		else {
			oldSuffixList = new ArrayList<>(oldSuffixes);
		}
		
		Queue<List<S>> blocks = new ArrayDeque<>();
		for(S state : automaton) {
			List<List<Object>> sig = buildSignature(automaton, oldSuffixList, state);
			List<S> block = initialPartitioning.get(sig);
			if(block == null) {
				block = new ArrayList<>();
				blocks.add(block);
				initialPartitioning.put(sig, block);
			}
			block.add(state);
		}
		
		if(!oldSuffixes.contains(Word.epsilon())) {
			if(epsilonRefine(automaton, blocks)) {
				newSuffixes.add(Word.<I>epsilon());
				refined = true;
			}
		}
		
		Word<I> suffix;
		
		while((suffix = refine(automaton, inputs, blocks)) != null) {
			newSuffixes.add(suffix);
			refined = true;
		}
		
		return refined;
	}
	
	
	
}
