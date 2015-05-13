/* Copyright (C) 2013-2015 TU Dortmund
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
package net.automatalib.graphs;

import net.automatalib.graphs.dot.EmptyDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;


/**
 * Graph interface. Like an {@link IndefiniteGraph}, but with the additional requirement
 * that the set of nodes be finite.
 * 
 * @author Malte Isberner
 *
 * @param <N> node type
 * @param <E> edge type
 */
public interface Graph<N, E> extends IndefiniteGraph<N,E>, SimpleGraph<N> {
	
	@Override
	default public GraphDOTHelper<N, ? super E> getGraphDOTHelper() {
		return new EmptyDOTHelper<N,E>();
	}
	
	@Override
	default public Graph<N,E> asNormalGraph() {
		return this;
	}
}
