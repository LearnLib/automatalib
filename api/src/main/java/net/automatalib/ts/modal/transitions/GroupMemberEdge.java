package net.automatalib.ts.modal.transitions;

public interface GroupMemberEdge<G> {

    G getGroup();
    int getMemberId();

}
