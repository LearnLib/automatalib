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
import net.automatalib.commons.smartcollections.IntrusiveLinkedList;
import net.automatalib.commons.smartcollections.UnorderedCollection;



/**
 * A block in the partition calculated during minimization.
 * 
 * At the end of the minimization process, all states in the same block may
 * be identified.
 * 
 * @author Malte Isberner
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
public final class Block<S,L> extends BasicLinkedListEntry<Block<S,L>,Block<S,L>> {
	// The states contained in this block
	private final UnorderedCollection<State<S,L>> states;
	
	// The references for both the partition and splitter collection.
	private ElementReference partitionReference,
		splitterQueueReference;

	// The bucket of this block, used for initially arranging the
	// states ordered by their respective blocks during the weak sort.
	private IntrusiveLinkedList<State<S,L>> bucket
		= new IntrusiveLinkedList<State<S,L>>();
	
	// The sub blocks, i.e., the new blocks that result from
	// splitting this block.
	private List<UnorderedCollection<State<S,L>>>
		subBlocks = new ArrayList<UnorderedCollection<State<S,L>>>();
	
	// The total number of elements in all sub blocks, this is used
	// to detect whether an actual split has to be performed.
	private int elementsInSubBlocks;
	
	// The sub block currently being created.
	private UnorderedCollection<State<S,L>> currSubBlock;
	
	private final int id;
	
	/**
	 * Constructor.
	 */
	Block(int id) {
		this.id = id;
		this.states = new UnorderedCollection<State<S,L>>();
	}
	
	/**
	 * Constructor.
	 * @param states creates a block for the given collection of states.
	 * Ownership of this collection is assumed.
	 */
	Block(int id, UnorderedCollection<State<S,L>> states) {
		this.id = id;
		this.states = states;
		for(State<S,L> state : states)
			state.setBlock(this);
	}
	
	/**
	 * Adds a state to this block.
	 * @param state the state to add.
	 */
	void addState(State<S,L> state) {
		ElementReference ref = states.referencedAdd(state);
		state.setBlockReference(ref);
		state.setBlock(this);
	}
	
	/**
	 * Retrieves the collection of states in this block. 
	 * @return the states in this block.
	 */
	UnorderedCollection<State<S,L>> getStates() {
		return states;
	}
	
	/**
	 * Retrieves the bucket of this block.
	 * @return this blocks bucket.
	 */
	IntrusiveLinkedList<State<S,L>> getBucket() {
		return bucket;
	}
	
	/**
	 * Adds a state to this blocks bucket.
	 * @param state the state to add.
	 * @return <code>true</code> iff this was the first state to be added
	 * to the bucket, <code>false</code> otherwise.
	 */
	boolean addToBucket(State<S,L> state) {
		boolean first = bucket.isEmpty();
		bucket.pushBack(state);
		return first;
	}
	
	/**
	 * Initializes a new sub block.
	 */
	void createSubBlock() {
		currSubBlock = new UnorderedCollection<State<S,L>>();
		subBlocks.add(currSubBlock);
	}
	
	/**
	 * Adds a state to the current sub block.
	 * @param state the state to be added.
	 */
	void addToSubBlock(State<S,L> state) {
		currSubBlock.referencedAdd(state);
		elementsInSubBlocks++;
	}
	
	
	/**
	 * Retrieves the size of this block, i.e., the number of states it
	 * contains.
	 * @return the size of this block.
	 */
	public int size() {
		return states.size();
	}
	
	/**
	 * Retrieves the number of elements contained in sub blocks.
	 * @return the number of elements in sub blocks.
	 */
	int getElementsInSubBlocks() {
		return elementsInSubBlocks;
	}

	/**
	 * Retrieves the {@link ElementReference} that references this block
	 * in the partition collection, for efficient removal.
	 * @return the reference.
	 */
	ElementReference getPartitionReference() {
		return partitionReference;
	}

	/**
	 * Sets the partition reference.
	 * @param partitionReference the reference.
	 */
	void setPartitionReference(
			ElementReference partitionReference) {
		this.partitionReference = partitionReference;
	}

	/**
	 * Retrieves the {@link ElementReference} referencing this block in the
	 * splitter collection, for efficient removal. If this block is no
	 * potential splitter, <code>null</code> is returned.
	 * @return the reference or <code>null</code>.
	 */
	ElementReference getSplitterQueueReference() {
		return splitterQueueReference;
	}

	/**
	 * Sets the splitter queue reference.
	 * @param splitterQueueReference the reference
	 */
	void setSplitterQueueReference(
			ElementReference splitterQueueReference) {
		this.splitterQueueReference = splitterQueueReference;
	}
	
	/**
	 * Removes a state from this block.
	 * @param ref the reference for this state.
	 */
	void removeState(ElementReference ref) {
		states.remove(ref);
	}

	/**
	 * Retrieves the list of sub blocks, if any.
	 * @return the sub blocks.
	 */
	List<UnorderedCollection<State<S,L>>> getSubBlocks() {
		return subBlocks;
	}
	
	/**
	 * Resets all sub block information.
	 */
	void clearSubBlocks() {
		subBlocks.clear();
		elementsInSubBlocks = 0;
	}

	/**
	 * Checks whether or not this block is empty, i.e., contains no states.
	 * @return <code>true</code> iff the block is empty, <code>false</code>
	 * otherwise.
	 */
	public boolean isEmpty() {
		return states.isEmpty();
	}
	
	/**
	 * Checks whether or not this block is a singleton, i.e., contains only
	 * a single state.
	 * @return <code>true</code> iff this block is a singleton,
	 * <code>false</code> otherwise.
	 */
	public boolean isSingleton() {
		return (states.size() == 1);
	}
	
	
	/**
	 * Retrieves the ID of this block.
	 * @return the id of this block.
	 */
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see de.ls5.smartcollections.LinkedListEntry#getElement()
	 */
	@Override
	public Block<S, L> getElement() {
		return this;
	}
}
