package net.automatalib.modelcheckers.m3c.formula;

import java.util.Objects;

import net.automatalib.modelcheckers.m3c.formula.ctl.CTLNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class AtomicNode extends TerminalFormulaNode {

    private String proposition;

    public AtomicNode(String proposition) {
        this.proposition = proposition;
    }

    public String getProposition() {
        return proposition;
    }

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(CTLNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(MuCalcNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AtomicNode that = (AtomicNode) o;

        return Objects.equals(proposition, that.proposition);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (proposition != null ? proposition.hashCode() : 0);
        return result;
    }

}
