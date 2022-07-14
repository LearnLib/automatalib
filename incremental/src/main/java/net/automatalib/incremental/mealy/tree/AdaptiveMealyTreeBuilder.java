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
    private final Map<Node<O>, Pair<Word<? extends I>, Integer>> stateToQuery = new HashMap<>();
    private final PriorityQueue<Node<O>> queryStates = new PriorityQueue<>(
            (Node<O> a, Node<O> b) -> Integer.compare(stateToQuery.get(a).getSecond(),
                    stateToQuery.get(b).getSecond()));

    public AdaptiveMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    public void insert(Word<? extends I> input, Word<? extends O> outputWord) {
        this.insert(input, outputWord, 0);
    }

    public Boolean insert(Word<? extends I> input, Word<? extends O> outputWord, Integer queryIndex) {
        Boolean hasOverwritten = false;
        Node<O> curr = root;

        Iterator<? extends O> outputIt = outputWord.iterator();
        for (int i = 0; i < input.length(); i++) {
            I sym = input.getSymbol(i);
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

        assert curr != null;
        stateToQuery.put(curr, Pair.of(input, queryIndex));

        // Make sure it uses the new ages.
        queryStates.remove(curr);
        queryStates.add(curr);

        assert stateToQuery.size() == queryStates.size();

        return hasOverwritten;
    }

    private void removeQueries(Node<O> node) {
        GraphTraversal.bfIterator(this.asGraph(), Collections.singleton(node)).forEachRemaining(n -> {
            if (queryStates.contains(n)) {
                queryStates.remove(n);
            }
            stateToQuery.remove(n);
        });

        assert stateToQuery.size() == queryStates.size();
    }

    private void removeEdge(Node<O> node, I symbol) {
        node.setEdge(this.getInputAlphabet().getSymbolIndex(symbol), null);
    }

    public Word<? extends I> getOldestQuery() {
        return stateToQuery.get(queryStates.peek()).getFirst();
    }
}
