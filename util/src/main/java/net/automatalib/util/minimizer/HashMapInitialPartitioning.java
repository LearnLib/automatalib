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
package net.automatalib.util.minimizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.graphs.UniversalIndefiniteGraph;


/**
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <S> state class.
 * @param <L> transition 
 */
class HashMapInitialPartitioning<S, L> implements
		InitialPartitioning<S, L> {
	private final Map<Object,Block<S,L>> initialBlockMap
		= new HashMap<Object,Block<S,L>>();
	private final UniversalIndefiniteGraph<S, ?, ?, L> graph;

	private int numExistingBlocks;
	
	public HashMapInitialPartitioning(UniversalIndefiniteGraph<S, ?, ?, L> graph) {
		this.graph = graph;
	}

	@Override
	public Block<S, L> getBlock(S origState) {
		Object clazz = graph.getNodeProperty(origState);
		Block<S,L> block = initialBlockMap.get(clazz);
		if(block == null) {
			block = new Block<S, L>(numExistingBlocks++);
			initialBlockMap.put(clazz, block);
		}
		return block;
	}

	@Override
	public Collection<Block<S, L>> getInitialBlocks() {
		return initialBlockMap.values();
	}

}
