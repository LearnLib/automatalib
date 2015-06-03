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
package net.automatalib.ts.acceptors;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.ts.UniversalTransitionSystem;

/**
 * A transition system whose semantics are defined by whether a state is "accepting"
 * or not.
 * 
 * @author Malte Isberner
 *
 * @param <S> state class
 * @param <I> input symbol class
 */
@ParametersAreNonnullByDefault
public interface AcceptorTS<S, I> extends UniversalTransitionSystem<S, I, S, Boolean, Void> {
	
	/**
	 * Checks whether the given state is accepting.
	 * @param state the state
	 * @return <code>true</code> if the state is accepting, <code>false</code>
	 * otherwise.
	 */
	public boolean isAccepting(S state);
	
	public boolean isAccepting(Collection<? extends S> states);
	
	/**
	 * Determines whether the given input word is accepted by this acceptor.
	 * @param input the input word.
	 * @return <code>true</code> if the input word is accepted,
	 * <code>false</code> otherwise. 
	 */
	default public boolean accepts(Iterable<? extends I> input) {
		Collection<? extends S> states = getStates(input);
		
		return isAccepting(states);
	}
	
	@Override
	default public Boolean getStateProperty(S state) {
		return Boolean.valueOf(isAccepting(state));
	}
	
	@Override
	default public Void getTransitionProperty(S transition) {
		return null;
	}
	
	@Override
	default public S getSuccessor(S transition) {
		return transition;
	}
}
