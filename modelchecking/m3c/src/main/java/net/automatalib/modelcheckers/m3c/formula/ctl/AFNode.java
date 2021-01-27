package net.automatalib.modelcheckers.m3c.formula.ctl;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.UnaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class AFNode extends UnaryFormulaNode {

    public AFNode() {
    }

    public AFNode(FormulaNode childNode) {
        super(childNode);
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
        throw new UnsupportedOperationException("AFNode represents a CTLFormula.");
    }

}
