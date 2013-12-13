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
package net.automatalib.incremental.dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.graphs.AbstractAutomatonGraph;
import net.automatalib.commons.util.UnionFind;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.IncrementalConstruction;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public abstract class AbstractIncrementalDFABuilder<I> extends
		AbstractDeterministicAutomaton<State, I, State> implements
		UniversalGraph<State, EdgeRecord, Acceptance, I>,
		DOTPlottableGraph<State, EdgeRecord>,
		IncrementalConstruction<DFA<?, I>, I> {

	protected final Map<StateSignature, State> register = new HashMap<>();

	protected final Alphabet<I> inputAlphabet;
	protected final int alphabetSize;
	protected final State init;
	protected State sink;
	
	public AbstractIncrementalDFABuilder(Alphabet<I> inputAlphabet) {
		this.inputAlphabet = inputAlphabet;
		this.alphabetSize = inputAlphabet.size();
		StateSignature sig = new StateSignature(alphabetSize, Acceptance.DONT_KNOW);
		this.init = new State(sig);
		register.put(null, init);
	}
	
	public abstract Acceptance lookup(Word<I> word);
	
	public abstract void insert(Word<I> word, boolean accepting) throws ConflictException;
	
	/**
	 * Inserts a word into the set of accepted words.
	 * @param word the word to insert
	 * @throws ConflictException if the word is already contained in the set of definitely
	 * rejected words
	 */
	public final void insert(Word<I> word) throws ConflictException {
		insert(word, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.abstractimpl.AbstractFiniteDTS#size()
	 */
	@Override
	public int size() {
		return register.size() + ((sink != null) ? 1 : 0);
	}
	
	@Override
	public Collection<State> getNodes() {
		if(sink == null)
			return Collections.unmodifiableCollection(register.values());
		List<State> result = new ArrayList<>(register.size() + 1);
		result.addAll(register.values());
		result.add(sink);
		return result;
	}
	
	@Override
	public Collection<EdgeRecord> getOutgoingEdges(State node) {
		List<EdgeRecord> edges = new ArrayList<EdgeRecord>();
		for(int i = 0; i < alphabetSize; i++) {
			if((sink != null && node == sink) || node.getSuccessor(i) != null)
				edges.add(new EdgeRecord(node, i));
		}
		return edges;
	}
	
	@Override
	public State getTarget(EdgeRecord edge) {
		if(sink != null && edge.source == sink)
			return edge.source;
		return edge.source.getSuccessor(edge.transIdx);
	}
	
	@Override
	public Acceptance getNodeProperty(State node) {
		if(sink != null && node == sink)
			return Acceptance.FALSE;
		return node.getAcceptance();
	}
	
	@Override
	public I getEdgeProperty(EdgeRecord edge) {
		return inputAlphabet.getSymbol(edge.transIdx);
	}
	
	@Override
	public State getTransition(State state, I input) {
		if(sink != null && state == sink)
			return sink;
		int idx = inputAlphabet.getSymbolIndex(input);
		return state.getSuccessor(idx);
	}
	
	@Override
	public State getSuccessor(State transition) {
		return transition;
	}
	
	@Override
	public State getInitialState() {
		return init;
	}
	
	@Override
	public Collection<State> getStates() {
		return getNodes();
	}
	
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}
	
	@Override
	public Word<I> findSeparatingWord(DFA<?, I> target, Collection<? extends I> inputs, boolean omitUndefined) {
		return doFindSeparatingWord(target, inputs, omitUndefined);
	}
	
	@Override
	public DFA<?, I> toAutomaton() {
		CompactDFA<I> result = new CompactDFA<>(inputAlphabet, register.size() + ((sink != null) ? 1 : 0));
		Map<State,Integer> stateMap = new HashMap<>();
		
		for(State s : register.values()) {
			Integer id;
			boolean acc = (s.getAcceptance() == Acceptance.TRUE);
			if(s == init)
				id = result.addInitialState(acc);
			else
				id = result.addState(acc);
			stateMap.put(s, id);
		}
		
		if(sink != null)
			stateMap.put(sink, result.addState(false));
		
		for(Map.Entry<State,Integer> e : stateMap.entrySet()) {
			State s = e.getKey();
			
			Integer srcId = e.getValue();
			for(int i = 0; i < inputAlphabet.size(); i++) {
				State succ;
				if(s == sink)
					succ = sink;
				else {
					succ = s.getSuccessor(i);
					if(succ == null)
						continue;
				}
				I sym = inputAlphabet.getSymbol(i);
				Integer succId = stateMap.get(succ);
				result.addTransition(srcId, sym, succId);
			}
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#hasDefinitiveInformation(net.automatalib.words.Word)
	 */
	@Override
	public boolean hasDefinitiveInformation(Word<I> word) {
		State s = getState(word);
		if(s == null)
			return false;
		if(s == sink)
			return true;
		return (s.getAcceptance() != Acceptance.DONT_KNOW);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DOTPlottableGraph#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<State, EdgeRecord> getGraphDOTHelper() {
		return new DOTHelper(inputAlphabet, init);
	}
	
	
	@Override
	public NodeIDs<State> nodeIDs() {
		return AbstractAutomatonGraph.nodeIDs(this);
	}
	
	@Override
	public <T> MutableMapping<State,T> createStaticNodeMapping() {
		return AbstractAutomatonGraph.createStaticNodeMapping(this);
	}
	
	@Override
	public <T> MutableMapping<State,T> createDynamicNodeMapping() {
		return AbstractAutomatonGraph.createDynamicNodeMapping(this);
	}
	
	private static int getStateId(State s, Map<State,Integer> idMap) {
		Integer id = idMap.get(s);
		if(id != null)
			return id.intValue();
		idMap.put(s, id = idMap.size());
		return id.intValue();
	}
	
	private static final class Record<S,I> {
		public final State state1;
		public final S state2;
		public final I reachedVia;
		public final Record<S,I> reachedFrom;
		public final int depth;
		
		public Record(State state1, S state2) {
			this.state1 = state1;
			this.state2 = state2;
			this.reachedVia = null;
			this.reachedFrom = null;
			this.depth = 0;
		}
		
		public Record(State state1, S state2, I reachedVia, Record<S,I> reachedFrom) {
			this.state1 = state1;
			this.state2 = state2;
			this.reachedVia = reachedVia;
			this.reachedFrom = reachedFrom;
			this.depth = reachedFrom.depth + 1;
		}
	}
	
	private <S> Word<I> doFindSeparatingWord(DFA<S,I> target, Collection<? extends I> inputs, boolean omitUndefined) {
		int thisStates = register.size();
		Map<State,Integer> stateIds = new HashMap<>();
		if(sink != null) {
			stateIds.put(sink, 0);
			thisStates++;
		}
		int targetStates = target.size();
		if(!omitUndefined)
			targetStates++;
		
		UnionFind uf = new UnionFind(thisStates + targetStates);

		State init1 = init;
		S init2 = target.getInitialState();
		
		if(init2 == null && omitUndefined)
			return null;
		
		boolean acc = target.isAccepting(init2);
		if(init1.getAcceptance().conflicts(acc))
			return Word.epsilon();
		
		StateIDs<S> tgtIds = target.stateIDs();
		int id1 = getStateId(init1, stateIds);
		int id2 = ((init2 != null) ? tgtIds.getStateId(init2) : (targetStates - 1)) + thisStates;
		
		uf.link(id1, id2);
		
		Queue<Record<S,I>> queue = new ArrayDeque<>();
		
		queue.offer(new Record<S,I>(init1, init2));
		
		I lastSym = null;
		
		Record<S,I> current;
		
explore:while((current = queue.poll()) != null) {
			State state1 = current.state1;
			S state2 = current.state2;
			
			for(I sym : inputs) {
				S succ2 = (state2 != null) ? target.getSuccessor(state2, sym) : null;
				if(succ2 == null && omitUndefined)
					continue;
				
				int idx = inputAlphabet.getSymbolIndex(sym);
				State succ1 = (state1 != sink) ? state1.getSuccessor(idx) : sink;
				
				if(succ1 == null)
					continue;
				
				id1 = getStateId(succ1, stateIds);
				id2 = ((succ2 != null) ? tgtIds.getStateId(succ2) : (targetStates-1)) + thisStates;
				
				int r1 = uf.find(id1), r2 = uf.find(id2);
				
				if(r1 == r2)
					continue;
				
				if(succ1 == sink) {
					if(succ2 == null)
						continue;
					if(target.isAccepting(succ2)) {
						lastSym = sym;
						break explore;
					}
				}
				else {
					boolean succ2acc = (succ2 != null) ? target.isAccepting(succ2) : false;
					if(succ1.getAcceptance().conflicts(succ2acc)) {
						lastSym = sym;
						break explore;
					}
				}
				
				uf.link(r1, r2);
				
				queue.offer(new Record<>(succ1, succ2, sym, current));
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
			wb.setSymbol(--index, current.reachedVia);
			current = current.reachedFrom;
		}
		
		return wb.toWord();
	}
	
	
	
	protected abstract State getState(Word<I> word);
	
	/**
	 * Returns (and possibly creates) the canonical state for the given signature.
	 * @param sig the signature
	 * @return the canonical state for the given signature
	 */
	protected State replaceOrRegister(StateSignature sig) {
		State state = register.get(sig);
		if(state != null)
			return state;
		
		register.put(sig, state = new State(sig));
		for(int i = 0; i < sig.successors.length; i++) {
			State succ = sig.successors[i];
			if(succ != null)
				succ.increaseIncoming();
		}
		return state;
	}
	
	/**
	 * Returns the canonical state for the given state's signature, or registers the
	 * state as canonical if no state with that signature exists.
	 * @param state the state
	 * @return the canonical state for the given state's signature
	 */
	protected State replaceOrRegister(State state) {
		StateSignature sig = state.getSignature();
		State other = register.get(sig);
		if(other != null) {
			if(state != other) {
				for(int i = 0; i < sig.successors.length; i++) {
					State succ = sig.successors[i];
					if(succ != null)
						succ.decreaseIncoming();
				}
			}
			return other;
		}
		
		register.put(sig, state);
		return state;
	}
	
	protected void updateInitSignature(Acceptance acc) {
		StateSignature sig = init.getSignature();
		sig.acceptance = acc;
	}
	

	/**
	 * Updates the signature for a given state.
	 * @param state the state
	 * @param acc the new acceptance value
	 * @return the canonical state for the updated signature
	 */
	protected State updateSignature(State state, Acceptance acc) {
		assert (state != init);
		StateSignature sig = state.getSignature();
		if(sig.acceptance == acc)
			return state;
		register.remove(sig);
		sig.acceptance = acc;
		sig.updateHashCode();
		return replaceOrRegister(state);
	}
	
	protected void updateInitSignature(int idx, State succ) {
		StateSignature sig = init.getSignature();
		State oldSucc = sig.successors[idx];
		if(oldSucc == succ)
			return;
		if(oldSucc != null)
			oldSucc.decreaseIncoming();
		sig.successors[idx] = succ;
		succ.increaseIncoming();
	}
	
	protected void updateInitSignature(Acceptance acc, int idx, State succ) {
		StateSignature sig = init.getSignature();
		State oldSucc = sig.successors[idx];
		Acceptance oldAcc = sig.acceptance;
		if(oldSucc == succ && oldAcc == acc)
			return;
		if(oldSucc != null)
			oldSucc.decreaseIncoming();
		sig.successors[idx] = succ;
		succ.increaseIncoming();
		sig.acceptance = acc;
	}
	
	/**
	 * Updates the signature for a given state.
	 * @param state the state
	 * @param idx the index of the transition to change
	 * @param succ the new successor for the above index
	 * @return the canonical state for the updated signature
	 */
	protected State updateSignature(State state, int idx, State succ) {
		assert (state != init);
		
		StateSignature sig = state.getSignature();
		if(sig.successors[idx] == succ)
			return state;
		register.remove(sig);
		if(sig.successors[idx] != null)
			sig.successors[idx].decreaseIncoming();
		
		sig.successors[idx] = succ;
		succ.increaseIncoming();
		sig.updateHashCode();
		return replaceOrRegister(state);
	}
	
	protected State updateSignature(State state, Acceptance acc, int idx, State succ) {
		assert (state != init);
		
		StateSignature sig = state.getSignature();
		if(sig.successors[idx] == succ && sig.acceptance == acc)
			return state;
		register.remove(sig);
		sig.successors[idx] = succ;
		sig.acceptance = acc;
		return replaceOrRegister(state);
	}
	
	/**
	 * Clones a state, changing the signature.
	 * @param other the state to clone
	 * @param acc the new acceptance value
	 * @return the canonical state for the derived signature
	 */
	protected State clone(State other, Acceptance acc) {
		assert (other != init);
		
		StateSignature sig = other.getSignature();
		if(sig.acceptance == acc)
			return other;
		sig = sig.clone();
		sig.acceptance = acc;
		sig.updateHashCode();
		return replaceOrRegister(sig);
	}
	
	protected State hiddenClone(State other) {
		assert (other != init);
		
		StateSignature sig = other.getSignature().clone();
		for(int i = 0; i < alphabetSize; i++) {
			State succ = sig.successors[i];
			if(succ != null) {
				succ.increaseIncoming();
			}
		}
		return new State(sig);
	}
	
	protected void hide(State state) {
		assert (state != init);
		
		StateSignature sig = state.getSignature();
		register.remove(sig);
	}
	
	protected State unhide(State state, Acceptance acc, int idx, State succ) {
		assert (state != init);
		
		StateSignature sig = state.getSignature();
		sig.acceptance = acc;
		State prevSucc = sig.successors[idx];
		if(prevSucc != null) {
			prevSucc.decreaseIncoming();
		}
		sig.successors[idx] = succ;
		if(succ != null) {
			succ.increaseIncoming();
		}
		sig.updateHashCode();
		
		return replaceOrRegister(state);
	}
	
	protected State unhide(State state, int idx, State succ) {
		assert (state != init);
		
		StateSignature sig = state.getSignature();
		State prevSucc = sig.successors[idx];
		if(prevSucc != null) {
			prevSucc.decreaseIncoming();
		}
		sig.successors[idx] = succ;
		if(succ != null) {
			succ.increaseIncoming();
		}
		sig.updateHashCode();
		
		return replaceOrRegister(state);
	}
	
	/**
	 * Clones a state, changing the signature.
	 * @param other the state to clone
	 * @param idx the index of the transition to change
	 * @param succ the new successor state
	 * @return the canonical state for the derived signature
	 */
	protected State clone(State other, int idx, State succ) {
		assert (other != init);
		
		StateSignature sig = other.getSignature();
		if(sig.successors[idx] == succ)
			return other;
		sig = sig.clone();
		sig.successors[idx] = succ;
		sig.updateHashCode();
		return replaceOrRegister(sig);
	}
	
	protected State clone(State other, Acceptance acc, int idx, State succ) {
		assert (other != init);
		
		StateSignature sig = other.getSignature();
		if(sig.successors[idx] == succ && sig.acceptance == acc)
			return other;
		sig = sig.clone();
		sig.successors[idx] = succ;
		sig.acceptance = acc;
		return replaceOrRegister(sig);
	}

}
