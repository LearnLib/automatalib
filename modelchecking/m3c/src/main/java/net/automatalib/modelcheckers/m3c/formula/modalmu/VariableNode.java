package net.automatalib.modelcheckers.m3c.formula.modalmu;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.CTLNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class VariableNode extends FormulaNode {

    private String variable;
    private FixedPointFormulaNode referencedFixedPoint;

    public VariableNode(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public FixedPointFormulaNode getReferencedFixedPoint() {
        return referencedFixedPoint;
    }

    public void setReferencedFixedPoint(FixedPointFormulaNode fixedPoint) {
        this.referencedFixedPoint = fixedPoint;
    }

    @Override
    public <T> T accept(FormulaNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> T accept(CTLNodeVisitor<T> visitor) {
        throw new IllegalArgumentException("VariableNode represents a ModalMuFormula");
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

        VariableNode that = (VariableNode) o;

        return variable != null ? variable.equals(that.variable) : that.variable == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (variable != null ? variable.hashCode() : 0);
        return result;
    }
}
