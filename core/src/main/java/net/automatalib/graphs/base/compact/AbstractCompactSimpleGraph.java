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
