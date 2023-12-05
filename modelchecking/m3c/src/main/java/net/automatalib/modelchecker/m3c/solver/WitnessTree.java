/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.graph.Graph;
import net.automatalib.graph.impl.CompactGraph;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * A tree-like {@link Graph} that represents the BFS-style exploration of the tableau generated by the
 * {@link WitnessTreeExtractor}. Edges may represent the extraction of inner subformulae, procedural calls/returns
 * to/from other procedures, or actual steps in the transition system (modal nodes).
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
@SuppressWarnings("type.argument.type.incompatible") // we only add non-null properties
public class WitnessTree<L, AP> extends CompactGraph<WitnessTreeState<?, L, ?, AP>, String> {

    private @MonotonicNonNull Word<L> result;

    public Word<L> getWitness() {
        assert result != null;
        return result;
    }

    void computePath(int finishingNode) {
        int currentNode = finishingNode;

        final List<L> tmpPath = new ArrayList<>();

        while (currentNode >= 0) {
            final WitnessTreeState<?, L, ?, AP> prop = super.getNodeProperty(currentNode);
            assert prop != null;

            final L label = prop.edgeLabel;
            prop.isPartOfResult = true;

            if (label != null) {
                tmpPath.add(label);
            }

            currentNode = prop.parentId;
        }

        Collections.reverse(tmpPath);
        this.result = Word.fromList(tmpPath);
    }
}
