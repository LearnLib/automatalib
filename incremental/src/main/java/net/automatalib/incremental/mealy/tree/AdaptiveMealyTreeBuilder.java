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
package net.automatalib.incremental.mealy.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import net.automatalib.commons.util.Pair;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class AdaptiveMealyTreeBuilder<I, O> extends IncrementalMealyTreeBuilder<I, O> {
    private final Map<Node<O>, Pair<Word<I>, Integer>> stateToQuery = new HashMap<>();
    private final PriorityQueue<Node<O>> queryStates = new PriorityQueue<>(
            (Node<O> a, Node<O> b) -> Integer.compare(stateToQuery.get(a).getSecond(),
                    stateToQuery.get(b).getSecond()));

    public AdaptiveMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends O> outputWord) {
        throw new UnsupportedOperationException("Use insert: (Word<I>, Word<O>, Intger) -> Boolean");
    }

    public Boolean insert(Word<I> input, Word<O> outputWord, Integer queryIndex) {
        Boolean hasOverwritten = false;
        Node<O> curr = root;

        Iterator<? extends O> outputIt = outputWord.iterator();
        for (I sym : input) {
            O out = outputIt.next();
            Edge<Node<O>, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    removeQueries(edge.getTarget());
                    removeEdge(curr, sym);
                    curr = insertNode(curr, sym, out);
                    hasOverwritten = true;
                } else {
                    curr = edge.getTarget();
                }
            }
        }

        stateToQuery.put(curr, Pair.of(input, queryIndex));
        queryStates.add(curr);
        return hasOverwritten;
    }

    private void removeQueries(Node<O> node) {
        GraphTraversal.bfIterator(this.asGraph(), Collections.singleton(node)).forEachRemaining(n -> {
            queryStates.remove(n);
            stateToQuery.remove(n);
        });
    }

    private void removeEdge(Node<O> node, I symbol) {
        node.setEdge(inputAlphabet.getSymbolIndex(symbol), null);
    }

    public Word<I> getOldestQuery() {
        return stateToQuery.get(queryStates.peek()).getFirst();
    }
}
