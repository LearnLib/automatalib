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

public abstract class AbstractCompactNPGraph<E extends CompactEdge<EP>, NP, EP> 
		extends AbstractCompactGraph<E, NP, EP> {

	protected final ResizingObjectArray npStorage;
	
	public AbstractCompactNPGraph() {
		this.npStorage = new ResizingObjectArray();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public NP getNodeProperties(int node) {
		return (NP)npStorage.array[node];
	}

	@Override
	public void setNodeProperty(int node, NP property) {
		npStorage.array[node] = property;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.base.compact.AbstractCompactGraph#addIntNode(java.lang.Object)
	 */
	@Override
	public int addIntNode(NP properties) {
		int node = super.addIntNode(properties);
		npStorage.ensureCapacity(size);
		npStorage.array[node] = properties;
		return node;
	}

	

	
}
