package net.automatalib.ts.modal.transitions;

import java.util.Objects;

public class ModalContractMembershipEdgePropertyImpl extends ModalContractEdgePropertyImpl
        implements MutableGroupMemberEdge<ModalContractEdgeProperty.EdgeColor> {

    private int memberId;

    public ModalContractMembershipEdgePropertyImpl(ModalType type, boolean tau, EdgeColor color, int memberId) {
        super(type, tau, color);
        this.memberId = memberId;
    }

    public ModalContractMembershipEdgePropertyImpl(ModalContractMembershipEdgePropertyImpl other) {
        super(other.getType(), other.isTau(), other.getColor());
        this.memberId = other.memberId;
    }

    @Override
    public void setGroup(EdgeColor group) {
        setColor(group);
    }

    @Override
    public void setMemberId(int id) {
        memberId = id;
    }

    @Override
    public EdgeColor getGroup() {
        return getColor();
    }

    @Override
    public int getMemberId() {
        return memberId;
    }

    @Override
    public String toString() {
        return "color={"+getColor()+"}, memberId={" + memberId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        ModalContractMembershipEdgePropertyImpl that = (ModalContractMembershipEdgePropertyImpl) o;
        return memberId == that.memberId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), memberId);
    }
}
