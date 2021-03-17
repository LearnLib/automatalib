/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.modelcheckers.m3c.formula.visitor;

import java.util.Objects;
import java.util.StringJoiner;

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

public class FormulaNodeToString<L, AP> extends FormulaNodeVisitor<String, L, AP> {

    @Override
    public String visit(AFNode<L, AP> node) {
        return visitUnaryFormulaNode(node, "AF");
    }

    @Override
    public String visit(AGNode<L, AP> node) {
        return visitUnaryFormulaNode(node, "AG");
    }

    @Override
    public String visit(AUNode<L, AP> node) {
        return visitUntilNode(node, "A", "U");
    }

    @Override
    public String visit(AWUNode<L, AP> node) {
        return visitUntilNode(node, "A", "W");
    }

    @Override
    public String visit(EFNode<L, AP> node) {
        return visitUnaryFormulaNode(node, "EF");
    }

    @Override
    public String visit(EGNode<L, AP> node) {
        return visitUnaryFormulaNode(node, "EG");
    }

    @Override
    public String visit(EUNode<L, AP> node) {
        return visitUntilNode(node, "E", "U");
    }

    @Override
    public String visit(EWUNode<L, AP> node) {
        return visitUntilNode(node, "E", "W");
    }

    @Override
    public String visit(AndNode<L, AP> node) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return visitBinaryFormulaNode("&&", lcToString, rcToString);
    }

    @Override
    public String visit(AtomicNode<L, AP> node) {
        final StringJoiner sj = new StringJoiner(",", "\"", "\"");
        for (AP ap : node.getPropositions()) {
            sj.add(Objects.toString(ap));
        }
        return sj.toString();
    }

    @Override
    public String visit(BoxNode<L, AP> node) {
        final L action = node.getAction();
        final String operator = action == null ? "[]" : "[" + action + "]";
        return visitUnaryFormulaNode(node, operator);
    }

    @Override
    public String visit(DiamondNode<L, AP> node) {
        final L action = node.getAction();
        final String operator = action == null ? "<>" : "<" + action + ">";
        return visitUnaryFormulaNode(node, operator);
    }

    @Override
    public String visit(FalseNode<L, AP> node) {
        return "false";
    }

    @Override
    public String visit(NotNode<L, AP> node) {
        String childToString = visit(node.getLeftChild());
        return "(!" + childToString + ")";
    }

    @Override
    public String visit(OrNode<L, AP> node) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return visitBinaryFormulaNode("||", lcToString, rcToString);
    }

    @Override
    public String visit(TrueNode<L, AP> node) {
        return "true";
    }

    @Override
    public String visit(GfpNode<L, AP> node) {
        return visitMuCalcNode(node);
    }

    @Override
    public String visit(LfpNode<L, AP> node) {
        return visitMuCalcNode(node);
    }

    @Override
    public String visit(VariableNode<L, AP> node) {
        return node.getVariable();
    }

    private String visitMuCalcNode(FixedPointFormulaNode<L, AP> node) {
        String childToString = visit(node.getLeftChild());
        String operator = node instanceof GfpNode ? "nu" : "mu";
        return "(" + operator + " " + node.getVariable() + ".(" + childToString + "))";
    }

    private String visitBinaryFormulaNode(String operator, String lcToString, String rcToString) {
        return "(" + lcToString + " " + operator + " " + rcToString + ")";
    }

    private String visitUntilNode(BinaryFormulaNode<L, AP> node, String quantifier, String weakOrStrong) {
        String lcToString = visit(node.getLeftChild());
        String rcToString = visit(node.getRightChild());
        return "(" + quantifier + "(" + lcToString + " " + weakOrStrong + " " + rcToString + "))";
    }

    private String visitUnaryFormulaNode(UnaryFormulaNode<L, AP> node, String operator) {
        String childToString = visit(node.getLeftChild());
        return "(" + operator + " " + childToString + ")";
    }

}
