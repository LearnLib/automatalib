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
package net.automatalib.incremental.dfa.dag;

import java.util.Arrays;

import net.automatalib.incremental.dfa.Acceptance;

/**
 * Signature of a state. A signature consists of the list of all successor states
 * for all alphabet symbols, and the acceptance status.
 * 
 * @author Malte Isberner 
 *
 */
final class StateSignature {

	public final State[] successors;
	public Acceptance acceptance;
	private int hashCode;
	
	public StateSignature(int numSuccs, Acceptance acceptance) {
		this.successors = new State[numSuccs];
		this.acceptance = acceptance;
		updateHashCode();
	}
	
	public StateSignature(StateSignature other) {
		this.successors = other.successors.clone();
		this.acceptance = other.acceptance;
	}
	
	public StateSignature duplicate() {
		return new StateSignature(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	public void updateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ acceptance.hashCode();
		result = prime * result + Arrays.hashCode(successors);
		hashCode = result;
	}

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
		if (acceptance != other.acceptance)
			return false;
		for(int i = 0; i < successors.length; i++) {
			if(successors[i] != other.successors[i])
				return false;
		}
		return true;
	}
	
	
	

}
