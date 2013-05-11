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
