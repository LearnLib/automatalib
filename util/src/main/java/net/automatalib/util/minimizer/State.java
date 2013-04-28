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
/**
 * 
 */
package net.automatalib.util.minimizer;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.smartcollections.BasicLinkedListEntry;
import net.automatalib.commons.smartcollections.ElementReference;


/**
 * State record. Represents a state in the automaton model the minimizer
 * operates on, and also keeps various other information that is relevant
 * for the process.
 * 
 * @author Malte Isberner
 *
 * @param <S> original state class.
 * @param <L> original transition label class.
 */
final class State<S,L> extends BasicLinkedListEntry<State<S,L>,State<S,L>> {
	// The identifier of this state.
	private final int id;
	// The state in the original automaton.
	private final S originalState;
	// The list of incoming edges.
	private final List<Edge<S,L>> incoming
		= new ArrayList<Edge<S,L>>();
	// The list of outgoing edges.
	private final List<Edge<S,L>> outgoing
		= new ArrayList<Edge<S,L>>();
	
	// The block that contains this state.
	private Block<S,L> block;
	// A reference to this state in the block's collection.
	private ElementReference blockReference;
	
	// Signals whether or not this state is a split point, i.e.,
	// differs from the preceeding states in the final list.
	private boolean splitPoint;
	
	
	// The signature of the state, i.e., a sorted list of the (relevant)
	// outgoing edge labels.
	private List<TransitionLabel<S,L>> signature
		= new ArrayList<TransitionLabel<S,L>>();
	
	/**
	 * Constructor.
	 * @param id the state id.
	 * @param originalState the original state represented by this
	 * record.
	 */
	public State(int id, S originalState) {
		this.id = id;
		this.originalState = originalState;
	}
	
	/**
	 * Retrieves the state id.
	 * @return the state id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Retrieves the original state represented by
	 * this record.
	 * @return the original state object.
	 */
	public S getOriginalState() {
		return originalState;
	}
	
	/**
	 * Sets the block which contains this state.
	 * @param block the new block containing this state.
	 */
	public void setBlock(Block<S,L> block) {
		this.block = block;
	}
	
	/**
	 * Retrieves the block which contains this state.
	 * @return the block containing this state.
	 */
	public Block<S,L> getBlock() {
		return block;
	}

	/**
	 * Retrieves the list of incoming edges.
	 * @return the incoming edges.
	 */
	public List<Edge<S, L>> getIncoming() {
		return incoming;
	}

	/**
	 * Retrieves the list of outgoing edges.
	 * @return the outgoing edges.
	 */
	public List<Edge<S, L>> getOutgoing() {
		return outgoing;
	}
	
	/**
	 * Adds a new incoming edge.
	 * @param edge the incoming edge.
	 */
	public void addIncomingEdge(Edge<S,L> edge) {
		incoming.add(edge);
	}
	
	/**
	 * Adds a new outgoing edge.
	 * @param edge the outgoing edge.
	 */
	public void addOutgoingEdge(Edge<S,L> edge) {
		outgoing.add(edge);
	}


	/**
	 * Sets the split point property of this state.
	 * @param splitPoint whether or not this state is a split point.
	 */
	public void setSplitPoint(boolean splitPoint) {
		this.splitPoint = splitPoint;
	}
	
	/**
	 * Retrieves the split point property of this state.
	 * @return <code>true</code> iff this state is a split point,
	 * <code>false</code> otherwise.
	 */
	public boolean isSplitPoint() {
		return splitPoint;
	}
	
	
	/**
	 * Resets the information needed for a single split step associated
	 * with this state, i.e., the split point property and the signature.
	 */
	public void reset() {
		splitPoint = false;
		if(signature == null)
			signature = new ArrayList<TransitionLabel<S,L>>();
		else
			signature.clear();
	}
	
	/**
	 * Adds a transition label (letter) to this state's signature.
	 * @param letter the letter to add.
	 * @return <code>true</code> iff this was the first letter to be added
	 * to the signature, <code>false</code> otherwise.
	 */
	public boolean addToSignature(TransitionLabel<S,L> letter) {
		boolean first = signature.isEmpty();
		signature.add(letter);
		return first;
	}
	
	/**
	 * Retrieves the letter from the signature with the given index.
	 * If there is no such index (because the signature is shorter),
	 * <code>null</code> is returned.
	 * 
	 * @param index the signature index. 
	 * @return the respective letter of the signature, or <code>null</code>.
	 */
	public TransitionLabel<S,L> getSignatureLetter(int index) {
		if(index < signature.size())
			return signature.get(index);
		return null;
	}
	
	/**
	 * Sets the reference referencing this state in its block's collection.
	 * @param ref the reference.
	 */
	public void setBlockReference(ElementReference ref) {
		this.blockReference = ref;
	}
	
	/**
	 * Retrieves the block reference.
	 * @return the reference.
	 */
	public ElementReference getBlockReference() {
		return blockReference;
	}

	/**
	 * Retrieves the signature of this state.
	 * @return the signature.
	 */
	public List<TransitionLabel<S,L>> getSignature() {
		return signature;
	}
	
	/**
	 * Retrieves whether or not the block containing this state is a singleton,
	 * i.e., contains <i>only</i> this state.
	 * @return <code>true</code> if the containing block is a singleton,
	 * <code>false</code> otherwise.
	 */
	public boolean isSingletonBlock() {
		return (block.size() == 1);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return originalState.toString();
	}

	@Override
	public State<S, L> getElement() {
		return this;
	}
}