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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.commons.util.Pair;
import net.automatalib.incremental.mealy.AdaptiveMealyBuilder;
import net.automatalib.util.graphs.traversal.GraphTraversal;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class AdaptiveMealyTreeBuilder<I, O> extends AbstractMealyTreeBuilder<Node<O>, I, O>
        implements AdaptiveMealyBuilder<I, O>, InputAlphabetHolder<I> {

    private final Map<Node<O>, Pair<Word<? extends I>, Integer>> stateToQuery = new HashMap<>();
    private final PriorityQueue<Node<O>> queryStates = new PriorityQueue<>(
            (Node<O> a, Node<O> b) -> Integer.compare(stateToQuery.get(a).getSecond(),
                    stateToQuery.get(b).getSecond()));
    private Integer insertCounter = 0;

    private final Alphabet<I> inputAlphabet;
    private int alphabetSize;

    public AdaptiveMealyTreeBuilder(Alphabet<I> inputAlphabet) {
        super(new Node<>(inputAlphabet.size()));
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(inputAlphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
            alphabetSize = newAlphabetSize;
        }
    }

    private void ensureInputCapacity(Node<O> node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node<O> child = node.getSuccessor(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    protected Edge<Node<O>, O> getEdge(Node<O> node, I symbol) {
        return node.getEdge(inputAlphabet.getSymbolIndex(symbol));
    }

    @Override
    protected Node<O> createNode() {
        return new Node<>(alphabetSize);
    }

    @Override
    protected Node<O> insertNode(Node<O> parent, I symIdx, O output) {
        Node<O> succ = createNode();
        Edge<Node<O>, O> edge = new Edge<>(output, succ);
        parent.setEdge(inputAlphabet.getSymbolIndex(symIdx), edge);
        return succ;
    }

    @Override
    protected Collection<AnnotatedEdge<Node<O>, I, O>> getOutgoingEdges(Node<O> node) {
        List<AnnotatedEdge<Node<O>, I, O>> result = new ArrayList<>(alphabetSize);
        for (int i = 0; i < alphabetSize; i++) {
            Edge<Node<O>, O> edge = node.getEdge(i);
            if (edge != null) {
                result.add(new AnnotatedEdge<>(edge, inputAlphabet.getSymbol(i)));
            }
        }
        return result;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    public boolean insert(Word<? extends I> input, Word<? extends O> outputWord) {
        Node<O> curr = root;
        boolean hasOverwritten = false;

        Iterator<? extends O> outputIt = outputWord.iterator();
        for (int i = 0; i < input.length(); i++) {
            I sym = input.getSymbol(i);
            O out = outputIt.next();
            Edge<Node<O>, O> edge = getEdge(curr, sym);
            if (edge == null) {
                curr = insertNode(curr, sym, out);
            } else {
                if (!Objects.equals(out, edge.getOutput())) {
                    hasOverwritten = true;
                    removeQueries(edge.getTarget());
                    removeEdge(curr, sym);
                    curr = insertNode(curr, sym, out);
                } else {
                    curr = edge.getTarget();
                }
            }
        }

        assert curr != null;
        stateToQuery.put(curr, Pair.of(input, insertCounter));
        insertCounter += 1;

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
