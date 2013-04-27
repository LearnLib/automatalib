package net.automatalib.graphs.base.compact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.util.array.ResizingObjectArray;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.graphs.abstractimpl.AbstractMutableGraph;
import net.automatalib.graphs.concepts.NodeIDs;

public class CompactGraph<NP, EP> extends
		AbstractMutableGraph<Integer,CompactEdge<EP>,NP,EP> implements NodeIDs<Integer> {
	
	private int size;
	private final ResizingObjectArray edges;
	
	public CompactGraph() {
		this.size = 0;
		this.edges = new ResizingObjectArray();
	}
	
	@SuppressWarnings("unchecked")
	protected List<CompactEdge<EP>> getEdgeList(int node) {
		return (List<CompactEdge<EP>>)edges.array[node];
	}

	@Override
	public Collection<Integer> getNodes() {
		return CollectionsUtil.rangeList(0, size);
	}

	@Override
	public NodeIDs<Integer> nodeIDs() {
		return this;
	}

	@Override
	public Collection<CompactEdge<EP>> getOutgoingEdges(Integer node) {
		return getOutgoingEdges(node.intValue());
	}
	
	public Collection<CompactEdge<EP>> getOutgoingEdges(int node) {
		List<CompactEdge<EP>> edgeList = getEdgeList(node);
		return Collections.unmodifiableCollection(edgeList);
	}

	@Override
	public Integer getTarget(CompactEdge<EP> edge) {
		return Integer.valueOf(edge.getTarget());
	}

	@Override
	public Integer addNode(NP properties) {
		return Integer.valueOf(addIntNode(properties));
	}
	
	public int addIntNode() {
		return addIntNode(null);
	}
	public int addIntNode(NP properties) {
		edges.ensureCapacity(size + 1);
		edges.array[size] = new ArrayList<CompactEdge<EP>>();
		return size++;
	}

	@Override
	public CompactEdge<EP> connect(Integer source, Integer target, EP properties) {
		return connect(source.intValue(), target.intValue(), properties);
	}
	
	public CompactEdge<EP> connect(int source, int target, EP property) {
		CompactEdge<EP> edge = new CompactEdge<>(target, property);
		List<CompactEdge<EP>> edges = getEdgeList(source);
		edges.add(edge);
		return edge;
	}
	
	public CompactEdge<EP> connect(int source, int target) {
		return connect(source, target, null);
	}

	@Override
	public int getNodeId(Integer node) {
		return node.intValue();
	}

	@Override
	public Integer getNode(int id) {
		return Integer.valueOf(id);
	}
	

}
