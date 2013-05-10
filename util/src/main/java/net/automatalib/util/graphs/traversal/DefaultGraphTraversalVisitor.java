package net.automatalib.util.graphs.traversal;

import net.automatalib.commons.util.Holder;

public class DefaultGraphTraversalVisitor<N, E, D> implements
		GraphTraversalVisitor<N, E, D> {

	@Override
	public GraphTraversalAction processInitial(N initialNode, Holder<D> outData) {
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public boolean startExploration(N node, D data) {
		return true;
	}

	@Override
	public void finishExploration(N node, D data) {
	}

	@Override
	public GraphTraversalAction processEdge(N srcNode, D srcData, E edge, N tgtNode,
			Holder<D> outData) {
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public void backtrackEdge(N srcNode, D srcData, E edge, N tgtNode, D tgtData) {
	}

}
