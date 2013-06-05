package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class DelegateDOTHelper<N, E> implements GraphDOTHelper<N, E> {

	private final GraphDOTHelper<N,? super E> parent;
	
	protected DelegateDOTHelper(GraphDOTHelper<N,? super E> parent) {
		this.parent = parent;
	}

	@Override
	public void writePreamble(Appendable a) throws IOException {
		parent.writePreamble(a);
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
		parent.writePostamble(identifiers, a);
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		return parent.getNodeProperties(node, properties);
	}

	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt,
			Map<String, String> properties) {
		return parent.getEdgeProperties(src, edge, tgt, properties);
	}
}
