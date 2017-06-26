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
package net.automatalib.util.minimizer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.graphs.UniversalIndefiniteGraph;


/**
 * 
 * @author Malte Isberner 
 *
 * @param <S> state class.
 * @param <L> transition 
 */
class HashMapInitialPartitioning<S, L> implements
		InitialPartitioning<S, L> {
	private final Map<Object,Block<S,L>> initialBlockMap
		= new HashMap<>();
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
			block = new Block<>(numExistingBlocks++);
			initialBlockMap.put(clazz, block);
		}
		return block;
	}

	@Override
	public Collection<Block<S, L>> getInitialBlocks() {
		return initialBlockMap.values();
	}

}
