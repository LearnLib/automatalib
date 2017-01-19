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
package net.automatalib.util.automata.equivalence;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.IntDisjointSets;
import net.automatalib.commons.util.UnionFindRemSP;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class NearLinearEquivalenceTest<I> {
	
	private static final class Record<S,S2,I> {
		private final S state1;
		private final S2 state2;
		private final I reachedBy;
		private final Record<S,S2,I> reachedFrom;
		private final int depth;
		
		public Record(S state1, S2 state2) {
			this(state1, state2, null, null);
		}
		
		public Record(S state1, S2 state2, I reachedBy, Record<S,S2,I> reachedFrom) {
			this.state1 = state1;
			this.state2 = state2;
			this.reachedBy = reachedBy;
			this.reachedFrom = reachedFrom;
			this.depth = (reachedFrom != null) ? reachedFrom.depth + 1 : 0;
		}
	}
	
	private static final class IntRecord {
		private final int state1;
		private final int state2;
		private final int reachedBy;
		private final IntRecord reachedFrom;
		private final int depth;
		
		public IntRecord(int state1, int state2) {
			this(state1, state2, -1, null);
		}
		
		public IntRecord(int state1, int state2, int reachedBy, IntRecord reachedFrom) {
			this.state1 = state1;
			this.state2 = state2;
			this.reachedBy = reachedBy;
			this.reachedFrom = reachedFrom;
			this.depth = (reachedFrom != null) ? reachedFrom.depth + 1 : 0;
		}
	}
	
	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> target;
	
	public NearLinearEquivalenceTest(UniversalDeterministicAutomaton<?, I, ?, ?, ?> target) {
		this.target = target;
	}
	
	public Word<I> findSeparatingWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> other, Collection<? extends I> inputs) {
		return findSeparatingWord(target, other, inputs);
	}
	
	@SuppressWarnings("unchecked")
	public static <S,S2,I,T,T2> Word<I> findSeparatingWord(UniversalDeterministicAutomaton<S,I,T,?,?> target,
			UniversalDeterministicAutomaton<S2,I,T2,?,?> other, Collection<? extends I> inputs) {
		
		if (inputs instanceof Alphabet && target instanceof InputAlphabetHolder && other instanceof InputAlphabetHolder) {
			Alphabet<I> alphabet = (Alphabet<I>) inputs;
			Alphabet<I> targetAlphabet = ((InputAlphabetHolder<I>) target).getInputAlphabet();
			if (alphabet.equals(targetAlphabet)) {
				Alphabet<I> otherAlphabet = ((InputAlphabetHolder<I>) other).getInputAlphabet();
				if (alphabet.equals(otherAlphabet)) {
					return findSeparatingWord(target, other, alphabet);
				}
			}
		}
		int targetStates = target.size();
		IntDisjointSets uf = new UnionFindRemSP(targetStates + other.size());
		
		
		S init1 = target.getInitialState();
		S2 init2 = other.getInitialState();
		
		Object sprop1 = target.getStateProperty(init1);
		Object sprop2 = other.getStateProperty(init2);
		
		if(!Objects.equals(sprop1, sprop2))
			return Word.epsilon();
		
		StateIDs<S> targetStateIds = target.stateIDs();
		StateIDs<S2> otherStateIds = other.stateIDs();
		
		int id1 = targetStateIds.getStateId(init1);
		int id2 = otherStateIds.getStateId(init2) + targetStates;
		
		uf.link(id1, id2);
		
		Queue<Record<S,S2,I>> queue = new ArrayDeque<Record<S,S2,I>>();
		
		queue.add(new Record<S,S2,I>(init1, init2));
		
		I lastSym = null;
		
		Record<S,S2,I> current;
		
explore:while((current = queue.poll()) != null) {
			S state1 = current.state1;
			S2 state2 = current.state2;
			
			for(I sym : inputs) {
				T trans1 = target.getTransition(state1, sym);
				T2 trans2 = other.getTransition(state2, sym);
				
				if(trans1 == null) {
					if(trans2 == null)
						continue;
					lastSym = sym;
					break explore;
				}
				else if(trans2 == null) {
					lastSym = sym;
					break explore;
				}
				
				Object tprop1 = target.getTransitionProperty(trans1);
				Object tprop2 = other.getTransitionProperty(trans2);
				
				if(!Objects.equals(tprop1, tprop2)) {
					lastSym = sym;
					break explore;
				}
				
				S succ1 = target.getSuccessor(trans1);
				S2 succ2 = other.getSuccessor(trans2);
				
				id1 = targetStateIds.getStateId(succ1);
				id2 = otherStateIds.getStateId(succ2) + targetStates;
				
				if (!uf.union(id1, id2)) {
					continue;
				}
				
				
				sprop1 = target.getStateProperty(succ1);
				sprop2 = other.getStateProperty(succ2);
				
				if(!Objects.equals(sprop1, sprop2)) {
					lastSym = sym;
					break explore;
				}
				
				queue.add(new Record<>(succ1, succ2, sym, current));
			}
        }
		
		if(current == null)
			return null;
		
		int ceLength = current.depth;
		if(lastSym != null)
			ceLength++;
		
		WordBuilder<I> wb = new WordBuilder<I>(null, ceLength);
		
		int index = ceLength;
		
		if(lastSym != null)
			wb.setSymbol(--index, lastSym);
		
		while(current.reachedFrom != null) {
			wb.setSymbol(--index, current.reachedBy);
			current = current.reachedFrom;
		}
		
		return wb.toWord();
	}
	
	public static <S,S2,I,T,T2> Word<I> findSeparatingWord(UniversalDeterministicAutomaton<S,I,T,?,?> target,
			UniversalDeterministicAutomaton<S2,I,T2,?,?> other, Alphabet<I> inputs) {
		int targetStates = target.size();
		IntDisjointSets uf = new UnionFindRemSP(targetStates + other.size());
		
		int numInputs = inputs.size();
		UniversalDeterministicAutomaton.FullIntAbstraction<T, ?, ?> absTarget = target.fullIntAbstraction(inputs);
		UniversalDeterministicAutomaton.FullIntAbstraction<T2, ?, ?> absOther = other.fullIntAbstraction(inputs);
		
		int init1 = absTarget.getIntInitialState();
		int init2 = absOther.getIntInitialState();
		
		Object sprop1 = absTarget.getStateProperty(init1);
		Object sprop2 = absOther.getStateProperty(init2);
		
		if(!Objects.equals(sprop1, sprop2))
			return Word.epsilon();
		
		int id1 = init1;
		int id2 = targetStates + init2;
				
		uf.link(id1, id2);
		
		Queue<IntRecord> queue = new ArrayDeque<>();
		
		queue.add(new IntRecord(init1, init2));
		
		int lastSym = -1;
		
		IntRecord current;
		
explore:while((current = queue.poll()) != null) {
			int state1 = current.state1;
			int state2 = current.state2;
			
			for(int sym = 0; sym < numInputs; sym++) {
				T trans1 = absTarget.getTransition(state1, sym);
				T2 trans2 = absOther.getTransition(state2, sym);
				
				if(trans1 == null) {
					if(trans2 == null)
						continue;
					lastSym = sym;
					break explore;
				}
				else if(trans2 == null) {
					lastSym = sym;
					break explore;
				}
				
				Object tprop1 = target.getTransitionProperty(trans1);
				Object tprop2 = other.getTransitionProperty(trans2);
				
				if(!Objects.equals(tprop1, tprop2)) {
					lastSym = sym;
					break explore;
				}
				
				int succ1 = absTarget.getIntSuccessor(trans1);
				int succ2 = absOther.getIntSuccessor(trans2);
				
				id1 = succ1;
				id2 = succ2 + targetStates;
				
				if (!uf.union(id1, id2)) {
					continue;
				}
				
				sprop1 = absTarget.getStateProperty(succ1);
				sprop2 = absOther.getStateProperty(succ2);
				
				if(!Objects.equals(sprop1, sprop2)) {
					lastSym = sym;
					break explore;
				}
				
				queue.add(new IntRecord(succ1, succ2, sym, current));
			}
        }
		
		if(current == null)
			return null;
		
		int ceLength = current.depth;
		if(lastSym != -1)
			ceLength++;
		
		WordBuilder<I> wb = new WordBuilder<I>(null, ceLength);
		
		int index = ceLength;
		
		if(lastSym != -1)
			wb.setSymbol(--index, inputs.getSymbol(lastSym));
		
		while(current.reachedFrom != null) {
			wb.setSymbol(--index, inputs.getSymbol(current.reachedBy));
			current = current.reachedFrom;
		}
		
		return wb.toWord();
	}

	public static <S, I, T> Word<I> findSeparatingWord(final UniversalDeterministicAutomaton<S, I, T, ?, ?> target,
													   final S init1,
													   final S init2,
													   final Collection<? extends I> inputs) {
		return findSeparatingWord(target, init1, init2, inputs, false);
	}

	/**
	 * Find a separating word for two states in a given automaton.
	 *
	 * @param target                     the automaton
	 * @param init1                      the first state
	 * @param init2                      the second state
	 * @param inputs                     the inputs to consider for a separating word
	 * @param ignoreUndefinedTransitions if {@code true}, undefined transitions are not considered to distinguish two
	 *                                   states, if {@code false} an undefined and defined transition are considered to
	 *                                   distinguish two states
	 * @param <S>                        automaton state type
	 * @param <I>                        input alphabet type
	 * @param <T>                        automaton transition type
	 * @return A word separating the two states, {@code null} if no such word can be found
	 */
	public static <S, I, T> Word<I> findSeparatingWord(final UniversalDeterministicAutomaton<S, I, T, ?, ?> target,
													   final S init1,
													   final S init2,
													   final Collection<? extends I> inputs,
													   final boolean ignoreUndefinedTransitions) {

		IntDisjointSets uf = new UnionFindRemSP(target.size());
		
		Object sprop1 = target.getStateProperty(init1);
		Object sprop2 = target.getStateProperty(init2);
		
		if(!Objects.equals(sprop1, sprop2))
			return Word.epsilon();
		
		StateIDs<S> stateIds = target.stateIDs();
		
		int id1 = stateIds.getStateId(init1), id2 = stateIds.getStateId(init2);
		
		uf.link(id1, id2);
		
		Queue<Record<S,S,I>> queue = new ArrayDeque<Record<S,S,I>>();
		
		queue.add(new Record<S,S,I>(init1, init2));
		
		I lastSym = null;
		Record<S,S,I> current;
		
explore:while((current = queue.poll()) != null) {
			S state1 = current.state1;
			S state2 = current.state2;
			
			for(I sym : inputs) {
				T trans1 = target.getTransition(state1, sym);
				T trans2 = target.getTransition(state2, sym);

				if(ignoreUndefinedTransitions && (trans1 == null || trans2 == null)) {
					continue;
				}
				else if(trans1 == null) {
					if(trans2 == null)
						continue;
					lastSym = sym;
					break explore;
				}
				else if(trans2 == null) {
					lastSym = sym;
					break explore;
				}
				
				Object tprop1 = target.getTransitionProperty(trans1);
				Object tprop2 = target.getTransitionProperty(trans2);
				
				if(!Objects.equals(tprop1, tprop2)) {
					lastSym = sym;
					break explore;
				}
				
				S succ1 = target.getSuccessor(trans1);
				S succ2 = target.getSuccessor(trans2);
				
				id1 = stateIds.getStateId(succ1);
				id2 = stateIds.getStateId(succ2);
				
				int r1 = uf.find(id1), r2 = uf.find(id2);
				
				if(r1 == r2)
					continue;
				
				
				sprop1 = target.getStateProperty(succ1);
				sprop2 = target.getStateProperty(succ2);
				
				if(!Objects.equals(sprop1, sprop2)) {
					lastSym = sym;
					break explore;
				}
				
				uf.link(r1, r2);
				
				queue.add(new Record<>(succ1, succ2, sym, current));
			}
        }
		
		if(current == null)
			return null;
		
		int ceLength = current.depth;
		if(lastSym != null)
			ceLength++;
		
		WordBuilder<I> wb = new WordBuilder<I>(null, ceLength);
		
		int index = ceLength;
		
		if(lastSym != null)
			wb.setSymbol(--index, lastSym);
		
		while(current.reachedFrom != null) {
			wb.setSymbol(--index, current.reachedBy);
			current = current.reachedFrom;
		}
		
		return wb.toWord();
	}
}
