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

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.nid.AbstractMutableNumericID;


public abstract class FastDetState<S extends FastDetState<S,T>, T> extends AbstractMutableNumericID {

	private final ResizingObjectArray transitions;
	
	public FastDetState(int initialNumOfInputs) {
		this.transitions = new ResizingObjectArray(initialNumOfInputs);
	}

	/**
	 * See {@link ResizingObjectArray#ensureCapacity(int)}
	 */
	public final boolean ensureInputCapacity(int capacity) {
		return this.transitions.ensureCapacity(capacity);
	}
	
	@SuppressWarnings("unchecked")
	public final T getTransition(int inputIdx) {
		return (T)transitions.array[inputIdx];
	}
	
	public final void setTransition(int inputIdx, T transition) {
		transitions.array[inputIdx] = transition;
	}
	
	public void clearTransitions() {
		for(int i = 0; i < transitions.array.length; i++)
			transitions.array[i] = null;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "s" + getId();
	}
}
