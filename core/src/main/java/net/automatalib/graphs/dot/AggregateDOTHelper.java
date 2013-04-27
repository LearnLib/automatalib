package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class AggregateDOTHelper<N, E> implements GraphDOTHelper<N, E> {
	
	private final List<GraphDOTHelper<N,? super E>> helpers;
	
	public AggregateDOTHelper() {
		helpers = new ArrayList<>();
	}
	
	public AggregateDOTHelper(List<? extends GraphDOTHelper<N, ? super E>> helpers) {
		this.helpers = new ArrayList<>(helpers);
	}
	
	public void add(GraphDOTHelper<N,? super E> helper) {
		this.helpers.add(helper);
	}

	@Override
	public void writePreamble(Appendable a) throws IOException {
		for(GraphDOTHelper<N,? super E> helper : helpers)
			helper.writePreamble(a);
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
		for(GraphDOTHelper<N,? super E> helper : helpers)
			helper.writePostamble(identifiers, a);
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		for(GraphDOTHelper<N,? super E> helper : helpers) {
			if(!helper.getNodeProperties(node, properties))
				return false;
		}
		return true;
	}

	@Override
	public boolean getEdgeProperties(E edge, Map<String, String> properties) {
		for(GraphDOTHelper<N,? super E> helper : helpers) {
			if(!helper.getEdgeProperties(edge, properties))
				return false;
		}
		return true;
	}

}
