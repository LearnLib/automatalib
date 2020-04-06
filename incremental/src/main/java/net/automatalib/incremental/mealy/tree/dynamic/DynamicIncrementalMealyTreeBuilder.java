/* Copyright (C) 2013-2020 TU Dortmund
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
package net.automatalib.incremental.mealy.tree.dynamic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.incremental.mealy.tree.AbstractIncrementalMealyTreeBuilder;
import net.automatalib.incremental.mealy.tree.AnnotatedEdge;
import net.automatalib.incremental.mealy.tree.Edge;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A variation of the normal {@link IncrementalMealyTreeBuilder}, which stores the successor information of each
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
 *
 * @author frohme
 */
public class DynamicIncrementalMealyTreeBuilder<I, O> extends AbstractIncrementalMealyTreeBuilder<Node<I, O>, I, O> {

    public DynamicIncrementalMealyTreeBuilder() {
        this(new Node<>());
    }

    DynamicIncrementalMealyTreeBuilder(Node<I, O> root) {
        super(root);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        // we do not need to do anything, because this implementation can handle arbitrarily seized alphabets
    }

    @Override
    protected @Nullable Edge<Node<I, O>, O> getEdge(Node<I, O> node, I symbol) {
        return node.getEdge(symbol);
    }

    @Override
    protected Node<I, O> createNode() {
        return new Node<>();
    }

    @Override
    protected Node<I, O> insertNode(Node<I, O> parent, I symbols, O output) {
        Node<I, O> succ = createNode();
        Edge<Node<I, O>, O> edge = new Edge<>(output, succ);
        parent.setEdge(symbols, edge);
        return succ;
    }

    @Override
    protected Collection<AnnotatedEdge<Node<I, O>, I, O>> getOutgoingEdges(Node<I, O> node) {

        final Map<I, @Nullable Edge<Node<I, O>, O>> outEdges = node.getOutEdges();
        final List<AnnotatedEdge<Node<I, O>, I, O>> result = new ArrayList<>(outEdges.size());

        for (Map.Entry<I, @Nullable Edge<Node<I, O>, O>> e : outEdges.entrySet()) {
            if (e.getValue() != null) {
                result.add(new AnnotatedEdge<>(e.getValue(), e.getKey()));
            }
        }

        return result;
    }
}
