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

import java.util.Objects;

import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeToString;
import net.automatalib.modelcheckers.m3c.formula.visitor.FormulaNodeVisitor;
import net.automatalib.modelcheckers.m3c.formula.visitor.NNFVisitor;

public abstract class FormulaNode<L, AP> {

    protected FormulaNode<L, AP> leftChild;
    protected FormulaNode<L, AP> rightChild;
    private Boolean belongsToMaxBlock;
    private int blockNumber;
    private int varNumber;

    public FormulaNode() {
        this(null);
    }

    public FormulaNode(FormulaNode<L, AP> leftChild) {
        this(leftChild, null);
    }

    public FormulaNode(FormulaNode<L, AP> leftChild, FormulaNode<L, AP> rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public FormulaNode<L, AP> getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(FormulaNode<L, AP> leftChild) {
        this.leftChild = leftChild;
    }

    public FormulaNode<L, AP> getRightChild() {
        return rightChild;
    }

    public void setRightChild(FormulaNode<L, AP> rightChild) {
        this.rightChild = rightChild;
    }

    public abstract <T> T accept(FormulaNodeVisitor<T, L, AP> visitor);

    public FormulaNode<L, AP> toNNF() {
        return new NNFVisitor<L, AP>().transformToNNF(this);
    }

    public Boolean isBelongsToMaxBlock() {
        return belongsToMaxBlock;
    }

    public void setBelongsToMaxBlock(Boolean belongsToMaxBlock) {
        this.belongsToMaxBlock = belongsToMaxBlock;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getVarNumberLeft() {
        return leftChild.getVarNumber();
    }

    public int getVarNumber() {
        return varNumber;
    }

    public void setVarNumber(int varNumber) {
        this.varNumber = varNumber;
    }

    public int getVarNumberRight() {
        return rightChild.getVarNumber();
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(leftChild);
        result = 31 * result + Objects.hash(rightChild);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormulaNode<?, ?> that = (FormulaNode<?, ?>) o;

        return Objects.equals(leftChild, that.leftChild) && Objects.equals(rightChild, that.rightChild);
    }

    @Override
    public String toString() {
        return new FormulaNodeToString<L, AP>().visit(this);
    }
}
