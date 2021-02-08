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

public class CTLToMuCalc extends FormulaNodeVisitor<FormulaNode> {

    private int numFixedPointVars;

    public CTLToMuCalc() {
        numFixedPointVars = 0;
    }

    public FormulaNode toMuCalc(FormulaNode ctlFormula) {
        numFixedPointVars = 0;
        return visit(ctlFormula);
    }

    @Override
    public FormulaNode visit(AFNode node) {
        /* AF p = mu X.(toMu(p) | (<>true & []X)) */
        FormulaNode p = visit(node.getLeftChild());
        String fixedPointVar = getFixedPointVar();
        DiamondNode hasSuccessor = new DiamondNode("", new TrueNode());
        BoxNode allSuccessorSatisfyX = new BoxNode("", new VariableNode(fixedPointVar));
        AndNode and = new AndNode(hasSuccessor, allSuccessorSatisfyX);
        OrNode or = new OrNode(p, and);
        return new LfpNode(fixedPointVar, or);
    }

    private String getFixedPointVar() {
        return "Z" + numFixedPointVars++;
    }

    @Override
    public FormulaNode visit(AGNode node) {
        /* AG p = nu X.(p & []X) */
        FormulaNode p = visit(node.getLeftChild());
        String fixedPointVar = getFixedPointVar();
        BoxNode allSuccessorsSatisfyX = new BoxNode("", new VariableNode(fixedPointVar));
        AndNode and = new AndNode(p, allSuccessorsSatisfyX);
        return new GfpNode(fixedPointVar, and);
    }

    @Override
    public FormulaNode visit(AUNode node) {
        /* A[p U q] = mu X.(toMu(q) | (toMu(p) & (<>true & []X))) */
        FormulaNode p = visit(node.getLeftChild());
        FormulaNode q = visit(node.getRightChild());
        String fixedPointVar = getFixedPointVar();
        DiamondNode hasSuccessor = new DiamondNode("", new TrueNode());
        BoxNode allSuccessorsSatisfyX = new BoxNode("", new VariableNode(fixedPointVar));
        AndNode innerAnd = new AndNode(hasSuccessor, allSuccessorsSatisfyX);
        AndNode outerAnd = new AndNode(p, innerAnd);
        OrNode or = new OrNode(q, outerAnd);
        return new LfpNode(fixedPointVar, or);
    }

    @Override
    public FormulaNode visit(AWUNode node) {
        /* A[p WU q] = !E[!q U (!p & !q)] */
        FormulaNode p = visit(node.getLeftChild());
        FormulaNode q = visit(node.getRightChild());
        AndNode and = new AndNode(new NotNode(p), new NotNode(q));
        EUNode ewu = new EUNode(new NotNode(visit(node.getRightChild())), and);
        return visit(new NotNode(ewu));
    }

    @Override
    public FormulaNode visit(EFNode node) {
        /* EF p = mu X.(toMu(p) | <>X) */
        String fixedPointVar = getFixedPointVar();
        FormulaNode p = visit(node.getLeftChild());
        DiamondNode hasSuccessorSatisfyingX = new DiamondNode("", new VariableNode(fixedPointVar));
        OrNode orNode = new OrNode(p, hasSuccessorSatisfyingX);
        return new LfpNode(fixedPointVar, orNode);
    }

    @Override
    public FormulaNode visit(EGNode node) {
        /* EG p = nu X.(toMu(p) & (<>X | [] false)) */
        String fixedPointVar = getFixedPointVar();
        FormulaNode childNode = visit(node.getLeftChild());
        DiamondNode hasSuccessorSatisfyingX = new DiamondNode("", new VariableNode(fixedPointVar));
        BoxNode hasNoSuccessor = new BoxNode("", new FalseNode());
        OrNode or = new OrNode(hasSuccessorSatisfyingX, hasNoSuccessor);
        AndNode and = new AndNode(childNode, or);
        return new GfpNode(fixedPointVar, and);
    }

    @Override
    public FormulaNode visit(EUNode node) {
        /* E[p U q] => mu X.(toMu(q) | (toMu(p) & <>X)) */
        String fixedPointVar = getFixedPointVar();
        LfpNode lfpNode = new LfpNode(fixedPointVar);
        FormulaNode p = visit(node.getLeftChild());
        FormulaNode q = visit(node.getRightChild());
        AndNode andNode = new AndNode(p, new DiamondNode("", new VariableNode(fixedPointVar)));
        OrNode orNode = new OrNode(q, andNode);
        lfpNode.setLeftChild(orNode);
        return lfpNode;
    }

    @Override
    public FormulaNode visit(EWUNode node) {
        /* E[p WU q] = E[p U q] | EG p */
        FormulaNode p = visit(node.getLeftChild());
        FormulaNode q = visit(node.getRightChild());
        EUNode until = new EUNode(p, q);
        EGNode egNode = new EGNode(visit(node.getLeftChild()));
        return visit(new OrNode(until, egNode));
    }

    @Override
    public FormulaNode visit(AndNode node) {
        FormulaNode leftChild = visit(node.getLeftChild());
        FormulaNode rightChild = visit(node.getRightChild());
        return new AndNode(leftChild, rightChild);
    }

    @Override
    public FormulaNode visit(AtomicNode node) {
        return new AtomicNode(node.getProposition());
    }

    @Override
    public FormulaNode visit(BoxNode node) {
        FormulaNode childNode = visit(node.getLeftChild());
        return new BoxNode(node.getAction(), childNode);
    }

    @Override
    public FormulaNode visit(DiamondNode node) {
        FormulaNode childNode = visit(node.getLeftChild());
        return new DiamondNode(node.getAction(), childNode);
    }

    @Override
    public FormulaNode visit(FalseNode node) {
        return new FalseNode();
    }

    @Override
    public FormulaNode visit(NotNode node) {
        FormulaNode childNode = visit(node.getLeftChild());
        return new NotNode(childNode);
    }

    @Override
    public FormulaNode visit(OrNode node) {
        FormulaNode leftChild = visit(node.getLeftChild());
        FormulaNode rightChild = visit(node.getRightChild());
        return new OrNode(leftChild, rightChild);
    }

    @Override
    public FormulaNode visit(TrueNode node) {
        return new TrueNode();
    }

    @Override
    public FormulaNode visit(GfpNode node) {
        return new GfpNode(node.getVariable(), node.getLeftChild());
    }

    @Override
    public FormulaNode visit(LfpNode node) {
        return new LfpNode(node.getVariable(), node.getLeftChild());
    }

    @Override
    public FormulaNode visit(VariableNode node) {
        return new VariableNode(node.getVariable());
    }

}
