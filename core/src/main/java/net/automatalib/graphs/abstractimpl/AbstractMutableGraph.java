package net.automatalib.graphs.abstractimpl;

import net.automatalib.graphs.MutableGraph;

public abstract class AbstractMutableGraph<N, E, NP, EP> extends AbstractGraph<N, E>
		implements MutableGraph<N, E, NP, EP> {

	@Override
	public N addNode() {
		return addNode(null);
	}

	@Override
	public E connect(N source, N target) {
		return connect(source, target, null);
	}

}
