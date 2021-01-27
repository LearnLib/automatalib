package net.automatalib.modelcheckers.m3c.formula;

import net.automatalib.modelcheckers.m3c.formula.ctl.CTLNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeToString;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.NNFVisitor;

public abstract class FormulaNode {

    protected FormulaNode leftChild;
    protected FormulaNode rightChild;
    private Boolean belongsToMaxBlock;
    private int blockNumber;
    private int varNumber;

    public FormulaNode() {
    }

    public FormulaNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(FormulaNode leftChild) {
        this.leftChild = leftChild;
    }

    public FormulaNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(FormulaNode rightChild) {
        this.rightChild = rightChild;
    }

    public abstract <T> T accept(FormulaNodeVisitor<T> visitor);

    public abstract <T> T accept(CTLNodeVisitor<T> visitor);

    public abstract <T> T accept(MuCalcNodeVisitor<T> visitor);

    public FormulaNode toNNF() {
        return new NNFVisitor().transformToNNF(this);
    }

    public Boolean isBelongsToMaxBlock() {
        return belongsToMaxBlock;
    }

    public void setBelongsToMaxBlock(Boolean belongsToMaxBlock) {
        this.belongsToMaxBlock = belongsToMaxBlock;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getVarNumberLeft() {
        return leftChild.getVarNumber();
    }

    public int getVarNumber() {
        return varNumber;
    }

    public void setVarNumber(int varNumber) {
        this.varNumber = varNumber;
    }

    public int getVarNumberRight() {
        return rightChild.getVarNumber();
    }

    public String getLabel() {
        throw new UnsupportedOperationException("Remove this");
    }

    @Override
    public int hashCode() {
        int result = leftChild != null ? leftChild.hashCode() : 0;
        result = 31 * result + (rightChild != null ? rightChild.hashCode() : 0);
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

        FormulaNode that = (FormulaNode) o;

        if (leftChild != null ? !leftChild.equals(that.leftChild) : that.leftChild != null) {
            return false;
        }
        return rightChild != null ? rightChild.equals(that.rightChild) : that.rightChild == null;
    }

    @Override
    public String toString() {
        return new FormulaNodeToString().visit(this);
    }
}
