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
package net.automatalib.automata.base.fast;

import java.util.Collection;
import java.util.HashSet;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;


public abstract class FastNondetState<S extends FastNondetState<S, T>, T>
		extends AbstractMutableNumericID {
	private final Collection<T>[] transitions;
	
	
	@SuppressWarnings("unchecked")
	public FastNondetState(int numInputs) {
		this.transitions = new Collection[numInputs];
	}
	
	
	public final Collection<T> getTransitions(int inputIdx) {
		return transitions[inputIdx];
	}
	
	public final void setTransitions(int inputIdx, Collection<? extends T> transitions) {
		this.transitions[inputIdx] = new HashSet<T>(transitions);
	}
	
	public void clearTransitions() {
		for(int i = 0; i < transitions.length; i++)
			transitions[i].clear();
	}

}
