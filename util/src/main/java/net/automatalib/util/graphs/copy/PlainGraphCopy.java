package net.automatalib.util.graphs.copy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.MutableGraph;

final class PlainGraphCopy<N1, E1, N2, E2, NP2, EP2>
		extends AbstractGraphCopy<N1, E1, N2, E2, NP2, EP2, Graph<N1,E1>> {

	
	public PlainGraphCopy(Graph<N1, E1> inGraph,
			MutableGraph<N2, E2, NP2, EP2> outGraph,
			Mapping<? super N1, ? extends NP2> npMapping,
			Mapping<? super E1, ? extends EP2> epMapping) {
		super(inGraph, outGraph, npMapping, epMapping);
	}

	@Override
	public void doCopy() {
		List<Pair<N1,N2>> outNodes = new ArrayList<>(inGraph.size());
		// Copy nodes
		for(N1 n1 : inGraph) {
			N2 n2 = copyNode(n1);
			outNodes.add(Pair.make(n1, n2));
		}
		
		// Copy edges
		for(Pair<N1,N2> p : outNodes) {
			N1 n1 = p.getFirst();
			N2 n2 = p.getSecond();
			
			for(E1 edge : inGraph.getOutgoingEdges(n1)) {
				N1 tgt1 = inGraph.getTarget(edge);
				copyEdge(n2, edge, tgt1);
			}
		}
	}
	
}
