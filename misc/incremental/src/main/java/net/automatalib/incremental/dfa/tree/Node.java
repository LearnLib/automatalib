/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.dfa.tree;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.incremental.dfa.Acceptance;

/**
 * A node in the tree internally used by {@link IncrementalDFATreeBuilder}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
@ParametersAreNonnullByDefault
public final class Node<I> {
	private Acceptance acceptance;
	private Node<I>[] children;
	
	/**
	 * Constructor. Constructs a new node with no children and an acceptance value
	 * of {@link Acceptance#DONT_KNOW}
	 */
	public Node() {
		this(Acceptance.DONT_KNOW);
	}
	
	/**
	 * Constructor. Constructs a new node with no children and the specified acceptance
	 * value.
	 * @param acceptance the acceptance value for the node
	 */
	public Node(Acceptance acceptance) {
		this.acceptance = acceptance;
	}
	
	/**
	 * Retrieves the acceptance value of this node.
	 * @return the acceptance value of this node
	 */
	public Acceptance getAcceptance() {
		return acceptance;
	}
	
	/**
	 * Sets the acceptance value for this node.
	 * @param acceptance the new acceptance value for this node
	 */
	public void setAcceptance(Acceptance acceptance) {
		this.acceptance = acceptance;
	}
	
	/**
	 * Retrieves, for a given index, the respective child of this node.
	 * @param idx the alphabet symbol index
	 * @return the child for the given index, or {@code null} if there is no such child
	 */
	@Nullable
	public Node<I> getChild(int idx) {
		if(children == null) {
			return null;
		}
		return children[idx];
	}
	
	/**
	 * Sets the child for a given index.
	 * @param idx the alphabet symbol index
	 * @param alphabetSize the overall alphabet size; this is needed if a new children array needs to be created
	 * @param child the new child
	 */
	@SuppressWarnings("unchecked")
	public void setChild(int idx, int alphabetSize, Node<I> child) {
		if(children == null) {
			children = new Node[alphabetSize];
		}
		children[idx] = child;
	}
	

	public void makeSink() {
		children = null;
		acceptance = Acceptance.FALSE;
	}
}
