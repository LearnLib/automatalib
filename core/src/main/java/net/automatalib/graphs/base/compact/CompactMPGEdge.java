package net.automatalib.graphs.base.compact;

public class CompactMPGEdge<L, EP> extends CompactEdge<EP> {

    private L label;

    public CompactMPGEdge(int target, EP property, L label) {
        super(target, property);
        this.label = label;
    }

    public L getLabel() {
        return label;
    }

    void setLabel(L label) {
        this.label = label;
    }
}
