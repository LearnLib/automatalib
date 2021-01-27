package net.automatalib.modelcheckers.m3c.formula;

public abstract class UnaryFormulaNode extends FormulaNode {

    public UnaryFormulaNode() {
    }

    public UnaryFormulaNode(FormulaNode childNode) {
        this.setLeftChild(childNode);
    }

}
