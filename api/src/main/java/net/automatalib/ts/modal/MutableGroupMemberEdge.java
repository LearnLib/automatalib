package net.automatalib.ts.modal;

public interface MutableGroupMemberEdge<G> extends GroupMemberEdge<G> {

    void setGroup(G group);
    void setMemberId(int id);

}
