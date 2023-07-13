/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.spmm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.Triple;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author frohme
 */
public class SPMMVisualizationHelper<I, S> extends DefaultVisualizationHelper<Pair<I, S>, Triple<I, I, S>> {

    final Map<I, MealyMachine<S, I, ?, ?>> subModels;
    final Alphabet<I> callAlphabet;

    // cast is fine, because we make sure to only query states belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public SPMMVisualizationHelper(Alphabet<I> callAlphabet,
                                   Map<I, ? extends MealyMachine<? extends S, I, ?, ?>> subModels) {
        this.callAlphabet = callAlphabet;
        this.subModels = (Map<I, MealyMachine<S, I, ?, ?>>) subModels;
    }

    @Override
    protected Collection<Pair<I, S>> initialNodes() {
        final List<Pair<I, S>> initialNodes = new ArrayList<>(this.subModels.size());

        for (Entry<I, MealyMachine<S, I, ?, ?>> e : subModels.entrySet()) {
            final S init = e.getValue().getInitialState();
            assert init != null;
            initialNodes.add(Pair.of(e.getKey(), init));
        }

        return initialNodes;
    }

    @Override
    public boolean getNodeProperties(Pair<I, S> node, Map<String, String> properties) {

        if (!super.getNodeProperties(node, properties)) {
            return false;
        }

        properties.put(NodeAttrs.SHAPE, NodeShapes.CIRCLE);
        properties.put(NodeAttrs.LABEL, node.getFirst() + " " + node.getSecond());

        return true;
    }

    @Override
    public boolean getEdgeProperties(Pair<I, S> src,
                                     Triple<I, I, S> edge,
                                     Pair<I, S> tgt,
                                     Map<String, String> properties) {

        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final I identifier = edge.getFirst();
        final I input = edge.getSecond();
        @SuppressWarnings("assignment.type.incompatible") // we only use identifier for which procedures exists
        final @NonNull MealyMachine<S, I, ?, ?> subModel = subModels.get(identifier);

        properties.put(NodeAttrs.LABEL, input + " / " + subModel.getOutput(src.getSecond(), input));

        if (callAlphabet.containsSymbol(input)) {
            properties.put(EdgeAttrs.STYLE, EdgeStyles.BOLD);
        }

        return true;
    }
}
