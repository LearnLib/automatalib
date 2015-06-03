/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.ts.simple;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.ts.TransitionSystem;

/**
 * A simple deterministic transition system. In a deterministic transition system,
 * there exists in each state at most one successor state for each input symbol.
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class
 * @param <I> input symbol class
 */
@ParametersAreNonnullByDefault
public interface SimpleDTS<S, I> extends SimpleTS<S, I> {
	
	public static <S> Set<S> stateToSet(S state) {
		if (state == null) {
			return Collections.emptySet();
		}
		return Collections.singleton(state);
	}
	
	
	
	@Override
	default public Set<? extends S> getInitialStates() {
		return stateToSet(getInitialState());
	}
	
	@Override
	@Nonnull
	default public Set<? extends S> getSuccessors(S state, @Nullable I input) {
		return stateToSet(getSuccessor(state, input));
	}
	
	@Override
	@Nonnull
	default public Set<? extends S> getSuccessors(S state, Iterable<? extends I> input) {
		return stateToSet(getSuccessor(state, input));
	}
	
	@Override
	@Nonnull
	default public Set<? extends S> getStates(Iterable<? extends I> input) {
		return stateToSet(getState(input));
	}
	
	/**
	 * Retrieves the initial state of this transition system.
	 * @return the initial state.
	 * @see TransitionSystem#getInitialStates()
	 */
	@Nullable
	public S getInitialState();
	
	/**
	 * Retrieves the successor state reachable by the given input symbol. 
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the successor state reachable by the given input symbol,
	 * or <code>null</code> if no state is reachable by this symbol.
	 * @see TransitionSystem#getSuccessors(Object, Object)
	 */
	@Nullable
	public S getSuccessor(S state, @Nullable I input);
	
	/**
	 * Retrieves the successor state reachable by the given sequence of
	 * input symbols.
	 * @param state the source state.
	 * @param input the input symbol.
	 * @return the successor state reachable by the given sequence of input
	 * symbols, or <code>null</code> if no state is reachable by this symbol.
	 * @see TransitionSystem#getSuccessors(Object, Iterable)
	 */
	@Nullable
	default public S getSuccessor(S state, Iterable<? extends I> input) {
		S curr = state;
		Iterator<? extends I> it = input.iterator();
		
		while(curr != null && it.hasNext()) {
			I sym = it.next();
			curr = getSuccessor(curr, sym);
		}
		
		return curr;
	}
	
	/**
	 * Retrieves the state reachable by the given sequence of input symbols
	 * from the initial state.
	 * @param input the input word.
	 * @return the state reachable by the given input word, or <code>null</code>
	 * if no state is reachable by this word.
	 * @see TransitionSystem#getStates(Iterable)
	 */
	@Nullable
	default public S getState(Iterable<? extends I> input) {
		return getSuccessor(getInitialState(), input);
	}
}
