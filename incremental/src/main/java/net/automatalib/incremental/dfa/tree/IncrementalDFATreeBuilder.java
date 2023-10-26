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
package net.automatalib.incremental.dfa.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.visualization.helper.DelegateVisualizationHelper;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incrementally builds a tree, from a set of positive and negative words. Using {@link #insert(Word, boolean)}, either
 * the set of words definitely in the target language or definitely <i>not</i> in the target language is augmented. The
 * {@link #lookup(Word)} method then returns, for a given word, whether this word is in the set of definitely accepted
 * words ({@link Acceptance#TRUE}), definitely rejected words ({@link Acceptance#FALSE}), or neither ({@link
 * Acceptance#DONT_KNOW}).
 *
 * @param <I>
 *         input symbol class
 */
public class IncrementalDFATreeBuilder<I> extends AbstractIncrementalDFABuilder<I> {

    protected final Node root;

    public IncrementalDFATreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.root = new Node();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.inputAlphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = this.inputAlphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
            alphabetSize = newAlphabetSize;
        }
    }

    private void ensureInputCapacity(Node node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node child = node.getChild(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(DFA<?, I> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    protected <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                                         Collection<? extends I> inputs,
                                                         boolean omitUndefined) {
        S automatonInit = target.getInitialState();
        if (root.getAcceptance().conflicts(automatonInit != null && target.isAccepting(automatonInit))) {
            return Word.epsilon();
        }

        // incomingInput can be null here, because we will always skip the bottom stack element below
        @SuppressWarnings("nullness")
        Record<@Nullable S, I> init = new Record<>(automatonInit, root, null, inputs.iterator());

        Deque<Record<@Nullable S, I>> dfsStack = new ArrayDeque<>();
        dfsStack.push(init);

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull Record<@Nullable S, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            int inputIdx = inputAlphabet.getSymbolIndex(input);

            Node succ = rec.treeNode.getChild(inputIdx);
            if (succ == null) {
                continue;
            }

            @Nullable S state = rec.automatonState;
            @Nullable S automatonSucc = state == null ? null : target.getTransition(state, input);
            if (automatonSucc == null && omitUndefined) {
                continue;
            }

            boolean succAcc = automatonSucc != null && target.isAccepting(automatonSucc);

            if (succ.getAcceptance().conflicts(succAcc)) {
                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);

                dfsStack.pop();
                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            dfsStack.push(new Record<>(automatonSucc, succ, input, inputs.iterator()));
        }

        return null;
    }

    @Override
    public Acceptance lookup(Word<? extends I> inputWord) {
        Node curr = root;

        for (I sym : inputWord) {
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node succ = curr.getChild(symIdx);
            if (succ == null) {
                return Acceptance.DONT_KNOW;
            }
            curr = succ;
        }
        return curr.getAcceptance();
    }

    @Override
    public void insert(Word<? extends I> word, boolean acceptance) {
        Node curr = root;

        for (I sym : word) {
            int inputIdx = inputAlphabet.getSymbolIndex(sym);
            Node succ = curr.getChild(inputIdx);
            if (succ == null) {
                succ = new Node();
                curr.setChild(inputIdx, alphabetSize, succ);
            }
            curr = succ;
        }

        Acceptance acc = curr.getAcceptance();
        Acceptance newWordAcc = Acceptance.fromBoolean(acceptance);
        if (acc == Acceptance.DONT_KNOW) {
            curr.setAcceptance(newWordAcc);
        } else if (acc != newWordAcc) {
            throw new ConflictException(
                    "Conflicting acceptance values for word " + word + ": " + acc + " vs " + newWordAcc);
        }
    }

    @Override
    public GraphView asGraph() {
        return new GraphView();
    }

    @Override
    public TransitionSystemView asTransitionSystem() {
        return new TransitionSystemView();
    }

    protected static final class Record<S, I> {

        public final S automatonState;
        public final Node treeNode;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        public Record(S automatonState, Node treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    public class GraphView extends AbstractGraphView<I, Node, Edge<I>> {

        @Override
        public Collection<Node> getNodes() {
            List<Node> result = new ArrayList<>();
            Iterators.addAll(result, GraphTraversal.depthFirstIterator(this, Collections.singleton(root)));
            return result;
        }

        @Override
        public Collection<Edge<I>> getOutgoingEdges(Node node) {
            List<Edge<I>> result = new ArrayList<>(alphabetSize);
            for (int i = 0; i < alphabetSize; i++) {
                Node succ = node.getChild(i);
                if (succ != null) {
                    result.add(new Edge<>(succ, inputAlphabet.getSymbol(i)));
                }
            }
            return result;
        }

        @Override
        public Node getTarget(Edge<I> edge) {
            return edge.getNode();
        }

        @Override
        public I getInputSymbol(Edge<I> edge) {
            return edge.getInput();
        }

        @Override
        public Acceptance getAcceptance(Node node) {
            return node.getAcceptance();
        }

        @Override
        public Node getInitialNode() {
            return root;
        }

        @Override
        public VisualizationHelper<Node, Edge<I>> getVisualizationHelper() {
            return new DelegateVisualizationHelper<Node, Edge<I>>(super.getVisualizationHelper()) {

                private int id;

                @Override
                public boolean getNodeProperties(Node node, Map<String, String> properties) {
                    if (!super.getNodeProperties(node, properties)) {
                        return false;
                    }
                    properties.put(NodeAttrs.LABEL, "n" + (id++));
                    return true;
                }
            };
        }
    }

    public class TransitionSystemView extends AbstractTransitionSystemView<Node, I, Node> {

        @Override
        public Node getSuccessor(Node transition) {
            return transition;
        }

        @Override
        public @Nullable Node getTransition(Node state, I input) {
            int inputIdx = inputAlphabet.getSymbolIndex(input);
            return state.getChild(inputIdx);
        }

        @Override
        public Node getInitialState() {
            return root;
        }

        @Override
        public Acceptance getStateProperty(Node state) {
            return state.getAcceptance();
        }
    }

}
