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
package net.automatalib.incremental.moore.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.MooreMachine.MooreGraphView;
import net.automatalib.automaton.visualization.MooreVisualizationHelper;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.moore.IncrementalMooreBuilder;
import net.automatalib.ts.output.MooreTransitionSystem;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incrementally builds a (tree-based) Moore machine from a set of input and corresponding output words.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
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
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
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

    private <S, T> @Nullable Word<I> doFindSeparatingWord(MooreMachine<S, I, T, O> target,
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
    public MooreTransitionSystem<?, I, ?, O> asTransitionSystem() {
        return new TransitionSystemView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new MooreGraphView<Node<O>, I, Node<O>, O, TransitionSystemView>(new TransitionSystemView(), alphabet) {

            @Override
            public VisualizationHelper<Node<O>, TransitionEdge<I, Node<O>>> getVisualizationHelper() {
                return new MooreVisualizationHelper<Node<O>, I, Node<O>, O>(automaton) {

                    @Override
                    public boolean getNodeProperties(Node<O> node, Map<String, String> properties) {
                        super.getNodeProperties(node, properties);
                        properties.put(NodeAttrs.LABEL, String.valueOf(node.getOutput()));
                        return true;
                    }
                };
            }
        };
    }

    private static final class Record<S, I, O> {

        public final S automatonState;
        public final Node<O> treeNode;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        Record(S automatonState, Node<O> treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    private class TransitionSystemView implements MooreMachine<Node<O>, I, Node<O>, O> {

        @Override
        public @Nullable Node<O> getInitialState() {
            return root;
        }

        @Override
        public O getStateOutput(Node<O> state) {
            return state.getOutput();
        }

        @Override
        public @Nullable Node<O> getTransition(Node<O> state, I input) {
            return state.getChild(alphabet.getSymbolIndex(input));
        }

        @Override
        public Node<O> getSuccessor(Node<O> transition) {
            return transition;
        }

        @Override
        public Collection<Node<O>> getStates() {
            return IteratorUtil.list(TSTraversal.breadthFirstIterator(this, alphabet));
        }

        /*
         * We need to override the default MooreMachine mapping, because its StateIDStaticMapping class requires our
         * nodeIDs, which requires our states, which requires our nodeIDs, which requires ... infinite loop!
         */
        @Override
        public <V> MutableMapping<Node<O>, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }
}
