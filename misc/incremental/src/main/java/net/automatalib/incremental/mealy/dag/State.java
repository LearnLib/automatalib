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
package net.automatalib.incremental.mealy.dag;


public final class State {
	
	private int numIncoming;
	private final StateSignature signature;
	
	public State(StateSignature signature) {
		this.signature = signature;
	}
	
	public void increaseIncoming() {
		numIncoming++;
	}
	
	public void decreaseIncoming() {
		numIncoming--;
	}
	
	public int getNumIncoming() {
		return numIncoming;
	}
	
	public boolean isConfluence() {
		return (numIncoming > 1);
	}
	
	public State getSuccessor(int idx) {
		return signature.successors[idx];
	}
	
	public Object getOutput(int idx) {
		return signature.outputs[idx];
	}
	
	public StateSignature getSignature() {
		return signature;
	}
}
