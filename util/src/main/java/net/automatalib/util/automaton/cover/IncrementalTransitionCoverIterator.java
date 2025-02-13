/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.util.automaton.cover;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;

import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.word.Word;

/**
 * An iterator for the transition cover of an automaton. Words are computed lazily (i.e., only when request by
 * {@link #next()}).
 * <p>
 * Supports incremental computation, i.e. given a set of cover traces, only sequences for transitions not covered by
 * these traces are returned.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 *
 * @see Covers#transitionCover(DeterministicAutomaton, Collection, Collection)
 */
class IncrementalTransitionCoverIterator<S, I> extends AbstractSimplifiedIterator<Word<I>> {

    private final DeterministicAutomaton<S, I, ?> automaton;
    private final Collection<? extends I> inputs;
    private final Collection<? extends Word<I>> oldCover;

    private final MutableMapping<S, Record<S, I>> reach;
    private final Queue<Record<S, I>> bfsQueue;

    private Iterator<? extends I> inputIterator;
    private Record<S, I> curr;

    IncrementalTransitionCoverIterator(DeterministicAutomaton<S, I, ?> automaton,
                                       Collection<? extends I> inputs,
                                       Collection<? extends Word<I>> oldCover) {
        this.automaton = automaton;
        this.inputs = inputs;
        this.oldCover = oldCover;
        this.reach = automaton.createStaticStateMapping();
        this.bfsQueue = new ArrayDeque<>();
    }

    @Override
    protected boolean calculateNext() {
        // first invocation
        if (inputIterator == null) {
            final S init = automaton.getInitialState();

            if (init == null) {
                return false;
            }

            initialize(init);

            curr = bfsQueue.poll();
            inputIterator = inputs.iterator();
        }

        while (curr != null) {
            while (inputIterator.hasNext()) {
                final I in = inputIterator.next();
                final S succ = automaton.getSuccessor(curr.state, in);

                if (succ != null) {
                    final Word<I> succAs = curr.accessSequence.append(in);

                    if (reach.get(succ) == null) {
                        final Record<S, I> succRec =
                                new Record<>(succ, succAs, new HashSet<>(HashUtil.capacity(inputs.size())));
                        reach.put(succ, succRec);
                        bfsQueue.add(succRec);
                    }

                    if (curr.coveredInputs.add(in)) {
                        super.nextValue = succAs;
                        return true;
                    }
                }
            }

            curr = bfsQueue.poll();
            inputIterator = inputs.iterator();
        }

        return false;
    }

    private void initialize(S init) {
        final Record<S, I> initRec =
                new Record<>(init, Word.epsilon(), new HashSet<>(HashUtil.capacity(inputs.size())));
        reach.put(init, initRec);
        bfsQueue.add(initRec);

        Covers.buildReachFromTransitionCover(reach,
                                             bfsQueue,
                                             automaton,
                                             oldCover,
                                             (s, as) -> new Record<>(s,
                                                                     as,
                                                                     new HashSet<>(HashUtil.capacity(inputs.size()))),
                                             w -> {});
    }
}
