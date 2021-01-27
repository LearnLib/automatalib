package net.automatalib.modelcheckers.m3c.formula.ctl;

import net.automatalib.modelcheckers.m3c.formula.BinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class AWUNode extends BinaryFormulaNode {

    public AWUNode() {
    }

    public AWUNode(FormulaNode leftChild, FormulaNode rightChild) {
        super(leftChild, rightChild);
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
        throw new UnsupportedOperationException("AWUNode represents a CTLFormula.");
    }

}
