package net.automatalib.incremental.mealy.tree;

public final class AnnotatedEdge<I, O> {
	private final Edge<I,O> edge;
	private final I input;
	
	public AnnotatedEdge(Edge<I,O> edge, I input) {
		this.edge = edge;
		this.input = input;
	}
	
	public Edge<I,O> getEdge() {
		return edge;
	}
	
	public I getInput() {
		return input;
	}
	
	public O getOutput() {
		return edge.getOutput();
	}
	
	public Node<I,O> getTarget() {
		return edge.getTarget();
	}
}
