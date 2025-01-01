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
package net.automatalib.incremental.mealy.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A variation of the normal {@link IncrementalMealyTreeBuilder} which stores the successor information of each
 * tree-node in a dynamically allocated {@link Map}.
 * <p>
 * In a dense tree-structure this may result in higher memory consumption than the regular tree. However, if only sparse
 * information are stored, the overall consumption may be lower. Also, allows to skip the initial alphabet definition as
 * symbol information can be stored <i>dynamically</i> and may us
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class DynamicIncrementalMealyTreeBuilder<I, O> extends AbstractMealyTreeBuilder<DynamicNode<I, O>, I, O>
        implements IncrementalMealyBuilder<I, O> {

    public DynamicIncrementalMealyTreeBuilder() {
        super(new DynamicNode<>());
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends O> outputWord) {
        DynamicNode<I, O> curr = root;

        for (int i = 0; i < input.length(); i++) {
            I sym = input.getSymbol(i);
            O out = outputWord.getSymbol(i);
            Edge<DynamicNode<I, O>, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    throw new ConflictException();
                }
                curr = edge.getTarget();
            }
        }
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        // we do not need to do anything, because this implementation can handle arbitrarily sized alphabets
    }

    @Override
    @Nullable Edge<DynamicNode<I, O>, O> getEdge(DynamicNode<I, O> node, I symbol) {
        return node.getEdge(symbol);
    }

    @Override
    DynamicNode<I, O> createNode() {
        return new DynamicNode<>();
    }

    @Override
    DynamicNode<I, O> insertNode(DynamicNode<I, O> parent, I symbols, O output) {
        DynamicNode<I, O> succ = createNode();
        Edge<DynamicNode<I, O>, O> edge = new Edge<>(output, succ);
        parent.setEdge(symbols, edge);
        return succ;
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new GraphView();
    }

    private final class GraphView implements Graph<DynamicNode<I, O>, Entry<I, Edge<DynamicNode<I, O>, O>>> {

        @Override
        public Collection<Entry<I, Edge<DynamicNode<I, O>, O>>> getOutgoingEdges(DynamicNode<I, O> node) {
            return node.getOutEdges().entrySet();
        }

        @Override
        public DynamicNode<I, O> getTarget(Entry<I, Edge<DynamicNode<I, O>, O>> edge) {
            return edge.getValue().getTarget();
        }

        @Override
        public Collection<DynamicNode<I, O>> getNodes() {
            return IteratorUtil.list(GraphTraversal.breadthFirstIterator(this, Collections.singleton(root)));
        }

        @Override
        public VisualizationHelper<DynamicNode<I, O>, Entry<I, Edge<DynamicNode<I, O>, O>>> getVisualizationHelper() {
            return new DefaultVisualizationHelper<DynamicNode<I, O>, Entry<I, Edge<DynamicNode<I, O>, O>>>() {

                private int id;

                @Override
                public boolean getNodeProperties(DynamicNode<I, O> node, Map<String, String> properties) {
                    super.getNodeProperties(node, properties);

                    properties.put(NodeAttrs.LABEL, "n" + (id++));

                    return true;
                }

                @Override
                public boolean getEdgeProperties(DynamicNode<I, O> src,
                                                 Entry<I, Edge<DynamicNode<I, O>, O>> edge,
                                                 DynamicNode<I, O> tgt,
                                                 Map<String, String> properties) {
                    super.getEdgeProperties(src, edge, tgt, properties);
                    properties.put(EdgeAttrs.LABEL, edge.getKey() + " / " + edge.getValue().getOutput());
                    return true;
                }

                @Override
                protected Collection<DynamicNode<I, O>> initialNodes() {
                    return Collections.singleton(root);
                }
            };
        }
    }
}
