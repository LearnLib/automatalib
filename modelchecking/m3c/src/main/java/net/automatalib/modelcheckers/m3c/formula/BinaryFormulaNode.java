package net.automatalib.modelcheckers.m3c.formula;

public abstract class BinaryFormulaNode extends FormulaNode {

    public BinaryFormulaNode() {
    }

    public BinaryFormulaNode(FormulaNode leftChild, FormulaNode rightChild) {
        this.setLeftChild(leftChild);
        this.setRightChild(rightChild);
    }

}
