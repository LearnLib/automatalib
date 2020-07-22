package net.automatalib.ts.modal.transitions;

public interface MutableGroupMemberEdge<G> extends GroupMemberEdge<G> {

    void setGroup(G group);
    void setMemberId(int id);

}
