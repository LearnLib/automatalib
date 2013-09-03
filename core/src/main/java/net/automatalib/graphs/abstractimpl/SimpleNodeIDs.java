package net.automatalib.graphs.abstractimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.NodeIDs;

public class SimpleNodeIDs<N> implements NodeIDs<N> {

	private final Map<N,Integer> nodeIds;
	private final List<N> nodes;
	
	public SimpleNodeIDs(Graph<N,?> graph) {
		this.nodes = new ArrayList<N>(graph.getNodes());
		int numNodes = this.nodes.size();
		this.nodeIds = new HashMap<N,Integer>((int)(numNodes / 0.75) + 1);
		
		for(int i = 0; i < numNodes; i++) {
			N node = this.nodes.get(i);
			nodeIds.put(node, i);
		}
	}

	@Override
	public int getNodeId(N node) {
		return nodeIds.get(node).intValue();
	}

	@Override
	public N getNode(int id) {
		return nodes.get(id);
	}
}
