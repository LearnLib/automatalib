package net.automatalib.ts.modal.transitions;

public class GroupMemberEdgePropertyImpl<G> implements MutableGroupMemberEdge<G> {

    private G group;
    private int memberId;

    @Override
    public void setGroup(G group) {
        this.group = group;
    }

    @Override
    public void setMemberId(int id) {
        memberId = id;
    }

    @Override
    public G getGroup() {
        return group;
    }

    @Override
    public int getMemberId() {
        return memberId;
    }


}
