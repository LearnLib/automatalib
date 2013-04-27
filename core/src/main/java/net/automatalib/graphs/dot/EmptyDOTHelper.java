package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class EmptyDOTHelper<N, E> implements GraphDOTHelper<N, E> {


	@Override
	public void writePreamble(Appendable a) throws IOException {
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		return true;
	}

	@Override
	public boolean getEdgeProperties(E edge, Map<String, String> properties) {
		return true;
	}

}
