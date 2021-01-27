package net.automatalib.modelcheckers.m3c.formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.automatalib.modelcheckers.m3c.formula.modalmu.FixedPointFormulaNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public class DependencyGraph {

    /* All formulaNodes except FixedPoint- and VariableNode */
    private final List<FormulaNode> formulaNodes;

    /* FormulaNodes divided into equational blocks */
    private final List<EquationalBlock> blocks;

    /* nu X1 -> fixedPointVarMap.get("X1") returns the node associated to nu X1 */
    private final Map<String, FormulaNode> fixedPointVarMap;

    private int numVars;

    public DependencyGraph(FormulaNode root) {
        this.formulaNodes = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.fixedPointVarMap = new HashMap<>();
        FormulaNode rootNNF = root.toNNF();
        setVarNumbers(rootNNF);
        createEquationalBlocks(rootNNF);
    }

    private void sortBlocks() {
        for (EquationalBlock block : blocks) {
            //TODO: Test if this is always enough
            Collections.reverse(block.getNodes());
        }
    }

    private void createEquationalBlocks(FormulaNode root) {
        int blockNumber = 0;
        boolean isMax = true;
        //TODO: What do we do when root is not a FixedPointNode? -> Atm. use maxBlock?
        if (root instanceof LfpNode) {
            isMax = false;
        }
        EquationalBlock block = new EquationalBlock(isMax, 0);
        blocks.add(block);
        createEquationalBlocks(root, blockNumber);
        sortBlocks();
    }

    private void createEquationalBlocks(FormulaNode node, int blockNumber) {
        //TODO: Test this
        EquationalBlock currentBlock = blocks.get(blockNumber);
        boolean isMax = currentBlock.isMaxBlock();

        /* Check if new equational block has to be created */
        if (node instanceof GfpNode && !isMax) {
            blockNumber = blocks.size();
            currentBlock = new EquationalBlock(true, blockNumber);
            blocks.add(currentBlock);
        } else if (node instanceof LfpNode && isMax) {
            blockNumber = blocks.size();
            currentBlock = new EquationalBlock(false, blockNumber);
            blocks.add(currentBlock);
        }

        /* Only keep track of non FixedPoint/VariableNodes in blocks */
        if (!(node instanceof FixedPointFormulaNode || node instanceof VariableNode)) {
            currentBlock.addNode(node);
        }

        /* Recurse into subtrees */
        if (node.getLeftChild() != null) {
            createEquationalBlocks(node.getLeftChild(), blockNumber);
        }
        if (node.getRightChild() != null) {
            createEquationalBlocks(node.getRightChild(), blockNumber);
        }
    }

    private void setVarNumbers(FormulaNode root) {
        AtomicInteger numVarsAtomic = new AtomicInteger(0);
        setVarNumbers(root, numVarsAtomic);
        this.numVars = numVarsAtomic.get();
    }

    private void setVarNumbers(FormulaNode node, AtomicInteger numVars) {
        /* Fill fixedPointVarMap */
        if (node instanceof FixedPointFormulaNode) {
            fixedPointVarMap.put(((FixedPointFormulaNode) node).getVariable(), node);
        }

        /* Set node's variableNumber */
        if (node instanceof VariableNode) {
            /* VariableNode has same variableNumber as the fixed point it references */
            String refVariable = ((VariableNode) node).getVariable();
            FormulaNode refNode = fixedPointVarMap.get(refVariable);
            node.setVarNumber(refNode.getVarNumber());
        } else {
            node.setVarNumber(numVars.get());
        }

        /* Only count non FixedPoint/VariableNodes */
        if (!(node instanceof FixedPointFormulaNode || node instanceof VariableNode)) {
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

    public void printEquationalBlocks() {
        for (int blockIdx = 0; blockIdx < blocks.size(); blockIdx++) {
            EquationalBlock block = blocks.get(blockIdx);
            System.out.println("Block " + blockIdx + " / isMax: " + block.isMaxBlock());
            for (FormulaNode node : block.getNodes()) {
                System.out.println("x" + node.getVarNumber());
                if (node.getLeftChild() != null) {
                    System.out.println("lc: x" + node.getVarNumberLeft());
                }
                if (node.getRightChild() != null) {
                    System.out.println("rc: x" + node.getVarNumberRight());
                }
                System.out.println();
            }
        }
    }

    public EquationalBlock getBlock(int index) {
        return blocks.get(index);
    }

    public int getNumVariables() {
        return numVars;
    }

    public List<FormulaNode> getFormulaNodes() {
        return formulaNodes;
    }

    public List<EquationalBlock> getBlocks() {
        return blocks;
    }

    public Map<String, FormulaNode> getFixedPointVarMap() {
        return fixedPointVarMap;
    }
}
