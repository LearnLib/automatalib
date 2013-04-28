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
package net.automatalib.util.ts.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.traversal.TraversalAction.Type;


/**
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class TSTraversal {

	private static final TraversalAction<?> IGNORE = new TraversalAction<Object>(Type.IGNORE);

	private static final TraversalAction<?> ABORT_INPUT
		= new TraversalAction<Object>(Type.ABORT_INPUT);

	private static final TraversalAction<?> ABORT_STATE
		= new TraversalAction<Object>(Type.ABORT_STATE);

	private static final TraversalAction<?> ABORT_TRAVERSAL
		= new TraversalAction<Object>(Type.ABORT_TRAVERSAL);

	public static <D> TraversalAction<D> explore(D data) {
		return new TraversalAction<D>(Type.EXPLORE, data);
	}

	@SuppressWarnings("unchecked")
	public static <D> TraversalAction<D> ignore() {
		return (TraversalAction<D>) IGNORE;
	}

	@SuppressWarnings("unchecked")
	public static <D> TraversalAction<D> abortInput() {
		return (TraversalAction<D>) ABORT_INPUT;
	}

	@SuppressWarnings("unchecked")
	public static <D> TraversalAction<D> abortState() {
		return (TraversalAction<D>) ABORT_STATE;
	}

	@SuppressWarnings("unchecked")
	public static <D> TraversalAction<D> abortTraversal() {
		return (TraversalAction<D>) ABORT_TRAVERSAL;
	}
	
	public static <D> TraversalAction<D> explore() {
		return explore(null);
	}
	
	
	public static <S,I,T,D> void depthFirst(TransitionSystem<S, I, T> ts,
			Collection<I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		Deque<DFRecord<S,I,T,D>> dfsStack = new ArrayDeque<DFRecord<S,I,T,D>>();
		
		for(S initS : ts.getInitialStates()) {
			TraversalAction<D> act = vis.processInitial(initS);
			switch(act.type) {
			case ABORT_INPUT:
			case ABORT_STATE:
			case IGNORE:
				continue;
			case ABORT_TRAVERSAL:
				return;
			case EXPLORE:
				D data = act.data;
				dfsStack.push(new DFRecord<S, I, T, D>(initS, inputs, data));
				break;
			}
		}
		
		while(!dfsStack.isEmpty()) {
			DFRecord<S,I,T,D> current = dfsStack.peek();
			
			S source = current.state;
			D data = current.data;
			
			if(current.start(ts)) {
				if(!vis.startExploration(source, data)) {
					dfsStack.pop();
					continue;
				}
			}
			
			if(!current.hasNextTransition()) {
				dfsStack.pop();
				continue;
			}
		
			I input = current.input();
			T trans = current.transition();
			
			TraversalAction<D> act = vis.processTransition(source, data, input, trans);
			
			switch(act.type) {
			case ABORT_INPUT:
				current.advanceInput(ts);
				continue;
			case ABORT_STATE:
				dfsStack.pop();
				continue;
			case ABORT_TRAVERSAL:
				return;
			case IGNORE:
				current.advance(ts);
				continue;
			case EXPLORE:
			}
			
			S succ = ts.getSuccessor(trans);
			D succData = act.data;
			
			dfsStack.push(new DFRecord<S,I,T,D>(succ, inputs, succData));
		}
	}
	
	
	/**
	 * Traverses the given transition system in a breadth-first fashion.
	 * The traversal is steered by the specified visitor.
	 * 
	 * @param ts the transition system.
	 * @param inputs the input alphabet.
	 * @param vis the visitor.
	 */
	public static <S,I,T,D> void breadthFirst(TransitionSystem<S, I, T> ts,
			Collection<? extends I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		Deque<BFSRecord<S,D>> bfsQueue = new ArrayDeque<BFSRecord<S,D>>();
		
		
		for(S initS : ts.getInitialStates()) {
			TraversalAction<D> act = vis.processInitial(initS);
			switch(act.type) {
			case ABORT_INPUT:
			case ABORT_STATE:
			case IGNORE:
				continue;
			case ABORT_TRAVERSAL:
				return;
			case EXPLORE:
				D data = act.data;
				bfsQueue.offer(new BFSRecord<S,D>(initS, data));
				break;
			}
		}
		
		while(!bfsQueue.isEmpty()) {
			BFSRecord<S,D> current = bfsQueue.poll();
			
			S state = current.state;
			D data = current.data;
			
			if(!vis.startExploration(state, data))
				continue;
			
inputs_loop:
			for(I input : inputs) {
				Collection<T> transitions = ts.getTransitions(state, input);
				
				if(transitions == null)
					continue;
				
				for(T trans : transitions) {
					TraversalAction<D> act = vis.processTransition(state, data, input, trans);
					
					switch(act.type) {
					case ABORT_INPUT:
						continue inputs_loop;
					case ABORT_STATE:
						break inputs_loop;
					case ABORT_TRAVERSAL:
						return;
					case IGNORE:
						continue;
					case EXPLORE:
					}
					

					S succ = ts.getSuccessor(trans);
					D succData = act.data;
					
					bfsQueue.offer(new BFSRecord<S,D>(succ, succData));
				}
			}
		}
		
	}

	
}
