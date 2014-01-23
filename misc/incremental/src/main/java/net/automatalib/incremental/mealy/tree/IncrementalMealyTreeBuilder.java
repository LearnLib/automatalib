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
package net.automatalib.incremental.mealy.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.AbstractIncrementalMealyBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import com.google.common.base.Objects;

public class IncrementalMealyTreeBuilder<I, O> extends AbstractIncrementalMealyBuilder<I,O> {
	
	private static final class Record<S,I,O> {
		private final S automatonState;
		private final Node<I,O> treeNode;
		private final I incomingInput;
		private final Iterator<? extends I> inputIt;
		
		public Record(S automatonState, Node<I,O> treeNode, I incomingInput, Iterator<? extends I> inputIt) {
			this.automatonState = automatonState;
			this.treeNode = treeNode;
			this.inputIt = inputIt;
			this.incomingInput = incomingInput;
		}
	}
	
	private static final class BuildAutomatonRecord<I,O> {
		private final int automatonState;
		private final Node<I,O> treeNode;
		private int inputIdx;
		
		public BuildAutomatonRecord(int automatonState, Node<I,O> treeNode) {
			this.automatonState = automatonState;
			this.treeNode = treeNode;
			this.inputIdx = 0;
		}
	}
	
	private final Node<I,O> root;
	private int size;
	
	public IncrementalMealyTreeBuilder(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
		this.root = new Node<>(inputAlphabet.size());
		this.size = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.mealy.IncrementalMealyBuilder#insert(net.automatalib.words.Word, net.automatalib.words.Word)
	 */
	@Override
	public void insert(Word<I> input, Word<O> outputWord) throws ConflictException {
		Node<I,O> curr = root;
		
		Iterator<? extends O> outputIt = outputWord.iterator();
		for(I sym : input) {
			int symIdx = inputAlphabet.getSymbolIndex(sym);
			O out = outputIt.next();
			Edge<I,O> edge = curr.getEdge(symIdx);
			if(edge == null) {
				curr = insertNode(curr, symIdx, out);
			}
			else {
				if(!Objects.equal(out, edge.getOutput())) {
					throw new ConflictException();
				}
				curr = curr.getSuccessor(symIdx);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.mealy.IncrementalMealyBuilder#lookup(net.automatalib.words.Word, java.util.List)
	 */
	@Override
	public boolean lookup(Word<I> word, List<? super O> output) {
		Node<I,O> curr = root;
		
		for(I sym : word) {
			int symIdx = inputAlphabet.getSymbolIndex(sym);
			Edge<I,O> edge = curr.getEdge(symIdx);
			if(edge == null) {
				return false;
			}
			output.add(edge.getOutput());
			curr = edge.getTarget();
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.mealy.IncrementalMealyBuilder#size()
	 */
	@Override
	public int size() {
		return size;
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
	public MealyMachine<?, I, ?, O> toAutomaton() {
		CompactMealy<I, O> result = new CompactMealy<>(inputAlphabet);
		
		int init = result.addInitialState();
		
		Deque<BuildAutomatonRecord<I,O>> dfsStack = new ArrayDeque<>();
		
		dfsStack.push(new BuildAutomatonRecord<>(init, root));
		
		int alphabetSize = inputAlphabet.size();
		
		while(!dfsStack.isEmpty()) {
			BuildAutomatonRecord<I,O> rec = dfsStack.peek();
			if(rec.inputIdx >= alphabetSize) {
				dfsStack.pop();
				continue;
			}
			
			int inputIdx = rec.inputIdx++;
			
			Edge<I,O> edge = rec.treeNode.getEdge(inputIdx);
			if(edge == null) {
				continue;
			}
			
			I inputSym = inputAlphabet.getSymbol(inputIdx);
			
			int succ = result.addState();
			result.addTransition(rec.automatonState, inputSym, succ, edge.getOutput());
			
			dfsStack.push(new BuildAutomatonRecord<>(succ, edge.getTarget()));
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.mealy.AbstractIncrementalMealyBuilder#hasDefinitiveInformation(net.automatalib.words.Word)
	 */
	@Override
	public boolean hasDefinitiveInformation(Word<I> word) {
		Node<I,O> curr = root;
		
		Iterator<I> symIt = word.iterator();
		while(symIt.hasNext() && curr != null) {
			int symIdx = inputAlphabet.getSymbolIndex(symIt.next());
			curr = curr.getSuccessor(symIdx);
		}
		return (curr != null);
	}
	
	
	private <S,T> Word<I> doFindSeparatingWord(MealyMachine<S,I,T,O> target, Collection<? extends I> inputs, boolean omitUndefined) {
		Deque<Record<S,I,O>> dfsStack = new ArrayDeque<>();
		
		dfsStack.push(new Record<>(target.getInitialState(), root, null, inputs.iterator()));
		
		while(!dfsStack.isEmpty()) {
			Record<S,I,O> rec = dfsStack.peek();
			if(!rec.inputIt.hasNext()) {
				dfsStack.pop();
				continue;
			}
			I input = rec.inputIt.next();
			int inputIdx = inputAlphabet.getSymbolIndex(input);
			
			Edge<I,O> edge = rec.treeNode.getEdge(inputIdx);
			if(edge == null) {
				continue;
			}
			
			T trans = target.getTransition(rec.automatonState, input);
			if(omitUndefined && trans == null) {
				continue;
			}
			if(trans == null || !Objects.equal(target.getTransitionOutput(trans), edge.getOutput())) {
				
				WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
				wb.append(input);
				
				dfsStack.pop();
				do {
					wb.append(rec.incomingInput);
					rec = dfsStack.pop();
				} while(!dfsStack.isEmpty());
				return wb.reverse().toWord();
			}
			
			dfsStack.push(new Record<>(target.getSuccessor(trans), edge.getTarget(), input, inputs.iterator()));
		}
		
		return null;
	}
	
	private Node<I,O> insertNode(Node<I,O> parent, int symIdx, O output) {
		Node<I,O> succ = new Node<>(inputAlphabet.size());
		Edge<I,O> edge = new Edge<I,O>(output, succ);
		parent.setEdge(symIdx, edge);
		return succ;
	}

}
