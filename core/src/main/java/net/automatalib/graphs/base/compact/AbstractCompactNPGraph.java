package net.automatalib.graphs.base.compact;

import net.automatalib.commons.util.array.ResizingObjectArray;

public abstract class AbstractCompactNPGraph<E extends CompactEdge<EP>, NP, EP> 
		extends AbstractCompactGraph<E, NP, EP> {

	protected final ResizingObjectArray npStorage;
	
	public AbstractCompactNPGraph() {
		this.npStorage = new ResizingObjectArray();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public NP getNodeProperties(int node) {
		return (NP)npStorage.array[node];
	}

	@Override
	public void setNodeProperty(int node, NP property) {
		npStorage.array[node] = property;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.graphs.base.compact.AbstractCompactGraph#addIntNode(java.lang.Object)
	 */
	@Override
	public int addIntNode(NP properties) {
		int node = super.addIntNode(properties);
		npStorage.ensureCapacity(size);
		npStorage.array[node] = properties;
		return node;
	}

	

	
}
