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
package net.automatalib.incremental.dfa;

/**
 * State data structure. Note that states are generally unique throughout the algorithm,
 * hence comparisons are always identity comparisons.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 * 
 */
final class State {
	private int numIncoming;
	private final StateSignature signature;
	
	/**
	 * Constructor. Initializes the state with a given signature
	 * @param signature the signature
	 */
	public State(StateSignature signature) {
		this.signature = signature;
		this.numIncoming = 0;
	}
	
	/**
	 * Increases the number of incoming transitions.
	 */
	public void increaseIncoming() {
		numIncoming++;
	}
	
	/**
	 * Decreases the number of incoming transitions.
	 */
	public void decreaseIncoming() {
		numIncoming--;
	}
	
	/**
	 * Checks whether this node is a confluence node (i.e. has more than one incoming
	 * transitions)
	 * @return <tt>true</tt> if this node is a confluence node, <tt>false</tt> otherwise.
	 */
	public boolean isConfluence() {
		return (numIncoming > 1);
	}
	
	/**
	 * Retrieves the ternary acceptance status of this node.
	 * @return the acceptance status of this node.
	 */
	public Acceptance getAcceptance() {
		return signature.acceptance;
	}
	
	/**
	 * Retrieves the successor for the given input index
	 * @param idx the input index
	 * @return the successor state for the given index
	 */
	public State getSuccessor(int idx) {
		return signature.successors[idx];
	}
	
	/**
	 * Retrieves the signature of this state
	 * @return the state's signature
	 */
	public StateSignature getSignature() {
		return signature;
	}
}
