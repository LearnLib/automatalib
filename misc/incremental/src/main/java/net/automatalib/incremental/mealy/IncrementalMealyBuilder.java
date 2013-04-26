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
package net.automatalib.incremental.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import net.automatalib.automata.abstractimpl.AbstractDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.concepts.TransitionOutput;
import net.automatalib.automata.graphs.AbstractAutomatonGraph;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
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

/**
 * Incrementally builds an (acyclic) Mealy machine, from a set of input and corresponding
 * output words.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class IncrementalMealyBuilder<I, O> extends
	AbstractDeterministicAutomaton<State, I, TransitionRecord> implements
	TransitionOutput<TransitionRecord, O>,
	UniversalGraph<State, TransitionRecord, Void, Pair<I,O>>,
	DOTPlottableGraph<State, TransitionRecord>,
	IncrementalConstruction<MealyMachine<?,I,?,O>, I> {
	
	
	private final Map<StateSignature, State> register = new HashMap<>();

	private final Alphabet<I> inputAlphabet;
	private final int alphabetSize;
	private final State init;

	public IncrementalMealyBuilder(Alphabet<I> inputAlphabet) {
		this.inputAlphabet = inputAlphabet;
		this.alphabetSize = inputAlphabet.size();
		StateSignature initSig = new StateSignature(alphabetSize);
		this.init = new State(initSig);
		register.put(null, init);
	}

	@Override
	public int size() {
		return register.size();
	}

	private State getState(Word<I> word) {
		State s = init;

		for (I sym : word) {
			int idx = inputAlphabet.getSymbolIndex(sym);
			s = s.getSuccessor(idx);
			if (s == null)
				return null;
		}
		return s;
	}

	public boolean isComplete(Word<I> word) {
		State s = getState(word);
		return (s != null);
	}
	
	@SuppressWarnings("unchecked")
	public boolean lookup(Word<I> word, WordBuilder<O> output) {
		State curr = init;
		for(I sym : word) {
			int idx = inputAlphabet.getSymbolIndex(sym);
			State succ = curr.getSuccessor(idx);
			if(succ == null)
				return false;
			output.append((O)curr.getOutput(idx));
			curr = succ;
		}
		
		return true;
	}
	

	public void insert(Word<I> word, Word<O> outputWord) {
		int len = word.length();

		State curr = init;
		State conf = null;

		int confIndex = -1;

		int prefixLen = 0;
		for (I sym : word) {
			if (conf == null && curr.isConfluence()) {
				conf = curr;
				confIndex = prefixLen;
			}

			int idx = inputAlphabet.getSymbolIndex(sym);
			State succ = curr.getSuccessor(idx);
			if (succ == null)
				break;
			O outSym = outputWord.getSymbol(prefixLen);
			if(!Objects.equals(outSym, curr.getOutput(idx)))
				throw new ConflictException("Incompatible output symbols: " + outSym + " vs " + curr.getOutput(idx));
			curr = succ;
			prefixLen++;
		}

		if (prefixLen == len)
			return;

		Word<I> suffix = word.subWord(prefixLen);
		Word<O> suffixOut = outputWord.subWord(prefixLen);
		
	

		State last;

		State suffixState;
		State endpoint = null;
		if(conf != null)
			suffixState = createSuffix(suffix.subWord(1), suffixOut.subWord(1));
		else {
			Pair<State,State> suffixRes = createSuffix2(suffix.subWord(1), suffixOut.subWord(1));
			suffixState = suffixRes.getFirst();
			endpoint = suffixRes.getSecond();
		}
		I sym = suffix.getSymbol(0);
		int suffTransIdx = inputAlphabet.getSymbolIndex(sym);
		O suffTransOut = suffixOut.getSymbol(0);
		

		int currentIndex;
		if (conf != null) {
			last = clone(curr, suffTransIdx, suffixState, suffTransOut);

			for (int i = prefixLen - 1; i >= confIndex; i--) {
				State s = getState(word.prefix(i));
				sym = word.getSymbol(i);
				int idx = inputAlphabet.getSymbolIndex(sym);
				last = clone(s, idx, last);
			}

			currentIndex = confIndex;
		} else {
			if(endpoint == curr)
				last = clone(curr, suffTransIdx, suffixState, suffTransOut);
			else if(curr != init)
				last = updateSignature(curr, suffTransIdx, suffixState, suffTransOut);
			else {
				updateInitSignature(suffTransIdx, suffixState, suffTransOut);
				return;
			}
			currentIndex = prefixLen;
		}
		
		while (--currentIndex > 0) {
			State state = getState(word.prefix(currentIndex));
			sym = word.getSymbol(currentIndex);
			int idx = inputAlphabet.getSymbolIndex(sym);
			last = updateSignature(state, idx, last);

			if (state == last)
				return;
		}
		
		sym = word.getSymbol(0);
		int idx = inputAlphabet.getSymbolIndex(sym);
		updateInitSignature(idx, last);
		
	}

	
	private void updateInitSignature(int idx, State succ) {
		StateSignature sig = init.getSignature();
		State oldSucc = sig.successors[idx];
		if(oldSucc == succ)
			return;
		if(oldSucc != null)
			oldSucc.decreaseIncoming();
		sig.successors[idx] = succ;
		succ.increaseIncoming();
	}
	
	private State updateSignature(State state, int idx, State succ) {
		StateSignature sig = state.getSignature();
		if (sig.successors[idx] == succ)
			return state;
		
		register.remove(sig);
		if(sig.successors[idx] != null)
			sig.successors[idx].decreaseIncoming();
		sig.successors[idx] = succ;
		succ.increaseIncoming();
		sig.updateHashCode();
		return replaceOrRegister(state);
	}
	
	private void updateInitSignature(int idx, State succ, O out) {
		StateSignature sig = init.getSignature();
		State oldSucc = sig.successors[idx];
		if(oldSucc == succ && Objects.equals(out, sig.outputs[idx]))
			return;
		if(oldSucc != null)
			oldSucc.decreaseIncoming();
		sig.successors[idx] = succ;
		sig.outputs[idx] = out;
		succ.increaseIncoming();
	}
	
	private State updateSignature(State state, int idx, State succ, O out) {
		StateSignature sig = state.getSignature();
		if (sig.successors[idx] == succ && Objects.equals(out, sig.outputs[idx]))
			return state;
		
		register.remove(sig);
		if(sig.successors[idx] != null)
			sig.successors[idx].decreaseIncoming();
		sig.successors[idx] = succ;
		succ.increaseIncoming();
		sig.outputs[idx] = out;
		sig.updateHashCode();
		return replaceOrRegister(state);
	}


	private State clone(State other, int idx, State succ) {
		StateSignature sig = other.getSignature();
		if (sig.successors[idx] == succ)
			return other;
		sig = sig.clone();
		sig.successors[idx] = succ;
		sig.updateHashCode();
		return replaceOrRegister(sig);
	}
	
	private State clone(State other, int idx, State succ, O out) {
		StateSignature sig = other.getSignature();
		if (sig.successors[idx] == succ && Objects.equals(out, sig.outputs[idx]))
			return other;
		sig = sig.clone();
		sig.successors[idx] = succ;
		sig.outputs[idx] = out;
		sig.updateHashCode();
		return replaceOrRegister(sig);
	}

	private State replaceOrRegister(StateSignature sig) {
		State state = register.get(sig);
		if (state != null)
			return state;

		register.put(sig, state = new State(sig));
		for (int i = 0; i < sig.successors.length; i++) {
			State succ = sig.successors[i];
			if (succ != null)
				succ.increaseIncoming();
		}
		return state;
	}

	private State replaceOrRegister(State state) {
		StateSignature sig = state.getSignature();
		State other = register.get(sig);
		if (other != null) {
			if (state != other) {
				for (int i = 0; i < sig.successors.length; i++) {
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

	private State createSuffix(Word<I> suffix, Word<O> suffixOut) {
		StateSignature sig = new StateSignature(alphabetSize);
		sig.updateHashCode();
		State last = replaceOrRegister(sig);
		
		int len = suffix.length();
		for (int i = len - 1; i >= 0; i--) {
			sig = new StateSignature(alphabetSize);
			I sym = suffix.getSymbol(i);
			O outsym = suffixOut.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			sig.outputs[idx] = outsym;
			sig.updateHashCode();
			last = replaceOrRegister(sig);
		}

		return last;
	}
	
	private Pair<State,State> createSuffix2(Word<I> suffix, Word<O> suffixOut) {
		StateSignature sig = new StateSignature(alphabetSize);
		sig.updateHashCode();
		State last = replaceOrRegister(sig);
		State end = last;
		
		int len = suffix.length();
		for (int i = len - 1; i >= 0; i--) {
			sig = new StateSignature(alphabetSize);
			I sym = suffix.getSymbol(i);
			O outsym = suffixOut.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			sig.outputs[idx] = outsym;
			sig.updateHashCode();
			last = replaceOrRegister(sig);
		}

		return Pair.make(last, end);
	}
	

	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.TransitionSystem#getSuccessor(java.lang.Object)
	 */
	@Override
	public State getSuccessor(TransitionRecord transition) {
		return transition.source.getSuccessor(transition.transIdx);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleDTS#getInitialState()
	 */
	@Override
	public State getInitialState() {
		return init;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.UniversalGraph#getNodeProperties(java.lang.Object)
	 */
	@Override
	public Void getNodeProperties(State node) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.UniversalGraph#getEdgeProperties(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Pair<I, O> getEdgeProperties(TransitionRecord edge) {
		I input = inputAlphabet.getSymbol(edge.transIdx);
		O out = (O)edge.source.getOutput(edge.transIdx);
		return Pair.make(input, out);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getOutgoingEdges(java.lang.Object)
	 */
	@Override
	public Collection<TransitionRecord> getOutgoingEdges(State node) {
		List<TransitionRecord> edges = new ArrayList<TransitionRecord>();
		for(int i = 0; i < alphabetSize; i++) {
			if(node.getSuccessor(i) != null)
				edges.add(new TransitionRecord(node, i));
		}
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getTarget(java.lang.Object)
	 */
	@Override
	public State getTarget(TransitionRecord edge) {
		return edge.source.getSuccessor(edge.transIdx);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.concepts.TransitionOutput#getTransitionOutput(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public O getTransitionOutput(TransitionRecord transition) {
		return (O)transition.source.getOutput(transition.transIdx);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(java.lang.Object, java.lang.Object)
	 */
	@Override
	public TransitionRecord getTransition(State state, I input) {
		int idx = inputAlphabet.getSymbolIndex(input);
		if(state.getSuccessor(idx) != null)
			return new TransitionRecord(state, idx);
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DOTPlottableGraph#getHelper()
	 */
	@Override
	public GraphDOTHelper<State, TransitionRecord> getGraphDOTHelper() {
		return new DOTHelper(inputAlphabet, init);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.ts.SimpleFiniteTS#getStates()
	 */
	@Override
	public Collection<State> getStates() {
		return Collections.unmodifiableCollection(register.values());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.FiniteGraph#getNodes()
	 */
	@Override
	public Collection<State> getNodes() {
		return Collections.unmodifiableCollection(register.values());
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#getInputAlphabet()
	 */
	@Override
	public Alphabet<I> getInputAlphabet() {
		return inputAlphabet;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#findSeparatingWord(java.lang.Object, java.util.Collection, boolean)
	 */
	@Override
	public Word<I> findSeparatingWord(MealyMachine<?, I, ?, O> target,
			Collection<? extends I> inputs, boolean omitUndefined) {
		return doFindSeparatingWord(target, inputs, omitUndefined);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#toAutomaton()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CompactMealy<I, O> toAutomaton() {
		CompactMealy<I,O> result = new CompactMealy<I,O>(inputAlphabet, register.size());
		
		Map<State,Integer> stateMap = new HashMap<>();
		
		for(State s : register.values()) {
			Integer id;
			if(s == init)
				id = result.addInitialState();
			else
				id = result.addState();
			stateMap.put(s, id);
		}
		
		for(Map.Entry<State,Integer> e : stateMap.entrySet()) {
			State s = e.getKey();
			Integer id = e.getValue();
			
			for(int i = 0; i < alphabetSize; i++) {
				State succ = s.getSuccessor(i);
				if(succ == null)
					continue;
				I sym = inputAlphabet.getSymbol(i);
				O out = (O)s.getOutput(i);
				Integer succId = stateMap.get(succ);
				result.addTransition(id, sym, succId, out);
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
		return (s != null);
	}
	
	///////////////////////////////////////////////////////////////////////
	// Equivalence test                                                  //
	///////////////////////////////////////////////////////////////////////
	
	private static int getStateId(State state, Map<State,Integer> ids) {
		Integer id = ids.get(state);
		if(id == null) {
			id = ids.size();
			ids.put(state, id);
		}
		return id.intValue();
	}
	
	private static final class Record<S,I> {
		private final State state1;
		private final S state2;
		private final I reachedVia;
		private final Record<S,I> reachedFrom;
		private final int depth;
		
		public Record(State state1, S state2, Record<S,I> reachedFrom, I reachedVia) {
			this.state1 = state1;
			this.state2 = state2;
			this.reachedFrom = reachedFrom;
			this.reachedVia = reachedVia;
			this.depth = (reachedFrom != null) ? reachedFrom.depth + 1 : 0;
		}
		
		public Record(State state1, S state2) {
			this(state1, state2, null, null);
		}
	}
	
	private <S,T> Word<I> doFindSeparatingWord(MealyMachine<S,I,T,O> mealy, Collection<? extends I> inputs, boolean omitUndefined) {
		int thisStates = register.size();
		
		UnionFind uf = new UnionFind(thisStates + mealy.size());
		
		Map<State,Integer> ids = new HashMap<State,Integer>();
		
		State init1 = init;
		S init2 = mealy.getInitialState();
		
		if(init2 == null)
			return omitUndefined ? null : Word.<I>epsilon();
		
		StateIDs<S> mealyIds = mealy.stateIDs();
		
		int id1 = getStateId(init1, ids), id2 = mealyIds.getStateId(init2) + thisStates;
		
		uf.link(id1, id2);
		
		Queue<Record<S,I>> queue = new ArrayDeque<Record<S,I>>();
		
		queue.offer(new Record<S,I>(init1, init2));
		
		I lastSym = null;
		
		Record<S,I> current;
		
explore:while((current = queue.poll()) != null) {
			State state1 = current.state1;
			S state2 = current.state2;
			
			for(I sym : inputs) {
				int idx = inputAlphabet.getSymbolIndex(sym);
				State succ1 = state1.getSuccessor(idx);
				if(succ1 == null)
					continue;
				
				T trans2 = mealy.getTransition(state2, sym);
				if(trans2 == null) {
					if(omitUndefined)
						continue;
					lastSym = sym;
					break explore;
				}
				
				Object out1 = state1.getOutput(idx);
				Object out2 = mealy.getTransitionOutput(trans2);
				if(!Objects.equals(out1, out2)) {
					lastSym = sym;
					break explore;
				}
				
				S succ2 = mealy.getSuccessor(trans2);
				
				id1 = getStateId(succ1, ids);
				id2 = mealyIds.getStateId(succ2) + thisStates;
				
				int r1 = uf.find(id1), r2 = uf.find(id2);
				
				if(r1 == r2)
					continue;
				
				uf.link(r1, r2);
				
				queue.offer(new Record<>(succ1, succ2, current, sym));
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

	@Override
	public NodeIDs<State> nodeIDs() {
		return AbstractAutomatonGraph.nodeIDs(this);
	}

	@Override
	public <V> MutableMapping<State, V> createStaticNodeMapping() {
		return AbstractAutomatonGraph.createDynamicNodeMapping(this);
	}

	@Override
	public <V> MutableMapping<State, V> createDynamicNodeMapping() {
		return AbstractAutomatonGraph.createDynamicNodeMapping(this);
	}


}
