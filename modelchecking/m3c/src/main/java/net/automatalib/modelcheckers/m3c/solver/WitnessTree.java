/* Copyright (C) 2013-2022 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.commons.smartcollections.ResizingArrayStorage;
import net.automatalib.graphs.base.compact.AbstractCompactBidiGraph;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class WitnessTree<L, AP> extends AbstractCompactBidiGraph<WitnessTreeState<?, L, ?, AP>, String> {

    private final ResizingArrayStorage<@Nullable WitnessTreeState<?, L, ?, AP>> nodeProperties;
    public int finishingNode;
    private @Nullable Word<L> result;

    WitnessTree() {
        this.nodeProperties = new ResizingArrayStorage<>(WitnessTreeState.class);
        finishingNode = -1;
    }

    @Override
    public void setNodeProperty(int node, WitnessTreeState<?, L, ?, AP> property) {
        nodeProperties.ensureCapacity(node + 1);
        nodeProperties.array[node] = property;
    }

    @Override
    public WitnessTreeState<?, L, ?, AP> getNodeProperty(int node) {
        if (node > nodeProperties.array.length) {
            return null;
        }

        return nodeProperties.array[node];
    }

    public Word<L> getWitness() {
        if (this.result == null) {
            this.result = extractPath();
        }

        return result;
    }

    void computePath() {
        getWitness();
    }

    private Word<L> extractPath() {
        int currentNode = this.getNode(this.finishingNode);

        final List<L> tmpPath = new ArrayList<>();

        while (currentNode >= 0) {
            final WitnessTreeState<?, L, ?, AP> prop = nodeProperties.array[currentNode];
            assert prop != null;

            final L label = prop.edgeLabel;
            prop.isPartOfResult = true;

            if (label != null) {
                tmpPath.add(label);
            }

            currentNode = prop.parentId;
        }

        Collections.reverse(tmpPath);
        return Word.fromList(tmpPath);
    }
}
