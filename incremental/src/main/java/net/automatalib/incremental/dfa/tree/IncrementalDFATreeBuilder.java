/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.incremental.dfa.tree;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
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
import net.automatalib.incremental.util.DFSepWordFinder;
import net.automatalib.incremental.util.DFSepWordFinder.Visitor;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
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

        final Visitor<Node, S, Node, S> visitor = new Visitor<>() {

            @Override
            public boolean initConflict(Node incInit, S targetInit) {
                return incInit.getAcceptance().conflicts(target.isAccepting(targetInit));
            }

            @Override
            public boolean succConflict(Node incTrans, @Nullable S targetTrans) {
                return incTrans.getAcceptance().conflicts(targetTrans != null && target.isAccepting(targetTrans));
            }
        };

        return DFSepWordFinder.findSeparatingWord(new AutomatonView(), target, inputs, omitUndefined, visitor);
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
    public DFA<?, I> asTransitionSystem() {
        return new AutomatonView();
    }

    @Override
    public Graph<?, ?> asGraph() {
        return new UniversalAutomatonGraphView<>(new AutomatonView(), inputAlphabet) {

            @Override
            public VisualizationHelper<Node, TransitionEdge<I, Node>> getVisualizationHelper() {
                return new AbstractVisualizationHelper<>(automaton) {

                    @Override
                    protected Acceptance getAcceptance(Node node) {
                        return node.getAcceptance();
                    }
                };
            }
        };
    }

    class AutomatonView implements DFA<Node, I> {

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
        public boolean isAccepting(Node state) {
            return state.getAcceptance() == Acceptance.TRUE;
        }

        @Override
        public Collection<Node> getStates() {
            return IteratorUtil.list(TSTraversal.breadthFirstIterator(this, inputAlphabet));
        }

        /*
         * We need to override the default DFA mapping, because its StateIDStaticMapping class requires our
         * stateIDs, which requires our states, which requires our stateIDs, which requires ... infinite loop!
         */
        @Override
        public <V> MutableMapping<Node, V> createStaticStateMapping() {
            return new MapMapping<>();
        }
    }

}
