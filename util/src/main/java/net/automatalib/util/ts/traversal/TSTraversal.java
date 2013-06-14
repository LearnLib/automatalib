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

import net.automatalib.commons.util.Holder;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.traversal.TraversalOrder;


/**
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public abstract class TSTraversal {

	public static final int NO_LIMIT = -1;
	
	
	
	public static <S,I,T,D>
	boolean depthFirst(TransitionSystem<S, I, T> ts,
			int limit,
			Collection<? extends I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		Deque<DFRecord<S,I,T,D>> dfsStack = new ArrayDeque<DFRecord<S,I,T,D>>();
		
		Holder<D> dataHolder = new Holder<>();
		
		// setting the following to false means that the traversal had to be aborted
		// due to reaching the limit
		boolean complete = true;
		int stateCount = 0;
		
		for(S initS : ts.getInitialStates()) {
			dataHolder.value = null;
			TSTraversalAction act = vis.processInitial(initS, dataHolder);
			switch(act) {
			case ABORT_INPUT:
			case ABORT_STATE:
			case IGNORE:
				continue;
			case ABORT_TRAVERSAL:
				return complete;
			case EXPLORE:
				if(stateCount != limit) {
					dfsStack.push(new DFRecord<S, I, T, D>(initS, inputs, dataHolder.value));
					stateCount++;
				}
				else
					complete = false;
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
			
			S succ = ts.getSuccessor(trans);
			dataHolder.value = null;
			TSTraversalAction act = vis.processTransition(source, data, input, trans, succ, dataHolder);
			
			switch(act) {
			case ABORT_INPUT:
				current.advanceInput(ts);
				break;
			case ABORT_STATE:
				dfsStack.pop();
				break;
			case ABORT_TRAVERSAL:
				return complete;
			case IGNORE:
				current.advance(ts);
				break;
			case EXPLORE:
				if(stateCount != limit) {
					dfsStack.push(new DFRecord<S,I,T,D>(succ, inputs, dataHolder.value));
					stateCount++;
				}
				else
					complete = false;
				break;
			}
		}
		
		return complete;
	}
	
	public static <S,I,T,D>
	boolean depthFirst(TransitionSystem<S, I, T> ts,
			Collection<? extends I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		return depthFirst(ts, NO_LIMIT, inputs, vis);
	}
	
	
	/**
	 * Traverses the given transition system in a breadth-first fashion.
	 * The traversal is steered by the specified visitor.
	 * 
	 * @param ts the transition system.
	 * @param inputs the input alphabet.
	 * @param vis the visitor.
	 */
	public static <S,I,T,D>
	boolean breadthFirst(TransitionSystem<S, I, T> ts,
			int limit,
			Collection<? extends I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		Deque<BFSRecord<S,D>> bfsQueue = new ArrayDeque<BFSRecord<S,D>>();

		// setting the following to false means that the traversal had to be aborted
		// due to reaching the limit
		boolean complete = true;
		int stateCount = 0;
		
		Holder<D> dataHolder = new Holder<>();
		
		for(S initS : ts.getInitialStates()) {
			dataHolder.value = null;
			TSTraversalAction act = vis.processInitial(initS, dataHolder);
			switch(act) {
			case ABORT_TRAVERSAL:
				return complete;
			case EXPLORE:
				if(stateCount != limit) {
					bfsQueue.offer(new BFSRecord<S,D>(initS, dataHolder.value));
					stateCount++;
				}
				else
					complete = false;
				break;
			case ABORT_INPUT:
			case ABORT_STATE:
			case IGNORE:
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
					S succ = ts.getSuccessor(trans);
					
					dataHolder.value = null;
					TSTraversalAction act = vis.processTransition(state, data, input, trans, succ, dataHolder);
					
					switch(act) {
					case ABORT_INPUT:
						continue inputs_loop;
					case ABORT_STATE:
						break inputs_loop;
					case ABORT_TRAVERSAL:
						return complete;
					case EXPLORE:
						if(stateCount != limit) {
							bfsQueue.offer(new BFSRecord<S,D>(succ, dataHolder.value));
							stateCount++;
						}
						else
							complete = false;
						break;
					case IGNORE:
					}
				}
			}
		}
		
		return complete;
	}
	
	public static <S,I,T,D>
	boolean breadthFirst(TransitionSystem<S, I, T> ts,
			Collection<? extends I> inputs,
			TSTraversalVisitor<S, I, T, D> vis) {
		return breadthFirst(ts, NO_LIMIT, inputs, vis);
	}

	
	public static <S,I,T,D>
	boolean traverse(TraversalOrder order, TransitionSystem<S,I,T> ts, int limit, Collection<? extends I> inputs, TSTraversalVisitor<S, I, T, D> vis) {
		switch(order) {
		case BREADTH_FIRST:
			return breadthFirst(ts, limit, inputs, vis);
		case DEPTH_FIRST:
			return depthFirst(ts, limit, inputs, vis);
		default:
			throw new IllegalArgumentException("Unknown traversal order: " + order);
		}
	}
	
	public static <S,I,T,D>
	boolean traverse(TraversalOrder order, TransitionSystem<S,I,T> ts, Collection<? extends I> inputs, TSTraversalVisitor<S, I, T, D> vis) {
		return traverse(order, ts, NO_LIMIT, inputs, vis);
	}
	
}
