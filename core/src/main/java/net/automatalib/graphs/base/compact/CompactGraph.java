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
package net.automatalib.graphs.base.compact;

import net.automatalib.commons.util.array.ResizingObjectArray;

public class CompactGraph<NP, EP> extends
		AbstractCompactGraph<CompactEdge<EP>, NP, EP> {
	
	private final ResizingObjectArray nodeProperties; 

	public CompactGraph() {
		super();
		this.nodeProperties = new ResizingObjectArray();
	}
	
	public CompactGraph(int initialCapacity) {
		super(initialCapacity);
		this.nodeProperties = new ResizingObjectArray(initialCapacity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NP getNodeProperties(int node) {
		if(node < nodeProperties.array.length)
			return (NP)nodeProperties.array[node];
		return null;
	}

	@Override
	public void setNodeProperty(int node, NP property) {
		if(node >= nodeProperties.array.length)
			nodeProperties.ensureCapacity(size);
		nodeProperties.array[node] = property;
	}

	@Override
	protected CompactEdge<EP> createEdge(int source, int target, EP property) {
		return new CompactEdge<>(target, property);
	}
	
	

}
