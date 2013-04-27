package net.automatalib.graphs.base.compact;

public final class CompactEdge<EP> {
	
	private final int target;
	private EP property;

	public CompactEdge(int target) {
		this(target, null);
	}
	
	public CompactEdge(int target, EP property) {
		this.target = target;
		this.property = property;
	}
	
	public EP getProperty() {
		return property;
	}
	
	public void setProperty(EP property) {
		this.property = property;
	}
	
	public int getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return String.valueOf(property);
	}

}
