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

public abstract class AbstractCompactSimpleGraph<E extends CompactEdge<EP>, EP> extends
		AbstractCompactGraph<E, Void, EP> {

	
	public AbstractCompactSimpleGraph() {
		super();
	}

	public AbstractCompactSimpleGraph(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public Void getNodeProperties(int node) {
		return null;
	}

	@Override
	public void setNodeProperty(int node, Void property) {
	}

}
