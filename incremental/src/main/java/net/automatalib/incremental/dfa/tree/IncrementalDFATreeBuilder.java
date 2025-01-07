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
package net.automatalib.incremental.dfa.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder;
import net.automatalib.incremental.dfa.AbstractVisualizationHelper;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incrementally builds a tree, from a set of positive and negative words. Using {@link #insert(Word, boolean)}, either
 * the set of words definitely in the target language or definitely <i>not</i> in the target language is augmented. The
 * {@link #lookup(Word)} method then returns, for a given word, whether this word is in the set of definitely accepted
 * words ({@link Acceptance#TRUE}), definitely rejected words ({@link Acceptance#FALSE}), or neither
 * ({@link Acceptance#DONT_KNOW}).
 *
 * @param <I>
 *         input symbol class
 */
public class IncrementalDFATreeBuilder<I> extends AbstractIncrementalDFABuilder<I> {

    final Node root;

    public IncrementalDFATreeBuilder(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
        this.root = new Node();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        final int oldSize = alphabetSize;
        super.addAlphabetSymbol(symbol);
        final int newSize = alphabetSize;

        if (oldSize < newSize) {
            ensureInputCapacity(root, oldSize, newSize);
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

    <S> @Nullable Word<I> doFindSeparatingWord(DFA<S, I> target,
                                               Collection<? extends I> inputs,
                                               boolean omitUndefined) {
        S automatonInit = target.getInitialState();

        if (automatonInit == null) {
            return omitUndefined ? null : Word.epsilon();
        }

        if (root.getAcceptance().conflicts(target.isAccepting(automatonInit))) {
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
    public UniversalDTS<?, I, ?, Acceptance, Void> asTransitionSystem() {
        return new TransitionSystemView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new UniversalAutomatonGraphView<Node, I, Node, Acceptance, Void, TransitionSystemView>(new TransitionSystemView(),
                                                                                                      inputAlphabet) {

            @Override
            public VisualizationHelper<Node, TransitionEdge<I, Node>> getVisualizationHelper() {
                return new AbstractVisualizationHelper<Node, I, Node, TransitionSystemView>(automaton) {

                    @Override
                    public Acceptance getAcceptance(Node node) {
                        return node.getAcceptance();
                    }
                };
            }
        };
    }

    static final class Record<S, I> {

        public final S automatonState;
        public final Node treeNode;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        Record(S automatonState, Node treeNode, I incomingInput, Iterator<? extends I> inputIt) {
            this.automatonState = automatonState;
            this.treeNode = treeNode;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    class TransitionSystemView implements UniversalDTS<Node, I, Node, Acceptance, Void>,
                                          UniversalAutomaton<Node, I, Node, Acceptance, Void> {

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

        @Override
        public Void getTransitionProperty(Node transition) {
            return null;
        }

        @Override
        public Collection<Node> getStates() {
            return IteratorUtil.list(TSTraversal.breadthFirstIterator(this, inputAlphabet));
        }

        /*
         * We need to override the default MooreMachine mapping, because its StateIDStaticMapping class requires our
         * nodeIDs, which requires our states, which requires our nodeIDs, which requires ... infinite loop!
         */
        @Override
        public <@Nullable V> MutableMapping<Node, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }

}
