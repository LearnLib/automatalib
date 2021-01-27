package net.automatalib.modelcheckers.m3c.formula.ctl;

import net.automatalib.modelcheckers.m3c.formula.BinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class EWUNode extends BinaryFormulaNode {

    public EWUNode() {
    }

    public EWUNode(FormulaNode leftChild, FormulaNode rightChild) {
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
        throw new UnsupportedOperationException("EWUNode represents a CTLFormula.");
    }

}
