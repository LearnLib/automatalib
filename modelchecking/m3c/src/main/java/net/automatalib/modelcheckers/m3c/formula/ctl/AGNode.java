package net.automatalib.modelcheckers.m3c.formula.ctl;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.UnaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class AGNode extends UnaryFormulaNode {

    public AGNode() {
    }

    public AGNode(FormulaNode node) {
        super(node);
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
        throw new UnsupportedOperationException("AGNode represents a CTLFormula.");
    }

}
