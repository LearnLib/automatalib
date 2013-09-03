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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.util.automata.asgraph.AutomatonAsGraph;
import net.automatalib.util.automata.asgraph.UniversalAutomatonAsGraph;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import net.automatalib.util.automata.equivalence.NearLinearEquivalenceTest;
import net.automatalib.util.minimizer.Block;
import net.automatalib.util.minimizer.BlockMap;
import net.automatalib.util.minimizer.MinimizationResult;
import net.automatalib.util.minimizer.Minimizer;
import net.automatalib.words.Word;

public class Automata {

	public static <S, I, T>
	Graph<S, TransitionEdge<I, T>> asGraph(
			Automaton<S, I, T> automaton, Collection<? extends I> inputs) {
		return new AutomatonAsGraph<S, I, T,Automaton<S,I,T>>(automaton, inputs);
	}
	
	public static <S,I,T,SP,TP>
	UniversalGraph<S,TransitionEdge<I,T>,SP,TransitionEdge.Property<I,TP>> asUniversalGraph(
			UniversalAutomaton<S, I, T, SP, TP> automaton, Collection<? extends I> inputs) {
		return new UniversalAutomatonAsGraph<S, I, T, SP, TP, UniversalAutomaton<S,I,T,SP,TP>>(automaton, inputs);
	}
	

	public static <S, I, T, SP, TP, SO, TO, A extends MutableDeterministic<SO, ? super I, TO, ? super SP, ? super TP>>
	A minimize(
			UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
			Collection<? extends I> inputs,
			A output) {

		UniversalGraph<S, TransitionEdge<I,T>, SP, TransitionEdge.Property<I,TP>> aag = asUniversalGraph(automaton, inputs);
		
		MinimizationResult<S, TransitionEdge.Property<I,TP>> mr = Minimizer.minimize(aag, Collections.singleton(automaton.getInitialState()));
		output.clear();
		
		S init = automaton.getInitialState();
		Block<S,TransitionEdge.Property<I,TP>> initBlock = mr.getBlockForState(init);
		BlockMap<SO> bm = new BlockMap<SO>(mr);
		
		for(Block<S, TransitionEdge.Property<I,TP>> block : mr.getBlocks()) {
			S rep = mr.getRepresentative(block);
			SO state;
			SP repProp = automaton.getStateProperty(rep);
			if(block == initBlock)
				state = output.addInitialState(repProp);
			else
				state = output.addState(repProp);
			bm.put(block, state);
		}
		
		for(Block<S,TransitionEdge.Property<I,TP>> block : mr.getBlocks()) {
			S rep = mr.getRepresentative(block);
			SO state = bm.get(block);
			for(I input : inputs) {
				T trans = automaton.getTransition(rep, input);
				TP prop = automaton.getTransitionProperty(trans);
				S oldSucc = automaton.getSuccessor(trans);
				Block<S,TransitionEdge.Property<I,TP>> succBlock = mr.getBlockForState(oldSucc);
				SO newSucc = bm.get(succBlock);
				output.addTransition(state, input, newSucc, prop);
			}
		}
		return output;
	}
	
	private static final class ResultTransRecord<TP> {
		public final int targetId;
		public final TP property;
		
		public ResultTransRecord(int targetId, TP property) {
			this.targetId = targetId;
			this.property = property;
		}
	}
	
	private static final class ResultStateRecord<SP,TP> {
		public final SP property;
		public final ResultTransRecord<TP>[] transitions;

		@SuppressWarnings("unchecked")
		public ResultStateRecord(int numInputs, SP property) {
			this.property = property;
			this.transitions = new ResultTransRecord[numInputs];
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>>
	A invasiveMinimize(A automaton, Collection<? extends I> inputs) {
		
		List<? extends I> inputList;
		if(inputs instanceof List)
			inputList = (List<? extends I>)inputs;
		else
			inputList = new ArrayList<I>(inputs);
		
		int numInputs = inputs.size();

		UniversalGraph<S, TransitionEdge<I,T>, SP, TransitionEdge.Property<I,TP>> aag = asUniversalGraph(automaton, inputs);
		
		MinimizationResult<S, TransitionEdge.Property<I,TP>> mr = Minimizer.minimize(aag, automaton.getInitialStates());
		
		S init = automaton.getInitialState();
		int initId = mr.getBlockForState(init).getId();
		
		ResultStateRecord<SP,TP>[] records = new ResultStateRecord[mr.getNumBlocks()];
		
		for(Block<S,TransitionEdge.Property<I,TP>> blk : mr.getBlocks()) {
			int id = blk.getId();
			S state = mr.getRepresentative(blk);
			SP prop = automaton.getStateProperty(state);
			ResultStateRecord<SP,TP> rec = new ResultStateRecord<>(numInputs, prop);
			records[id] = rec;
			for(int i = 0; i < numInputs; i++) {
				I input = inputList.get(i);
				T trans = automaton.getTransition(state, input);
				if(trans == null)
					continue;
				
				TP transProp = automaton.getTransitionProperty(trans);
				S succ = automaton.getSuccessor(trans);
				int tgtId = mr.getBlockForState(succ).getId();
				rec.transitions[i] = new ResultTransRecord<TP>(tgtId, transProp);
			}
		}
		
		automaton.clear();
		
		Object[] states = new Object[records.length];
		for(int i = 0; i < records.length; i++) {
			ResultStateRecord<SP, TP> rec = records[i];
			SP prop = rec.property;
			S state;
			if(i == initId)
				state = automaton.addInitialState(prop);
			else
				state = automaton.addState(prop);
			states[i] = state;
		}
		
		for(int i = 0; i < records.length; i++) {
			ResultStateRecord<SP, TP> rec = records[i];
			S state = (S)states[i];
			
			for(int j = 0; j < numInputs; j++) {
				ResultTransRecord<TP> transRec = rec.transitions[j];
				if(transRec == null)
					continue;
				S succ = (S)states[transRec.targetId];
				I input = inputList.get(j);
				automaton.addTransition(state, input, succ, transRec.property);
			}
		}
		return automaton;
	}
	
	/*
	public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A minimize(
			A automaton, Collection<? extends I> inputs) {
		return minimize(automaton, inputs, automaton);
	}*/
	
	/**
	 * Finds a separating word for two automata. A separating word is a word that exposes
	 * a difference (differing state or transition properties, or a transition undefined in
	 * only one of the automata) between the two automata.
	 * 
	 * @param reference the one automaton to consider
	 * @param other the other automaton to consider
	 * @param inputs the input symbols to consider
	 * @return a separating word, or <tt>null</tt> if no such word could be found.
	 */
	public static <I> Word<I> findSeparatingWord(
			UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference,
			UniversalDeterministicAutomaton<?, I, ?, ?, ?> other,
			Collection<? extends I> inputs) {
		return NearLinearEquivalenceTest.findSeparatingWord(reference, other, inputs);
	}
	
	/**
	 * Finds a separating word for two states in an automaton. A separating word is a word
	 * that exposes a difference (differing state or transition properties, or a transition
	 * undefined in only one of the paths) between the two states.
	 * @param automaton the automaton containing the states
	 * @param state1 the one state
	 * @param state2 the other state
	 * @param inputs the input symbols to consider
	 * @return a separating word, or <tt>null</tt> if no such word could be found
	 */
	public static <S,I> Word<I> findSeparatingWord(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			S state1, S state2,
			Collection<? extends I> inputs) {
		return NearLinearEquivalenceTest.findSeparatingWord(automaton, state1, state2, inputs);
	}
	
	/**
	 * Computes a characterizing set for the given automaton.
	 * <p>
	 * This is a convenience method acting as a shortcut to
	 * {@link CharacterizingSets#findCharacterizingSet(UniversalDeterministicAutomaton, Collection, Collection)}.
	 * 
	 * @see CharacterizingSets
	 * 
	 * @param automaton the automaton for which to determine the characterizing set
	 * @param inputs the input symbols to consider
	 * @param result the collection in which to store the characterizing words
	 */
	public static <I> void characterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs,
			Collection<? super Word<I>> result) {
		CharacterizingSets.findCharacterizingSet(automaton, inputs, result);
	}
	
	/**
	 * Computes a characterizing set, and returns it as a {@link List}.
	 * 
	 * @see CharacterizingSets
	 * 
	 * @param automaton the automaton for which to determine the characterizing set
	 * @param inputs the input symbols to consider
	 * @return a list containing the characterizing words
	 */
	public static <I> List<Word<I>> characterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs) {
		List<Word<I>> result = new ArrayList<Word<I>>();
		characterizingSet(automaton, inputs, result);
		return result;
	}
	
	/**
	 * Computes a characterizing set for a single state.
	 * <p>
	 * This is a convenience method acting as a shortcut to
	 * {@link CharacterizingSets#findCharacterizingSet(UniversalDeterministicAutomaton, Collection, Object, Collection)}.
	 * 
	 * @see CharacterizingSets
	 * 
	 * @param automaton the automaton containing the state
	 * @param inputs the input symbols to consider
	 * @param state the state for which to determine a characterizing set
	 * @param result the collection in which to store the characterizing words
	 */
	public static <S,I> void stateCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs,
			S state,
			Collection<? super Word<I>> result) {
		CharacterizingSets.findCharacterizingSet(automaton, inputs, state, result);
	}
	
	/**
	 * Computes a characterizing set for a single state, and returns it as a {@link List}.
	 * 
	 * @see CharacterizingSets
	 * 
	 * @param automaton the automaton containing the state
	 * @param inputs the input symbols to consider
	 * @param state the state for which to determine a characterizing set
	 * @return a list containing the characterizing words
	 */
	public static <S,I> List<Word<I>> stateCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs,
			S state) {
		List<Word<I>> result = new ArrayList<Word<I>>();
		stateCharacterizingSet(automaton, inputs, state, result);
		return result;
	}
	
	public static <I> void cover(DeterministicAutomaton<?, I, ?> automaton,
			Collection<? extends I> inputs, Collection<? super Word<I>> states, Collection<? super Word<I>> transitions) {
		Covers.cover(automaton, inputs, states, transitions);
	}
	
	public static <I> List<Word<I>> stateCover(DeterministicAutomaton<?, I, ?> automaton,
			Collection<? extends I> inputs) {
		List<Word<I>> states = new ArrayList<Word<I>>(automaton.size());
		cover(automaton, inputs, states, null);
		return states;
	}
	
	public static <I> List<Word<I>> transitionCover(DeterministicAutomaton<?, I, ?> automaton,
			Collection<? extends I> inputs) {
		List<Word<I>> all = new ArrayList<Word<I>>(automaton.size() * inputs.size());
		cover(automaton, inputs, all, all);
		return all;
	}
}
