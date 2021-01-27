package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BinaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.UnaryFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AWUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EWUNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.FixedPointFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public class FormulaNodeToString extends FormulaNodeVisitor<String> {

    @Override
    public String visit(AFNode node) {
        return visitUnaryFormulaNode(node, "AF");
    }

    @Override
    public String visit(AGNode node) {
        return visitUnaryFormulaNode(node, "AG");
    }

    @Override
    public String visit(AUNode node) {
        return visitUntilNode(node, "A", "U");
    }

    @Override
    public String visit(AWUNode node) {
        return visitUntilNode(node, "A", "W");
    }

    @Override
    public String visit(EFNode node) {
        return visitUnaryFormulaNode(node, "EF");
    }

    @Override
    public String visit(EGNode node) {
        return visitUnaryFormulaNode(node, "EG");
    }

    @Override
    public String visit(EUNode node) {
        return visitUntilNode(node, "E", "U");
    }

    @Override
    public String visit(EWUNode node) {
        return visitUntilNode(node, "E", "W");
    }

    @Override
    public String visit(AndNode node) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return visitBinaryFormulaNode("&&", lcToString, rcToString);
    }

    @Override
    public String visit(AtomicNode node) {
        return "\"" + node.getProposition() + "\"";
    }

    @Override
    public String visit(BoxNode node) {
        String operator = "[" + node.getAction() + "]";
        return visitUnaryFormulaNode(node, operator);
    }

    @Override
    public String visit(DiamondNode node) {
        String operator = "<" + node.getAction() + ">";
        return visitUnaryFormulaNode(node, operator);
    }

    @Override
    public String visit(FalseNode node) {
        return "false";
    }

    @Override
    public String visit(NotNode node) {
        String childToString = visit(node.getLeftChild());
        return "(!" + childToString + ")";
    }

    @Override
    public String visit(OrNode node) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return visitBinaryFormulaNode("||", lcToString, rcToString);
    }

    @Override
    public String visit(TrueNode node) {
        return "true";
    }

    @Override
    public String visit(GfpNode node) {
        return visitMuCalcNode(node);
    }

    @Override
    public String visit(LfpNode node) {
        return visitMuCalcNode(node);
    }

    @Override
    public String visit(VariableNode node) {
        return node.getVariable();
    }

    private String visitMuCalcNode(FixedPointFormulaNode node) {
        String childToString = visit(node.getLeftChild());
        String operator = node instanceof GfpNode ? "nu" : "mu";
        return "(" + operator + " " + node.getVariable() + ".(" + childToString + "))";
    }

    private String visitBinaryFormulaNode(String operator, String lcToString, String rcToString) {
        return "(" + lcToString + " " + operator + " " + rcToString + ")";
    }

    private String visitUntilNode(BinaryFormulaNode node, String quantifier, String weakOrStrong) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return "(" + quantifier + "(" + lcToString + " " + weakOrStrong + " " + rcToString + "))";
    }

    private String visitUnaryFormulaNode(UnaryFormulaNode node, String operator) {
        String childToString = visit(node.getLeftChild());
        return "(" + operator + " " + childToString + ")";
    }

}
