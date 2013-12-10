package net.automatalib.incremental.dfa;

final class PathElem {
	public final State state;
	public final int transIdx;
	
	public PathElem(State state, int transIdx) {
		this.state = state;
		this.transIdx = transIdx;
	}
}
