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

/**
 * @author murtovi
 */
public class EquationalBlock<L, AP> {

    private final List<FormulaNode<L, AP>> nodes;
    private final boolean isMaxBlock;

    public EquationalBlock(boolean isMaxBlock) {
        this.isMaxBlock = isMaxBlock;
        this.nodes = new ArrayList<>();
    }

    public List<FormulaNode<L, AP>> getNodes() {
        return nodes;
    }

    public void addNode(FormulaNode<L, AP> node) {
        nodes.add(node);
    }

    public boolean isMaxBlock() {
        return isMaxBlock;
    }

}
