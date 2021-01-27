package net.automatalib.modelcheckers.m3c.formula;

public abstract class ModalFormulaNode extends UnaryFormulaNode {

    protected String action;

    public ModalFormulaNode(String action) {
        this.action = action;
    }

    public ModalFormulaNode(String action, FormulaNode node) {
        this.action = action;
        this.setLeftChild(node);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
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

        ModalFormulaNode that = (ModalFormulaNode) o;

        return action != null ? action.equals(that.action) : that.action == null;
    }
}
