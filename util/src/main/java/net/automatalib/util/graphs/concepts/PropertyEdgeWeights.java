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
package net.automatalib.util.graphs.concepts;

import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.concepts.EdgeWeights;

public class PropertyEdgeWeights<E> implements EdgeWeights<E> {
	
	private final UniversalGraph<?,E,?,? extends Number> graph;

	public PropertyEdgeWeights(UniversalGraph<?, E, ?, ? extends Number> graph) {
		this.graph = graph;
	}
	
	@Override
	public float getEdgeWeight(E edge) {
		Number prop = graph.getEdgeProperty(edge);
		return prop.floatValue();
	}

	

}
