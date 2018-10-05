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
package net.automatalib.incremental.dfa.tree;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

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
 *
 * @author Malte Isberner
 */
public class IncrementalPCDFATreeBuilder<I> extends IncrementalDFATreeBuilder<I> {

    private Node<I> sink;

    public IncrementalPCDFATreeBuilder(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    protected <S> Word<I> doFindSeparatingWord(final DFA<S, I> target,
                                               Collection<? extends I> inputs,
                                               boolean omitUndefined) {

        S automatonInit = target.getInitialState();
        Acceptance rootAcc = root.getAcceptance();
        if (rootAcc.conflicts(target.isAccepting(automatonInit))) {
            return Word.epsilon();
        }
        if (rootAcc == Acceptance.FALSE) {
            return findLive(target, automatonInit, inputs, target.createStaticStateMapping());
        }

        Deque<Record<S, I>> dfsStack = new ArrayDeque<>();
        dfsStack.push(new Record<>(automatonInit, root, null, inputs.iterator()));

        MutableMapping<S, Boolean> deadStates = null;

        while (!dfsStack.isEmpty()) {
            Record<S, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();
            int inputIdx = inputAlphabet.getSymbolIndex(input);

            Node<I> succ = rec.treeNode.getChild(inputIdx);
            if (succ == null) {
                continue;
            }

            Acceptance acc = succ.getAcceptance();

            S automatonSucc = (rec.automatonState == null) ? null : target.getTransition(rec.automatonState, input);
            if (automatonSucc == null && (omitUndefined || acc == Acceptance.FALSE)) {
                continue;
            }

            boolean succAcc = (automatonSucc != null) && target.isAccepting(automatonSucc);

            Word<I> liveSuffix = null;
            if (acc == Acceptance.FALSE) {
                if (deadStates == null) {
                    deadStates = target.createStaticStateMapping();
                }
                liveSuffix = findLive(target, automatonSucc, inputs, deadStates);
            }

            if (acc.conflicts(succAcc) || (liveSuffix != null)) {
                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);

                dfsStack.pop();
                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                wb.reverse();
                if (liveSuffix != null) {
                    wb.append(liveSuffix);
                }
                return wb.toWord();
            }

            dfsStack.push(new Record<>(automatonSucc, succ, input, inputs.iterator()));
        }

        return null;
    }

    @Override
    public Acceptance lookup(Word<? extends I> inputWord) {
        Node<I> curr = root;

        for (I sym : inputWord) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                return Acceptance.FALSE;
            }

            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node<I> succ = curr.getChild(symIdx);
            if (succ == null) {
                return Acceptance.DONT_KNOW;
            }
            curr = succ;
        }
        return curr.getAcceptance();
    }

    @Override
    public void insert(Word<? extends I> word, boolean acceptance) throws ConflictException {
        if (acceptance) {
            insertTrue(word);
        } else {
            insertFalse(word);
        }
    }

    @Override
    public TransitionSystemView asTransitionSystem() {
        return new TransitionSystemView();
    }

    private void insertTrue(Word<? extends I> word) throws ConflictException {
        Node<I> curr = root;

        int idx = 0;
        for (I sym : word) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                throw new ConflictException("Conflicting acceptance values for word " + word.prefix(idx) +
                                            ": found FALSE, expected DONT_KNOW or TRUE");
            }
            curr.setAcceptance(Acceptance.TRUE);
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node<I> succ = curr.getChild(symIdx);
            if (succ == null) {
                succ = new Node<>(Acceptance.TRUE);
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

    private void insertFalse(Word<? extends I> word) throws ConflictException {
        Node<I> curr = root;
        Node<I> prev = null;
        int lastSymIdx = -1;

        for (I sym : word) {
            if (curr.getAcceptance() == Acceptance.FALSE) {
                return; // done!
            }
            int symIdx = inputAlphabet.getSymbolIndex(sym);
            Node<I> succ = curr.getChild(symIdx);
            if (succ == null) {
                succ = new Node<>(Acceptance.DONT_KNOW);
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
            assert curr == root;
            root.makeSink();
        } else {
            Node<I> sink = getSink();
            prev.setChild(lastSymIdx, alphabetSize, sink);
        }
    }

    public Node<I> getSink() {
        if (sink == null) {
            sink = new Node<>(Acceptance.FALSE);
        }
        return sink;
    }

    private static <S, I> Word<I> findLive(DFA<S, I> dfa,
                                           S state,
                                           Collection<? extends I> inputs,
                                           MutableMapping<S, Boolean> deadStates) {
        if (dfa.isAccepting(state)) {
            return Word.epsilon();
        }

        Boolean dead = deadStates.get(state);
        if (dead != null && dead) {
            return null;
        }
        deadStates.put(state, true);

        Deque<FindLiveRecord<S, I>> dfsStack = new ArrayDeque<>();
        dfsStack.push(new FindLiveRecord<>(state, null, inputs.iterator()));

        while (!dfsStack.isEmpty()) {
            FindLiveRecord<S, I> rec = dfsStack.peek();
            if (!rec.inputIt.hasNext()) {
                dfsStack.pop();
                continue;
            }
            I input = rec.inputIt.next();

            S succ = dfa.getTransition(rec.state, input);
            if (succ == null) {
                continue;
            }
            if (dfa.isAccepting(succ)) {
                WordBuilder<I> wb = new WordBuilder<>(dfsStack.size());
                wb.append(input);

                dfsStack.pop();
                while (!dfsStack.isEmpty()) {
                    wb.append(rec.incomingInput);
                    rec = dfsStack.pop();
                }
                return wb.reverse().toWord();
            }

            dead = deadStates.get(succ);
            if (dead == null) {
                dfsStack.push(new FindLiveRecord<>(succ, input, inputs.iterator()));
                deadStates.put(succ, true);
            } else {
                assert (dead);
            }
        }

        return null;
    }

    private static final class FindLiveRecord<S, I> {

        public final S state;
        public final I incomingInput;
        public final Iterator<? extends I> inputIt;

        FindLiveRecord(S state, I incomingInput, Iterator<? extends I> inputIt) {
            this.state = state;
            this.incomingInput = incomingInput;
            this.inputIt = inputIt;
        }
    }

    public class TransitionSystemView extends IncrementalDFATreeBuilder<I>.TransitionSystemView {

        @Override
        public Node<I> getTransition(Node<I> state, I input) {
            if (state.getAcceptance() == Acceptance.FALSE) {
                return state;
            }
            return super.getTransition(state, input);
        }
    }

}
