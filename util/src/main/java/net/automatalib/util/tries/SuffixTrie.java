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
package net.automatalib.util.tries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.abstractimpl.AbstractGraph;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

public class SuffixTrie<I> extends AbstractGraph<SuffixTrieNode<I>,SuffixTrieNode<I>> implements DOTPlottableGraph<SuffixTrieNode<I>,SuffixTrieNode<I>> {
	
	public static final boolean DEFAULT_GRAPH_REPRESENTABLE = true;
	
	protected final SuffixTrieNode<I> root;
	protected final List<SuffixTrieNode<I>> nodes;

	/**
	 * Constructor. Constructs a graph-representable suffix trie.
	 */
	public SuffixTrie() {
		this(DEFAULT_GRAPH_REPRESENTABLE);
	}
	
	/**
	 * Constructor. Constructs a suffix trie.
	 * @param graphRepresentable whether the trie should be graph representable.
	 */
	public SuffixTrie(boolean graphRepresentable) {
		this(graphRepresentable, new SuffixTrieNode<I>());
	}
	
	/**
	 * Internal constructor. Allows to override the root node.
	 * @param root the root node.
	 */
	protected SuffixTrie(SuffixTrieNode<I> root) {
		this(DEFAULT_GRAPH_REPRESENTABLE, root);
	}
	
	/**
	 * Internal constructor. Allows to override the root node
	 * and graph representability.
	 * @param graphRepresentable whether the trie should be graph representable.
	 * @param root the root node.
	 */
	protected SuffixTrie(boolean graphRepresentable, SuffixTrieNode<I> root) {
		this.root = root;
		if(graphRepresentable) {
			this.nodes = new ArrayList<>();
			this.nodes.add(root);
		}
		else {
			this.nodes = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.Graph#getNodes()
	 */
	@Override
	public Collection<SuffixTrieNode<I>> getNodes() {
		if(nodes == null)
			throw new UnsupportedOperationException("This trie is not graph representable");
		
		return Collections.unmodifiableCollection(nodes);
	}


	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getOutgoingEdges(java.lang.Object)
	 */
	@Override
	public Collection<SuffixTrieNode<I>> getOutgoingEdges(SuffixTrieNode<I> node) {
		if(nodes == null)
			throw new UnsupportedOperationException("This trie is not graph representable");
		
		SuffixTrieNode<I> parent = node.getParent();
		if(parent == null)
			return Collections.emptySet();
		return Collections.singleton(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#getTarget(java.lang.Object)
	 */
	@Override
	public SuffixTrieNode<I> getTarget(SuffixTrieNode<I> edge) {
		if(nodes == null)
			throw new UnsupportedOperationException("This trie is not graph representable");
		
		return edge;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.dot.DOTPlottableGraph#getGraphDOTHelper()
	 */
	@Override
	public GraphDOTHelper<SuffixTrieNode<I>, SuffixTrieNode<I>> getGraphDOTHelper() {
		if(nodes == null)
			throw new UnsupportedOperationException("This trie is not graph representable");
		
		return new EmptyDOTHelper<SuffixTrieNode<I>,SuffixTrieNode<I>>() {

			/* (non-Javadoc)
			 * @see net.automatalib.graphs.dot.DefaultDOTHelper#getNodeProperties(java.lang.Object, java.util.Map)
			 */
			@Override
			public boolean getNodeProperties(SuffixTrieNode<I> node,
					Map<String, String> properties) {
				if(!super.getNodeProperties(node, properties))
					return false;
				String lbl = node.isRoot() ? "ε" : String.valueOf(node.getSymbol());
				properties.put(NodeAttrs.LABEL, lbl);
				return true;
			}
			
		};
	}

	
	/**
	 * Adds a word to the trie.
	 * @param symbol the first symbol of the word.
	 * @param parent the remaining suffix of the word.
	 * @return a trie node corresponding to the inserted word.
	 */
	public SuffixTrieNode<I> add(I symbol, SuffixTrieNode<I> parent) {
		SuffixTrieNode<I> n = new SuffixTrieNode<>(symbol, parent);
		if(nodes != null) {
			nodes.add(n);
		}
		return n;
	}

	/**
	 * Returns the root of this trie.
	 * @return the root of this trie.
	 */
	public SuffixTrieNode<I> getRoot() {
		return root;
	}


}
