package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AWUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EWUNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public abstract class FormulaNodeVisitor<T> {

    public T visit(FormulaNode node) {
        return node.accept(this);
    }

    public abstract T visit(AFNode node);

    public abstract T visit(AGNode node);

    public abstract T visit(AUNode node);

    public abstract T visit(AWUNode node);

    public abstract T visit(EFNode node);

    public abstract T visit(EGNode node);

    public abstract T visit(EUNode node);

    public abstract T visit(EWUNode node);

    public abstract T visit(AndNode node);

    public abstract T visit(AtomicNode node);

    public abstract T visit(BoxNode node);

    public abstract T visit(DiamondNode node);

    public abstract T visit(FalseNode node);

    public abstract T visit(NotNode node);

    public abstract T visit(OrNode node);

    public abstract T visit(TrueNode node);

    public abstract T visit(GfpNode node);

    public abstract T visit(LfpNode node);

    public abstract T visit(VariableNode node);

}
