/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
package net.automatalib.graph.ads.impl;

import java.util.Map;

import net.automatalib.graph.ads.ADSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ADSNodeTest {

    private final ADSSymbolNode<Integer, Character, Boolean> symbolNode;
    private final ADSLeafNode<Integer, Character, Boolean> leafNode;

    public ADSNodeTest() {
        this.symbolNode = new ADSSymbolNode<>(null, 'a');
        this.leafNode = new ADSLeafNode<>(this.symbolNode, 0);
        this.symbolNode.getChildren().put(true, leafNode);
    }

    @Test
    public void testSymbolNode() {
        Assert.assertEquals(this.symbolNode.getSymbol(), 'a');
        this.symbolNode.setSymbol('b');
        Assert.assertEquals(this.symbolNode.getSymbol(), 'b');

        Assert.assertThrows(UnsupportedOperationException.class, this.symbolNode::getState);
        Assert.assertThrows(UnsupportedOperationException.class, () -> this.symbolNode.setState(1));

        final Map<Boolean, ADSNode<Integer, Character, Boolean>> children = this.symbolNode.getChildren();
        Assert.assertEquals(children.size(), 1);
        Assert.assertEquals(children.get(true), leafNode);
    }

    @Test
    public void testLeafNode() {
        Assert.assertEquals(this.leafNode.getState(), 0);
        this.leafNode.setState(1);
        Assert.assertEquals(this.leafNode.getState(), 1);

        Assert.assertThrows(UnsupportedOperationException.class, this.leafNode::getSymbol);
        Assert.assertThrows(UnsupportedOperationException.class, () -> this.leafNode.setSymbol('b'));
    }
}
