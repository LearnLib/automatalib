package net.automatalib.modelcheckers.m3c.formula.modalmu;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.UnaryFormulaNode;

public abstract class FixedPointFormulaNode extends UnaryFormulaNode {

    private String variable;

    public FixedPointFormulaNode(String variable) {
        this.variable = variable;
    }

    public FixedPointFormulaNode(String variable, FormulaNode leftChild) {
        this.variable = variable;
        this.setLeftChild(leftChild);
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
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

        FixedPointFormulaNode that = (FixedPointFormulaNode) o;

        return variable != null ? variable.equals(that.variable) : that.variable == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (variable != null ? variable.hashCode() : 0);
        return result;
    }
}
