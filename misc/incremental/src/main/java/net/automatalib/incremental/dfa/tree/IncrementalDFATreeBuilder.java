/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.dfa.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.graphs.dot.DelegateDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import com.google.common.collect.Iterators;

public class IncrementalDFATreeBuilder<I> extends AbstractIncrementalDFABuilder<I> {
	
	@Nonnull
	protected final Node<I> root;
	
	@ParametersAreNonnullByDefault
	public class GraphView extends AbstractGraphView<I,Node<I>,Edge<I>> {
		@Override
		public Collection<Node<I>> getNodes() {
			List<Node<I>> result = new ArrayList<>();
			Iterators.addAll(result, GraphTraversal.dfIterator(this, Collections.singleton(root)));
			return result;
		}
		
		@Override
		public Collection<Edge<I>> getOutgoingEdges(Node<I> node) {
			List<Edge<I>> result = new ArrayList<>();
			for(int i = 0; i < alphabetSize; i++) {
				Node<I> succ = node.getChild(i);
				if(succ != null) {
					result.add(new Edge<>(succ, inputAlphabet.getSymbol(i)));
				}
			}
			return result;
		}
		
		@Override
		@Nonnull
		public Node<I> getTarget(Edge<I> edge) {
			return edge.getNode();
		}
		@Override
		@Nullable
		public I getInputSymbol(Edge<I> edge) {
			return edge.getInput();
		}
		@Override
		@Nonnull
		public Acceptance getAcceptance(Node<I> node) {
			return node.getAcceptance();
		}
		@Override
		@Nonnull
		public GraphDOTHelper<Node<I>, Edge<I>> getGraphDOTHelper() {
			return new DelegateDOTHelper<Node<I>,Edge<I>>(super.getGraphDOTHelper()) {
				private int id = 0;
				@Override
				public boolean getNodeProperties(Node<I> node,
						Map<String, String> properties) {
					if(!super.getNodeProperties(node, properties)) {
						return false;
					}
					properties.put(NodeAttrs.LABEL, "n" + (id++));
					return true;
				}
			};
		}
		@Override
		@Nonnull
		public Node<I> getInitialNode() {
			return root;
		}
	}
	
	@ParametersAreNonnullByDefault
	public class TransitionSystemView extends AbstractTransitionSystemView<Node<I>, I, Node<I>> {
		@Override
		@Nonnull
		public Node<I> getSuccessor(Node<I> transition) {
			return transition;
		}
		@Override
		@Nullable
		public Node<I> getTransition(Node<I> state, I input) {
			int inputIdx = inputAlphabet.getSymbolIndex(input);
			return state.getChild(inputIdx);
		}
		@Nonnull
		@Override
		public Node<I> getInitialState() {
			return root;
		}
		@Override
		@Nonnull
		public Acceptance getAcceptance(Node<I> state) {
			return state.getAcceptance();
		}
	}
	
	public IncrementalDFATreeBuilder(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
		this.root = new Node<I>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.IncrementalConstruction#findSeparatingWord(java.lang.Object, java.util.Collection, boolean)
	 */
	@Override
	@Nullable
	public Word<I> findSeparatingWord(DFA<?, I> target,
			Collection<? extends I> inputs, boolean omitUndefined) {
		return doFindSeparatingWord(target, inputs, omitUndefined);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.IncrementalDFABuilder#asGraph()
	 */
	@Override
	public GraphView asGraph() {
		return new GraphView();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.IncrementalDFABuilder#asTransitionSystem()
	 */
	@Override
	public TransitionSystemView asTransitionSystem() {
		return new TransitionSystemView();
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.IncrementalDFABuilder#lookup(net.automatalib.words.Word)
	 */
	@Override
	public Acceptance lookup(Word<? extends I> inputWord) {
		Node<I> curr = root;
		
		for(I sym : inputWord) {
			int symIdx = inputAlphabet.getSymbolIndex(sym);
			Node<I> succ = curr.getChild(symIdx);
			if(succ == null) {
				return Acceptance.DONT_KNOW;
			}
			curr = succ;
		}
		return curr.getAcceptance();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.IncrementalDFABuilder#insert(net.automatalib.words.Word, boolean)
	 */
	@Override
	public void insert(Word<? extends I> word, boolean acceptance) {
		Node<I> curr = root;
		
		for(I sym : word) {
			int inputIdx = inputAlphabet.getSymbolIndex(sym);
			Node<I> succ = curr.getChild(inputIdx);
			if(succ == null) {
				succ = new Node<I>();
				curr.setChild(inputIdx, alphabetSize, succ);
			}
			curr = succ;
		}
		
		Acceptance acc = curr.getAcceptance();
		Acceptance newWordAcc = Acceptance.fromBoolean(acceptance);
		if(acc == Acceptance.DONT_KNOW) {
			curr.setAcceptance(newWordAcc);
		}
		else if(acc != newWordAcc) {
			throw new ConflictException("Conflicting acceptance values for word " + word + ": " + acc + " vs " + newWordAcc);
		}
	}
	
	protected static final class Record<S,I> {
		public final S automatonState;
		public final Node<I> treeNode;
		public final I incomingInput;
		public final Iterator<? extends I> inputIt;
		
		public Record(S automatonState, Node<I> treeNode, I incomingInput, Iterator<? extends I> inputIt) {
			this.automatonState = automatonState;
			this.treeNode = treeNode;
			this.incomingInput = incomingInput;
			this.inputIt = inputIt;
		}
	}
	
	protected <S> Word<I> doFindSeparatingWord(final DFA<S,I> target, Collection<? extends I> inputs, boolean omitUndefined) {
		Deque<Record<S,I>> dfsStack = new ArrayDeque<>();
		
		S automatonInit = target.getInitialState();
		if(root.getAcceptance().conflicts(target.isAccepting(automatonInit))) {
			return Word.epsilon();
		}
		
		dfsStack.push(new Record<>(automatonInit, root, null, inputs.iterator()));
		
		while(!dfsStack.isEmpty()) {
			Record<S,I> rec = dfsStack.peek();
			if(!rec.inputIt.hasNext()) {
				dfsStack.pop();
				continue;
			}
			I input = rec.inputIt.next();
			int inputIdx = inputAlphabet.getSymbolIndex(input);
			
			Node<I> succ = rec.treeNode.getChild(inputIdx);
			if(succ == null) {
				continue;
			}
			
			S automatonSucc = (rec.automatonState == null) ? null : target.getTransition(rec.automatonState, input);
			if(automatonSucc == null && omitUndefined) {
				continue;
			}
			
			boolean succAcc = (automatonSucc == null) ? false : target.isAccepting(automatonSucc);
			
			if(succ.getAcceptance().conflicts(succAcc)) {
				WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
				wb.append(input);
				
				dfsStack.pop();
				while(!dfsStack.isEmpty()) {
					wb.append(rec.incomingInput);
					rec = dfsStack.pop();
				}
				return wb.reverse().toWord();
			}
						
			dfsStack.push(new Record<>(automatonSucc, succ, input, inputs.iterator()));
		}
		
		return null;
	}

}
