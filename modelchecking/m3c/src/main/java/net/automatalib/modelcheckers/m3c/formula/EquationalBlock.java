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
import java.util.List;

public class EquationalBlock<L, AP> {

    private final List<FormulaNode<L, AP>> nodes;
    private boolean isMaxBlock;
    private int blockNumber;

    public EquationalBlock() {
        blockNumber = 0;
        nodes = new ArrayList<>();
    }

    public EquationalBlock(boolean isMaxBlock) {
        this.isMaxBlock = isMaxBlock;
        this.nodes = new ArrayList<>();
    }

    public EquationalBlock(boolean isMaxBlock, int blockNumber) {
        this.isMaxBlock = isMaxBlock;
        this.blockNumber = blockNumber;
        this.nodes = new ArrayList<>();
    }

    public boolean containsNodeWithVarNumber(int varNumber) {
        for (FormulaNode<L, AP> node : nodes) {
            if (node.getVarNumber() == varNumber) {
                return true;
            }
        }
        return false;
    }

    public List<FormulaNode<L, AP>> getNodes() {
        return nodes;
    }

    public void addNode(FormulaNode<L, AP> node) {
        nodes.add(node);
    }

    public void setIsMaxBlock(boolean isMaxBlock) {
        this.isMaxBlock = isMaxBlock;
    }

    public boolean isMaxBlock() {
        return isMaxBlock;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

}
