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

import java.util.HashSet;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public class NNFVisitor {

    private Set<String> varsToNegate;

    public NNFVisitor() {
    }

    public FormulaNode transformToNNF(FormulaNode node) {
        this.varsToNegate = new HashSet<>();
        return visit(node, false);
    }

    private FormulaNode visit(FormulaNode node, boolean negate) {
        if (node instanceof GfpNode) {
            return visitGFPNode((GfpNode) node, negate);
        } else if (node instanceof LfpNode) {
            return visitLFPNode((LfpNode) node, negate);
        } else if (node instanceof AndNode) {
            return visitAndNode((AndNode) node, negate);
        } else if (node instanceof AtomicNode) {
            return visitAtomicNode((AtomicNode) node, negate);
        } else if (node instanceof BoxNode) {
            return visitBoxNode((BoxNode) node, negate);
        } else if (node instanceof DiamondNode) {
            return visitDiamondNode((DiamondNode) node, negate);
        } else if (node instanceof FalseNode) {
            return visitFalseNode((FalseNode) node, negate);
        } else if (node instanceof VariableNode) {
            return visitVariableNode((VariableNode) node, negate);
        } else if (node instanceof NotNode) {
            return visitNotNode((NotNode) node, negate);
        } else if (node instanceof OrNode) {
            return visitOrNode((OrNode) node, negate);
        } else if (node instanceof TrueNode) {
            return visitTrueNode((TrueNode) node, negate);
        } else {
            throw new IllegalArgumentException("Node is not a ModalMuNode");
        }
    }

    private FormulaNode visitGFPNode(GfpNode node, boolean negate) {
        if (!negate) {
            FormulaNode childNode = visit(node.getLeftChild(), false);
            node.setLeftChild(childNode);
            return node;
        }
        varsToNegate.add(node.getVariable());
        LfpNode lfpNode = new LfpNode(node.getVariable());
        FormulaNode childNode = visit(node.getLeftChild(), true);
        lfpNode.setLeftChild(childNode);
        varsToNegate.remove(node.getVariable());
        return lfpNode;
    }

    private FormulaNode visitLFPNode(LfpNode node, boolean negate) {
        if (!negate) {
            FormulaNode childNode = visit(node.getLeftChild(), false);
            node.setLeftChild(childNode);
            return node;
        }
        varsToNegate.add(node.getVariable());
        GfpNode gfpNode = new GfpNode(node.getVariable());
        FormulaNode childNode = visit(node.getLeftChild(), true);
        gfpNode.setLeftChild(childNode);
        varsToNegate.remove(node.getVariable());
        return gfpNode;
    }

    private FormulaNode visitAndNode(AndNode node, boolean negate) {
        if (!negate) {
            FormulaNode leftChild = visit(node.getLeftChild(), false);
            FormulaNode rightChild = visit(node.getRightChild(), false);
            node.setLeftChild(leftChild);
            node.setRightChild(rightChild);
            return node;
        }
        OrNode orNode = new OrNode();
        FormulaNode leftChild = visit(node.getLeftChild(), true);
        FormulaNode rightChild = visit(node.getRightChild(), true);
        orNode.setLeftChild(leftChild);
        orNode.setRightChild(rightChild);
        return orNode;
    }

    private FormulaNode visitAtomicNode(AtomicNode node, boolean negate) {
        if (negate) {
            return new NotNode(node);
        }
        return node;
    }

    private FormulaNode visitBoxNode(BoxNode node, boolean negate) {
        if (!negate) {
            FormulaNode childNode = visit(node.getLeftChild(), false);
            node.setLeftChild(childNode);
            return node;
        }
        DiamondNode diamondNode = new DiamondNode(node.getAction());
        FormulaNode childNode = visit(node.getLeftChild(), true);
        diamondNode.setLeftChild(childNode);
        return diamondNode;
    }

    private FormulaNode visitDiamondNode(DiamondNode node, boolean negate) {
        if (!negate) {
            FormulaNode childNode = visit(node.getLeftChild(), false);
            node.setLeftChild(childNode);
            return node;
        }
        BoxNode boxNode = new BoxNode(node.getAction());
        FormulaNode childNode = visit(node.getLeftChild(), true);
        boxNode.setLeftChild(childNode);
        return boxNode;
    }

    private FormulaNode visitFalseNode(FalseNode node, boolean negate) {
        if (!negate) {
            return node;
        }
        return new TrueNode();
    }

    private FormulaNode visitVariableNode(VariableNode node, boolean negate) {
        boolean negateVariable = negate ^ varsToNegate.contains(node.getVariable());
        if (negateVariable) {
            return new NotNode(node);
        }
        return node;
    }

    private FormulaNode visitNotNode(NotNode node, boolean negate) {
        FormulaNode leftChild = node.getLeftChild();
        boolean invertedNegate = !negate;
        return visit(leftChild, invertedNegate);
    }

    private FormulaNode visitOrNode(OrNode node, boolean negate) {
        if (!negate) {
            FormulaNode leftChild = visit(node.getLeftChild(), false);
            FormulaNode rightChild = visit(node.getRightChild(), false);
            node.setLeftChild(leftChild);
            node.setRightChild(rightChild);
            return node;
        }
        AndNode andNode = new AndNode();
        FormulaNode leftChild = visit(node.getLeftChild(), true);
        FormulaNode rightChild = visit(node.getRightChild(), true);
        andNode.setLeftChild(leftChild);
        andNode.setRightChild(rightChild);
        return andNode;
    }

    private FormulaNode visitTrueNode(TrueNode node, boolean negate) {
        if (!negate) {
            return node;
        }
        return new FalseNode();
    }

}
