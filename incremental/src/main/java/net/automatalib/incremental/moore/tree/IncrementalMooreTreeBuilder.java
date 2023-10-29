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
package net.automatalib.incremental.moore.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterators;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.moore.AbstractGraphView;
import net.automatalib.incremental.moore.IncrementalMooreBuilder;
import net.automatalib.ts.output.MooreTransitionSystem;
import net.automatalib.util.graph.traversal.GraphTraversal;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IncrementalMooreTreeBuilder<I, O> implements IncrementalMooreBuilder<I, O> {

    private final Alphabet<I> alphabet;
    private int alphabetSize;
    private @Nullable Node<O> root;

    public IncrementalMooreTreeBuilder(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.alphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.alphabet).addSymbol(symbol);
        }

        final int newAlphabetSize = this.alphabet.size();
        // even if the symbol was already in the alphabet, we need to make sure to be able to store the new symbol
        if (alphabetSize < newAlphabetSize && root != null) {
            ensureInputCapacity(root, alphabetSize, newAlphabetSize);
        }
        alphabetSize = newAlphabetSize;
    }

    private void ensureInputCapacity(Node<O> node, int oldAlphabetSize, int newAlphabetSize) {
        node.ensureInputCapacity(newAlphabetSize);
        for (int i = 0; i < oldAlphabetSize; i++) {
            final Node<O> child = node.getChild(i);
            if (child != null) {
                ensureInputCapacity(child, oldAlphabetSize, newAlphabetSize);
            }
        }
    }

    @Override
    public @Nullable Word<I> findSeparatingWord(MooreMachine<?, I, ?, O> target,
                                                Collection<? extends I> inputs,
                                                boolean omitUndefined) {
        return doFindSeparatingWord(target, inputs, omitUndefined);
    }

    protected <S, T> @Nullable Word<I> doFindSeparatingWord(MooreMachine<S, I, T, O> target,
                                                            Collection<? extends I> inputs,
                                                            boolean omitUndefined) {
        S init2 = target.getInitialState();

        if (root == null && init2 == null) {
            return null;
        } else if (root == null || init2 == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        if (!Objects.equals(root.getOutput(), target.getStateOutput(init2))) {
            return Word.epsilon();
        }

        // incomingInput can be null here, because we will always skip the bottom stack element below
        @SuppressWarnings("nullness")
        Record<@Nullable S, I, O> init = new Record<>(init2, root, null, inputs.iterator());

        Deque<Record<@Nullable S, I, O>> dfsStack = new ArrayDeque<>();
        dfsStack.push(init);

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull Record<@Nullable S, I, O> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            int inputIdx = alphabet.getSymbolIndex(input);

            Node<O> succ = rec.treeNode.getChild(inputIdx);
            if (succ == null) {
                continue;
            }

            @Nullable S state = rec.automatonState;
            @Nullable S automatonSucc = state == null ? null : target.getSuccessor(state, input);
            if (automatonSucc == null && omitUndefined) {
                continue;
            }

            if (automatonSucc == null || !Objects.equals(target.getStateOutput(automatonSucc), succ.getOutput())) {
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
    public boolean lookup(Word<? extends I> inputWord, List<? super O> output) {
        Node<O> curr = root;

        if (curr == null) {
            return false;
        }

        output.add(curr.getOutput());

        for (I sym : inputWord) {
            int symIdx = alphabet.getSymbolIndex(sym);
            Node<O> succ = curr.getChild(symIdx);
            if (succ == null) {
                return false;
            }
            output.add(succ.getOutput());
            curr = succ;
        }
        return true;
    }

    @Override
    public void insert(Word<? extends I> word, Word<? extends O> output) {
        assert word.size() + 1 == output.size();

        final Iterator<? extends O> outIter = output.iterator();
        final O rootOut = outIter.next();

        if (root == null) {
            root = new Node<>(rootOut);
        }

        Node<O> curr = root;
        for (I sym : word) {
            int inputIdx = alphabet.getSymbolIndex(sym);
            Node<O> succ = curr.getChild(inputIdx);
            if (succ == null) {
                succ = new Node<>(outIter.next());
                curr.setChild(inputIdx, alphabetSize, succ);
            } else if (!Objects.equals(succ.getOutput(), outIter.next())) {
                throw new ConflictException();
            }
            curr = succ;
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

    protected static final class Record<S, I, O> {

        public final S automatonState;
        public final Node<O> treeNode;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        public Record(S automatonState, Node<O> treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    public class GraphView extends AbstractGraphView<I, O, Node<O>, Edge<I, O>> {

        @Override
        public Collection<Node<O>> getNodes() {
            List<Node<O>> result = new ArrayList<>();
            Iterators.addAll(result, GraphTraversal.depthFirstIterator(this, Collections.singleton(root)));
            return result;
        }

        @Override
        public Collection<Edge<I, O>> getOutgoingEdges(Node<O> node) {
            List<Edge<I, O>> result = new ArrayList<>(alphabetSize);
            for (int i = 0; i < alphabetSize; i++) {
                Node<O> succ = node.getChild(i);
                if (succ != null) {
                    result.add(new Edge<>(alphabet.getSymbol(i), succ));
                }
            }
            return result;
        }

        @Override
        public Node<O> getTarget(Edge<I, O> edge) {
            return edge.getSucc();
        }

        @Override
        public I getInputSymbol(Edge<I, O> edge) {
            return edge.getInput();
        }

        @Override
        public O getOutputSymbol(Node<O> node) {
            return node.getOutput();
        }

        @Override
        public @Nullable Node<O> getInitialNode() {
            return root;
        }
    }

    public class TransitionSystemView implements MooreTransitionSystem<Node<O>, I, Edge<I, O>, O> {

        @Override
        public @Nullable Node<O> getInitialState() {
            return root;
        }

        @Override
        public O getStateOutput(Node<O> state) {
            return state.getOutput();
        }

        @Override
        public @Nullable Edge<I, O> getTransition(Node<O> state, I input) {
            final Node<O> child = state.getChild(alphabet.getSymbolIndex(input));
            return child == null ? null : new Edge<>(input, child);
        }

        @Override
        public Node<O> getSuccessor(Edge<I, O> transition) {
            return transition.getSucc();
        }
    }
}
