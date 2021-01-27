package net.automatalib.modelcheckers.m3c.formula;

import net.automatalib.modelcheckers.m3c.formula.ctl.CTLNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.modalmu.MuCalcNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;

public class TrueNode extends TerminalFormulaNode {

    public TrueNode() {
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
        return visitor.visit(this);
    }

}
