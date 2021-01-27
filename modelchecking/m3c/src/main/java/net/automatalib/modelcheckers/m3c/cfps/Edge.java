package net.automatalib.modelcheckers.m3c.cfps;

public class Edge {

    private final State source;
    private final State target;
    private final String label;
    private final EdgeType edgeType;

    public Edge(State source, State dest, String label, EdgeType edgeType) {
        this.source = source;
        this.target = dest;
        this.label = label;
        this.edgeType = edgeType;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public boolean isProcessCall() {
        return edgeType == EdgeType.MUST_PROCESS || edgeType == EdgeType.MAY_PROCESS;
    }

    public boolean isMayProcessCall() {
        return edgeType == EdgeType.MAY_PROCESS;
    }

    public boolean isMust() {
        return edgeType == EdgeType.MUST || isMustProcessCall();
    }

    public boolean isMustProcessCall() {
        return edgeType == EdgeType.MUST_PROCESS;
    }

    public boolean isMay() {
        return edgeType == EdgeType.MAY;
    }

    public boolean labelMatches(String label) {
        return label.equals("") || this.label.equals(label);
    }

}
