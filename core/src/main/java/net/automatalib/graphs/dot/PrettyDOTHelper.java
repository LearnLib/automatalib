package net.automatalib.graphs.dot;

import java.util.Map;

public class PrettyDOTHelper<N,E> extends EmptyDOTHelper<N, E> {

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		
		return true;
	}

	@Override
	public void getGlobalEdgeProperties(Map<String, String> properties) {
		properties.put(EdgeAttrs.ARROWHEAD, "vee");
	}

	@Override
	public void getGlobalNodeProperties(Map<String, String> properties) {
		properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
		properties.put(NodeAttrs.HEIGHT, "0.35");
		properties.put(NodeAttrs.WIDTH, "0.35");
		properties.put(NodeAttrs.FIXEDSIZE, "true");
	}



}
