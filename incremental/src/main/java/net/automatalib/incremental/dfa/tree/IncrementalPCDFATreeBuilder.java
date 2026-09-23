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
import net.automatalib.graph.Graph;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.AbstractVisualizationHelper;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.util.DFSepWordFinder;
import net.automatalib.incremental.util.DFSepWordFinder.Visitor;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The prefix-closed version of {@link IncrementalDFATreeBuilder}. Contrary to the regular lookup semantics, where an
 * exact response to a lookup can only be given, if the exact word has been observed before, the prefix-closed semantics
 * behave as follows:
 *
 * <ul>
 * <li>prefixes of previously observed accepted words will result in a {@link Acceptance#TRUE} response as well.</li>
 * <li>continuations of previously observed rejected words will result in a {@link Acceptance#FALSE} response as
 * well.</li>
 * </ul>
 *
 * @param <I>
 *         input symbol class
 */
public class IncrementalPCDFATreeBuilder<I> extends IncrementalDFATreeBuilder<I> {

    private @Nullable Node sink;

    public IncrementalPCDFATreeBuilder(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
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

            @Override
            public boolean continueExploration(Node incSucc, @Nullable S targetSucc) {
                // we break the DAG contract by making rejecting nodes reflexive
                // thus we need to explicitly terminate exploration
                return incSucc.getAcceptance() != Acceptance.FALSE || targetSucc != null;
            }
        };

        return DFSepWordFinder.findSeparatingWord(new AutomatonView(), target, inputs, omitUndefined, visitor);
    }

    @Override
    public Acceptance lookup(Word<? extends I> inputWord) {
        Node curr = root;

        for (I sym : inputWord) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                return Acceptance.FALSE;
            }

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
        if (acceptance) {
            insertTrue(word);
        } else {
            insertFalse(word);
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

    private void insertTrue(Word<? extends I> word) {
        Node curr = root;

        int idx = 0;
        for (I sym : word) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                throw new ConflictException("Conflicting acceptance values for word " + word.prefix(idx) +
                                            ": found FALSE, expected DONT_KNOW or TRUE");
            }
            curr.setAcceptance(Acceptance.TRUE);
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node succ = curr.getChild(symIdx);
            if (succ == null) {
                succ = new Node(Acceptance.TRUE);
                curr.setChild(symIdx, alphabetSize, succ);
            }
            curr = succ;
            idx++;
        }
        if (curr.getAcceptance() == Acceptance.FALSE) {
            throw new ConflictException(
                    "Conflicting acceptance values for word " + word + ": found FALSE, expected DONT_KNOW or TRUE");
        }
        curr.setAcceptance(Acceptance.TRUE);
    }

    private void insertFalse(Word<? extends I> word) {
        Node curr = root;
        Node prev = null;
        int lastSymIdx = -1;

        for (I sym : word) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                return; // done!
            }
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node succ = curr.getChild(symIdx);
            if (succ == null) {
                succ = new Node(Acceptance.DONT_KNOW);
                curr.setChild(symIdx, alphabetSize, succ);
            }
            prev = curr;
            curr = succ;
            lastSymIdx = symIdx;
        }

        if (curr.getAcceptance() == Acceptance.TRUE) {
            throw new ConflictException(
                    "Conflicting acceptance values for word " + word + ": found TRUE, expected DONT_KNOW or FALSE");
        }

        // Note that we do not need to look deeper into the tree, because
        // if any of the successor of curr would have an acceptance value
        // of true, also curr would
        if (prev == null) {
            root.makeSink();
        } else {
            Node sink = getSink();
            prev.setChild(lastSymIdx, alphabetSize, sink);
        }
    }

    private Node getSink() {
        if (sink == null) {
            sink = new Node(Acceptance.FALSE);
        }
        return sink;
    }

    private final class AutomatonView extends IncrementalDFATreeBuilder<I>.AutomatonView {

        @Override
        public @Nullable Node getTransition(Node state, I input) {
            if (state.getAcceptance() == Acceptance.FALSE) {
                return state;
            }
            return super.getTransition(state, input);
        }
    }

}
