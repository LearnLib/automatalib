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
package net.automatalib.graph.ads;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.automatalib.graph.Graph;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An interface representing a node in an adaptive distinguishing sequence (which essentially forms a decision tree).
 * <p>
 * For convenience, this interface extends the {@link Graph} interface so that an ADS may be passed easily to the
 * {@link Visualization} factory.
 * <p>
 * This is a utility interface with a recursive generic type parameter to allow for better inheritance with this
 * recursive data structure. Algorithms may use more simplified sub-interfaces such as {@link ADSNode}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 * @param <N>
 *         the concrete node type
 */
public interface RecursiveADSNode<S, I, O, N extends RecursiveADSNode<S, I, O, N>> extends Graph<N, N> {

    /**
     * Returns the input symbol associated with this ADS node.
     *
     * @return the associated input symbol
     *
     * @throws UnsupportedOperationException
     *         if trying to get an input symbol from a leaf node (see {@link #isLeaf()}).
     */
    I getSymbol();

    /**
     * Sets the input symbol associated with this ADS node.
     *
     * @param symbol
     *         the input symbol to be associated with this ADS node.
     *
     * @throws UnsupportedOperationException
     *         if trying to set an input symbol on a leaf node (see {@link #isLeaf()}).
     */
    void setSymbol(I symbol);

    /**
     * Returns the parent node of this node.
     *
     * @return The parent node of this ADS node. May be {@code null}, if this node is the root node of an ADS.
     */
    @Nullable N getParent();

    /**
     * Sets the parent node of this node.
     *
     * @param parent
     *         the parent node to set
     */
    void setParent(N parent);

    /**
     * A utility method to collect all nodes of a subtree specified by the given root node. May be used for the
     * {@link Graph#getNodes()} implementation where a concrete type for {@link N} is needed.
     *
     * @param root
     *         the node for which all subtree nodes should be collected
     *
     * @return all nodes in the specified subtree, including the root node itself
     */
    default Collection<N> getNodesForRoot(N root) {
        final List<N> result = new ArrayList<>();
        final Queue<N> queue = new ArrayDeque<>();

        queue.add(root);

        // level-order iteration of the tree nodes
        while (!queue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            final @NonNull N node = queue.poll();
            result.add(node);
            queue.addAll(node.getChildren().values());
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a mapping to the child nodes of this ADS node.
     *
     * @return A mapping from hypothesis outputs to child ADS nodes. May be empty/unmodifiable for leaf nodes.
     */
    Map<O, N> getChildren();

    @Override
    default Collection<N> getOutgoingEdges(N node) {
        return Collections.unmodifiableCollection(node.getChildren().values());
    }

    @Override
    default N getTarget(N edge) {
        return edge;
    }

    // default methods for graph interface

    @Override
    default VisualizationHelper<N, N> getVisualizationHelper() {
        return new VisualizationHelper<N, N>() {

            @Override
            public boolean getNodeProperties(N node, Map<String, String> properties) {
                if (node.isLeaf()) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
                    properties.put(NodeAttrs.LABEL, String.valueOf(node.getState()));
                } else {
                    properties.put(NodeAttrs.LABEL, node.toString());
                    properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(N src, N edge, N tgt, Map<String, String> properties) {

                for (Map.Entry<O, N> e : src.getChildren().entrySet()) {
                    if (e.getValue().equals(tgt)) {
                        properties.put(EdgeAttrs.LABEL, String.valueOf(e.getKey()));
                        return true;
                    }
                }
                return true;
            }
        };
    }

    /**
     * A utility method indicating whether this node represents a leaf of an ADS (and therefore referencing an automaton
     * state) or an inner node (and therefore referencing an input symbol).
     *
     * @return {@code true} if this node is a leaf of an ADS, {@code false} otherwise.
     */
    boolean isLeaf();

    /**
     * Returns the automaton state associated with this ADS node.
     *
     * @return the associated state
     *
     * @throws UnsupportedOperationException
     *         if trying to get a state from a symbol node (see {@link #isLeaf()}).
     */
    S getState();

    /**
     * Sets the automaton state associated with this ADS node.
     *
     * @param state
     *         the state to be associated with this ADS node.
     *
     * @throws UnsupportedOperationException
     *         if trying to set a state on a symbol node (see {@link #isLeaf()}).
     */
    void setState(S state);
}
