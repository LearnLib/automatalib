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

import java.util.Arrays;
import java.util.Objects;

final class StateSignature {
	public final State[] successors;
	public final Object[] outputs;
	private int hashCode;
	
	public StateSignature(int numSuccs) {
		this.successors = new State[numSuccs];
		this.outputs = new Object[numSuccs];
	}
	
	public StateSignature(StateSignature other) {
		this.successors = other.successors.clone();
		this.outputs = other.outputs.clone();
	}
	
	
	@Override
	public StateSignature clone() {
		return new StateSignature(this);
	}

	public void updateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(outputs);
		result = prime * result + Arrays.hashCode(successors);
		hashCode = result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != StateSignature.class)
			return false;
		StateSignature other = (StateSignature) obj;
		if(hashCode != other.hashCode)
			return false;
		for(int i = 0; i < successors.length; i++) {
			if(successors[i] != other.successors[i])
				return false;
		}
		for(int i = 0; i < outputs.length; i++) {
			if(!Objects.equals(outputs[i], other.outputs[i]))
				return false;
		}
		return true;
	}

	
	
	
}
