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
package net.automatalib.modelcheckers.m3c.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.automatalib.modelcheckers.m3c.formula.modalmu.AbstractFixedPointFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public class DependencyGraph<L, AP> {

    /* All formulaNodes except FixedPoint- and VariableNode */
    private final List<FormulaNode<L, AP>> formulaNodes;

    /* FormulaNodes divided into equational blocks */
    private final List<EquationalBlock<L, AP>> blocks;

    /* nu X1 -> fixedPointVarMap.get("X1") returns the node associated to nu X1 */
    private final Map<String, FormulaNode<L, AP>> fixedPointVarMap;
    private final FormulaNode<L, AP> ast;
    private int numVars;

    public DependencyGraph(FormulaNode<L, AP> root) {
        this.formulaNodes = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.fixedPointVarMap = new HashMap<>();
        this.ast = root.toNNF();
        setVarNumbers(ast);
        createEquationalBlocks(ast);
    }

    private void sortBlocks() {
        for (EquationalBlock<L, AP> block : blocks) {
            //TODO: Test if this is always enough
            Collections.reverse(block.getNodes());
        }
    }

    private void createEquationalBlocks(FormulaNode<L, AP> root) {
        int blockNumber = 0;
        boolean isMax = true;
        //TODO: What do we do when root is not a FixedPointNode? -> Atm. use maxBlock?
        if (root instanceof LfpNode) {
            isMax = false;
        }
        EquationalBlock<L, AP> block = new EquationalBlock<>(isMax);
        blocks.add(block);
        createEquationalBlocks(root, blockNumber);
        sortBlocks();
    }

    private void createEquationalBlocks(FormulaNode<L, AP> node, int blockNumber) {
        //TODO: Test this
        EquationalBlock<L, AP> currentBlock = blocks.get(blockNumber);
        boolean isMax = currentBlock.isMaxBlock();

        /* Check if new equational block has to be created */
        int newBlockNumber = blockNumber;
        if (node instanceof GfpNode && !isMax) {
            newBlockNumber = blocks.size();
            currentBlock = new EquationalBlock<>(true);
            blocks.add(currentBlock);
        } else if (node instanceof LfpNode && isMax) {
            newBlockNumber = blocks.size();
            currentBlock = new EquationalBlock<>(false);
            blocks.add(currentBlock);
        }

        /* Only keep track of non FixedPoint/VariableNodes in blocks */
        if (!(node instanceof AbstractFixedPointFormulaNode || node instanceof VariableNode)) {
            currentBlock.addNode(node);
        }

        /* Recurse into subtrees */
        if (node.getLeftChild() != null) {
            createEquationalBlocks(node.getLeftChild(), newBlockNumber);
        }
        if (node.getRightChild() != null) {
            createEquationalBlocks(node.getRightChild(), newBlockNumber);
        }
    }

    private void setVarNumbers(FormulaNode<L, AP> root) {
        AtomicInteger numVarsAtomic = new AtomicInteger(0);
        setVarNumbers(root, numVarsAtomic);
        this.numVars = numVarsAtomic.get();
    }

    private void setVarNumbers(FormulaNode<L, AP> node, AtomicInteger numVars) {
        /* Fill fixedPointVarMap */
        if (node instanceof AbstractFixedPointFormulaNode) {
            fixedPointVarMap.put(((AbstractFixedPointFormulaNode<L, AP>) node).getVariable(), node);
        }

        /* Set node's variableNumber */
        if (node instanceof VariableNode) {
            /* VariableNode has same variableNumber as the fixed point it references */
            String refVariable = ((VariableNode<L, AP>) node).getVariable();
            FormulaNode<L, AP> refNode = fixedPointVarMap.get(refVariable);
            node.setVarNumber(refNode.getVarNumber());
        } else {
            node.setVarNumber(numVars.get());
        }

        /* Only count non FixedPoint/VariableNodes */
        if (!(node instanceof AbstractFixedPointFormulaNode || node instanceof VariableNode)) {
            numVars.incrementAndGet();
            formulaNodes.add(node);
        }

        /* Recurse into subtrees */
        if (node.getLeftChild() != null) {
            setVarNumbers(node.getLeftChild(), numVars);
        }
        if (node.getRightChild() != null) {
            setVarNumbers(node.getRightChild(), numVars);
        }
    }

    public EquationalBlock<L, AP> getBlock(int index) {
        return blocks.get(index);
    }

    public int getNumVariables() {
        return numVars;
    }

    public List<FormulaNode<L, AP>> getFormulaNodes() {
        return formulaNodes;
    }

    public List<EquationalBlock<L, AP>> getBlocks() {
        return blocks;
    }

    public FormulaNode<L, AP> getAST() {
        return ast;
    }
}
