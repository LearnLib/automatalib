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
package net.automatalib.util.graphs.copy;

import java.util.Collection;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.Mappings;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.IndefiniteGraph;
import net.automatalib.graphs.MutableGraph;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.graphs.UniversalIndefiniteGraph;
import net.automatalib.util.graphs.Graphs;
import net.automatalib.util.traversal.TraversalOrder;

public class GraphCopy {
	
	
	
	public static <N1,E1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyPlain(Graph<N1, E1> in,
			MutableGraph<N2, E2, NP2, EP2> out,
			Mapping<? super N1,? extends NP2> npMapping,
			Mapping<? super E1,? extends EP2> epMapping) {
		PlainGraphCopy<N1, E1, N2, E2, NP2, EP2> copy = new PlainGraphCopy<>(in, out, npMapping, epMapping);
		copy.doCopy();
		return copy.getNodeMapping();
	}
	
	public static <N1,E1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyPlain(UniversalGraph<N1, E1,? extends NP2, ? extends EP2> in,
			MutableGraph<N2, E2, NP2, EP2> out) {
		Mapping<N1,? extends NP2> npMapping = Graphs.nodeProperties(in);
		Mapping<E1,? extends EP2> epMapping = Graphs.edgeProperties(in);
		return copyPlain(in, out, npMapping, epMapping);
	}
	
	public static <N1,E1,NP1,EP1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyUniversalPlain(UniversalGraph<N1, E1, NP1, EP1> in,
			MutableGraph<N2, E2, NP2, EP2> out,
			Mapping<? super NP1,? extends NP2> npConversion,
			Mapping<? super EP1,? extends EP2> epConversion) {
		Mapping<? super N1,? extends NP2> npMapping = Mappings.compose(Graphs.nodeProperties(in), npConversion);
		Mapping<? super E1,? extends EP2> epMapping = Mappings.compose(Graphs.edgeProperties(in), epConversion);
		return copyPlain(in, out, npMapping, epMapping);
	}
	
	public static <N1,E1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyTraversal(IndefiniteGraph<N1,E1> in,
			MutableGraph<N2,E2,NP2,EP2> out,
			TraversalOrder order,
			int limit,
			Collection<? extends N1> initialNodes,
			Mapping<? super N1,? extends NP2> npMapping,
			Mapping<? super E1,? extends EP2> epMapping) {
		TraversalGraphCopy<N1, E1, N2, E2, NP2, EP2> copy
			= new TraversalGraphCopy<>(order, limit, in, initialNodes, out, npMapping, epMapping);
		copy.doCopy();
		return copy.getNodeMapping();
	}
	
	public static <N1,E1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyTraversal(UniversalIndefiniteGraph<N1,E1,? extends NP2,? extends EP2> in,
			MutableGraph<N2,E2,NP2,EP2> out,
			TraversalOrder order,
			int limit,
			Collection<? extends N1> initialNodes) {
		Mapping<N1,? extends NP2> npMapping = Graphs.nodeProperties(in);
		Mapping<E1,? extends EP2> epMapping = Graphs.edgeProperties(in);
		return copyTraversal(in, out, order, limit, initialNodes, npMapping, epMapping);
	}
	
	public static <N1,E1,NP1,EP1,N2,E2,NP2,EP2>
	Mapping<N1,N2> copyUniversalTraversal(UniversalIndefiniteGraph<N1,E1,NP1,EP1> in,
			MutableGraph<N2, E2, NP2, EP2> out,
			TraversalOrder order,
			int limit,
			Collection<? extends N1> initialNodes,
			Mapping<? super NP1,? extends NP2> npConversion,
			Mapping<? super EP1,? extends EP2> epConversion) {
		Mapping<? super N1,? extends NP2> npMapping = Mappings.compose(Graphs.nodeProperties(in), npConversion);
		Mapping<? super E1,? extends EP2> epMapping = Mappings.compose(Graphs.edgeProperties(in), epConversion);
		return copyTraversal(in, out, order, limit, initialNodes, npMapping, epMapping);
	}

}
