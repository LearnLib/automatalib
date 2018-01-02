/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.util.tries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.graphs.Graph;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

public class SuffixTrie<I> implements Graph<SuffixTrieNode<I>, SuffixTrieNode<I>> {

    public static final boolean DEFAULT_GRAPH_REPRESENTABLE = true;

    protected final SuffixTrieNode<I> root;
    protected final List<SuffixTrieNode<I>> nodes;

    /**
     * Constructor. Constructs a graph-representable suffix trie.
     */
    public SuffixTrie() {
        this(DEFAULT_GRAPH_REPRESENTABLE);
    }

    /**
     * Constructor. Constructs a suffix trie.
     *
     * @param graphRepresentable
     *         whether the trie should be graph representable.
     */
    public SuffixTrie(boolean graphRepresentable) {
        this(graphRepresentable, new SuffixTrieNode<>());
    }

    /**
     * Internal constructor. Allows to override the root node and graph representability.
     *
     * @param graphRepresentable
     *         whether the trie should be graph representable.
     * @param root
     *         the root node.
     */
    protected SuffixTrie(boolean graphRepresentable, SuffixTrieNode<I> root) {
        this.root = root;
        if (graphRepresentable) {
            this.nodes = new ArrayList<>();
            this.nodes.add(root);
        } else {
            this.nodes = null;
        }
    }

    /**
     * Internal constructor. Allows to override the root node.
     *
     * @param root
     *         the root node.
     */
    protected SuffixTrie(SuffixTrieNode<I> root) {
        this(DEFAULT_GRAPH_REPRESENTABLE, root);
    }

    @Override
    public Collection<SuffixTrieNode<I>> getNodes() {
        if (nodes == null) {
            throw new UnsupportedOperationException("This trie is not graph representable");
        }

        return Collections.unmodifiableCollection(nodes);
    }

    @Override
    public Collection<SuffixTrieNode<I>> getOutgoingEdges(SuffixTrieNode<I> node) {
        if (nodes == null) {
            throw new UnsupportedOperationException("This trie is not graph representable");
        }

        SuffixTrieNode<I> parent = node.getParent();
        if (parent == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(parent);
    }

    @Override
    public SuffixTrieNode<I> getTarget(SuffixTrieNode<I> edge) {
        if (nodes == null) {
            throw new UnsupportedOperationException("This trie is not graph representable");
        }

        return edge;
    }

    @Override
    public VisualizationHelper<SuffixTrieNode<I>, SuffixTrieNode<I>> getVisualizationHelper() {
        if (nodes == null) {
            throw new UnsupportedOperationException("This trie is not graph representable");
        }

        return new DefaultVisualizationHelper<SuffixTrieNode<I>, SuffixTrieNode<I>>() {

            @Override
            public boolean getNodeProperties(SuffixTrieNode<I> node, Map<String, String> properties) {
                if (!super.getNodeProperties(node, properties)) {
                    return false;
                }
                String lbl = node.isRoot() ? "ε" : String.valueOf(node.getSymbol());
                properties.put(NodeAttrs.LABEL, lbl);
                return true;
            }

        };
    }

    /**
     * Adds a word to the trie.
     *
     * @param symbol
     *         the first symbol of the word.
     * @param parent
     *         the remaining suffix of the word.
     *
     * @return a trie node corresponding to the inserted word.
     */
    public SuffixTrieNode<I> add(I symbol, SuffixTrieNode<I> parent) {
        SuffixTrieNode<I> n = new SuffixTrieNode<>(symbol, parent);
        if (nodes != null) {
            nodes.add(n);
        }
        return n;
    }

    /**
     * Returns the root of this trie.
     *
     * @return the root of this trie.
     */
    public SuffixTrieNode<I> getRoot() {
        return root;
    }

}
