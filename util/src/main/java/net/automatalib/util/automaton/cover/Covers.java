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
package net.automatalib.util.automaton.cover;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import net.automatalib.automaton.DeterministicAutomaton.FiniteSemantics;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Covers {

    private Covers() {}

    /**
     * Computes a state cover for a given automaton.
     * <p>
     * A state cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> of an automaton, there
     * exists an input sequence in <i>C</i> that transitions the automaton from its initial state to state s.
     * <p>
     * Note: if restrictions on the {@code inputs} parameter do not allow to reach certain states, the computed cover is
     * not complete.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param states
     *         the collection in which the sequences will be stored
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     */
    public static <S, I> void stateCover(FiniteSemantics<S, I, ?> automaton,
                                         Collection<? extends I> inputs,
                                         Collection<? super Word<I>> states) {
        cover(automaton, inputs, automaton.getInitialState(), states::add, w -> {});
    }

    /**
     * Returns an iterator for the sequences of a state cover. Sequences are computed lazily (i.e., as requested by the
     * iterators {@link Iterator#next() next} method).
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the input sequences of the cover.
     *
     * @see #stateCover(FiniteSemantics, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> stateCoverIterator(FiniteSemantics<?, I, ?> automaton,
                                                           Collection<? extends I> inputs) {
        return new IncrementalStateCoverIterator<>(automaton, inputs, Collections.emptyList());
    }

    /**
     * Computes a transition cover for a given automaton.
     * <p>
     * A transition cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> and each input symbol
     * <i>i</i> of an automaton, there exists an input sequence in <i>C</i> that starts from the initial state of the
     * automaton and ends with the transition that applies <i>i</i> to state <i>s</i>.
     * <p>
     * Note: if restrictions on the {@code inputs} parameter do not allow to reach certain transitions, the computed
     * cover is not complete.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param transitions
     *         the collection in which the sequences will be stored
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     */
    public static <S, I> void transitionCover(FiniteSemantics<S, I, ?> automaton,
                                              Collection<? extends I> inputs,
                                              Collection<? super Word<I>> transitions) {
        cover(automaton, inputs, automaton.getInitialState(), w -> {}, transitions::add);
    }

    /**
     * Returns an iterator for the sequences of a transition cover. Sequences are computed lazily (i.e., as requested by
     * the iterators {@link Iterator#next() next} method).
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the input sequences of the cover.
     *
     * @see #transitionCover(FiniteSemantics, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> transitionCoverIterator(FiniteSemantics<?, I, ?> automaton,
                                                                Collection<? extends I> inputs) {
        return new IncrementalTransitionCoverIterator<>(automaton, inputs, Collections.emptyList());
    }

    /**
     * Computes a structural cover for a given automaton.
     * <p>
     * A structural cover is the union of a state cover and a transition cover
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param cover
     *         the collection in which the sequences will be stored
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     *
     * @see #stateCover(FiniteSemantics, Collection, Collection)
     * @see #transitionCover(FiniteSemantics, Collection, Collection)
     */
    public static <S, I> void structuralCover(FiniteSemantics<S, I, ?> automaton,
                                              Collection<? extends I> inputs,
                                              Collection<? super Word<I>> cover) {
        cover(automaton, inputs, automaton.getInitialState(), cover::add, cover::add);
    }

    /**
     * Utility method that allows to compute a state and transition cover simultaneously.
     *
     * @param automaton
     *         the automaton for which the covers should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param states
     *         the collection in which the state cover sequences will be stored
     * @param transitions
     *         the collection in which the transition cover sequences will be stored
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     *
     * @see #stateCover(FiniteSemantics, Collection, Collection)
     * @see #transitionCover(FiniteSemantics, Collection, Collection)
     */
    public static <S, I> void cover(FiniteSemantics<S, I, ?> automaton,
                                    Collection<? extends I> inputs,
                                    Collection<? super Word<I>> states,
                                    Collection<? super Word<I>> transitions) {
        cover(automaton, inputs, automaton.getInitialState(), states::add, transitions::add);
    }

    /**
     * Computes the state and transition covers for a given automaton beginning in the given state.
     * <p>
     * A state cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> of an automaton, there
     * exists an input sequence in <i>C</i> that transitions the automaton from its initial state to state s.
     * <p>
     * A transition cover is a set <i>C</i> of input sequences, such that for each state <i>s</i> and each input symbol
     * <i>i</i> of an automaton, there exists an input sequence in <i>C</i> that starts from the initial state of the
     * automaton and ends with the transition that applies <i>i</i> to state <i>s</i>.
     * <p>
     * Note: if restrictions on the {@code inputs} parameter do not allow to reach certain states or transitions, the
     * computed covers are not complete.
     *
     * @param automaton
     *         the automaton for which the covers should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param start
     *         the state from which to begin the computation of overs
     * @param states
     *         a consumer that accepts the state cover sequences
     * @param transitions
     *         a consumer that accepts the transition cover sequences
     * @param <S>
     *         automaton state type
     * @param <I>
     *         input symbol type
     *
     * @return a mapping from automaton states to their access sequences
     */
    public static <S, I> Mapping<S, @Nullable Word<I>> cover(FiniteSemantics<S, I, ?> automaton,
                                                             Collection<? extends I> inputs,
                                                             @Nullable S start,
                                                             Consumer<? super Word<I>> states,
                                                             Consumer<? super Word<I>> transitions) {

        MutableMapping<S, @Nullable Word<I>> reach = automaton.createStaticStateMapping();

        if (start == null) {
            return reach;
        }

        reach.put(start, Word.epsilon());

        Queue<S> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(start);

        states.accept(Word.epsilon());

        S curr;

        while ((curr = bfsQueue.poll()) != null) {
            @SuppressWarnings("nullness") // in a breadth-first traversal the predecessors are always defined
            @NonNull
            Word<I> as = reach.get(curr);

            for (I in : inputs) {
                S succ = automaton.getSuccessor(curr, in);
                if (succ == null) {
                    continue;
                }

                final Word<I> succAs = as.append(in);

                if (reach.get(succ) == null) {
                    reach.put(succ, succAs);
                    states.accept(succAs);
                    bfsQueue.add(succ);
                }
                transitions.accept(succAs);
            }
        }

        return reach;
    }

    /**
     * Computes an incremental state cover for a given automaton, i.e. a cover that only contains the missing sequences
     * for obtaining a complete state cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldStates
     *         the collection containing the already existing sequences of the state cover
     * @param newStates
     *         the collection in which the missing sequences will be stored
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the state cover, {@code false} otherwise.
     *
     * @see #stateCover(FiniteSemantics, Collection, Collection)
     */
    public static <S, I> boolean incrementalStateCover(FiniteSemantics<S, I, ?> automaton,
                                                       Collection<? extends I> inputs,
                                                       Collection<? extends Word<I>> oldStates,
                                                       Collection<? super Word<I>> newStates) {
        S init = automaton.getInitialState();

        if (init == null) {
            return false;
        }

        MutableMapping<S, Record<S, I>> reach = automaton.createStaticStateMapping();

        boolean augmented = false;

        Queue<Record<S, I>> bfsQueue = new ArrayDeque<>();

        buildReachFromStateCover(reach, bfsQueue, automaton, oldStates, Record::new);

        if (reach.get(init) == null) {
            // apparently the initial state was not yet covered
            Record<S, I> rec = new Record<>(init, Word.epsilon());
            reach.put(init, rec);
            bfsQueue.add(rec);
            newStates.add(Word.epsilon());
            augmented = true;
        }

        Record<S, I> curr;
        while ((curr = bfsQueue.poll()) != null) {
            S state = curr.state;
            Word<I> as = curr.accessSequence;

            for (I in : inputs) {
                S succ = automaton.getSuccessor(state, in);
                if (succ == null) {
                    continue;
                }

                if (reach.get(succ) == null) {
                    Word<I> succAs = as.append(in);
                    Record<S, I> succRec = new Record<>(succ, succAs);
                    reach.put(succ, succRec);
                    bfsQueue.add(succRec);
                    newStates.add(succAs);
                    augmented = true;
                }
            }
        }

        return augmented;
    }

    /**
     * Returns an iterator for the remaining sequences of a state cover. Sequences are computed lazily (i.e., as
     * requested by the iterators {@link Iterator#next() next} method).
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param stateCover
     *         the collection containing the already existing sequences of the state cover
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the remaining input sequences of the cover.
     *
     * @see #incrementalStateCover(FiniteSemantics, Collection, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> incrementalStateCoverIterator(FiniteSemantics<?, I, ?> automaton,
                                                                      Collection<? extends I> inputs,
                                                                      Collection<? extends Word<I>> stateCover) {
        return new IncrementalStateCoverIterator<>(automaton, inputs, stateCover);
    }

    /**
     * Computes an incremental transition cover for a given automaton, i.e. a cover that only contains the missing
     * sequences for obtaining a complete transition cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldTransCover
     *         the collection containing the already existing sequences of the transition cover
     * @param newTransCover
     *         the collection in which the missing sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the state cover, {@code false} otherwise.
     *
     * @see #transitionCover(FiniteSemantics, Collection, Collection)
     */
    public static <I> boolean incrementalTransitionCover(FiniteSemantics<?, I, ?> automaton,
                                                         Collection<? extends I> inputs,
                                                         Collection<? extends Word<I>> oldTransCover,
                                                         Collection<? super Word<I>> newTransCover) {
        final int oldTransSize = newTransCover.size();

        incrementalCover(automaton, inputs, Collections.emptySet(), oldTransCover, w -> {}, newTransCover::add);

        return oldTransSize < newTransCover.size();
    }

    /**
     * Returns an iterator for the remaining sequences of a transition cover. Sequences are computed lazily (i.e., as
     * requested by the iterators {@link Iterator#next() next} method).
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param transitionCover
     *         the collection containing the already existing sequences of the transition cover
     * @param <I>
     *         input symbol type
     *
     * @return an iterator for the remaining input sequences of the cover.
     *
     * @see #incrementalStateCover(FiniteSemantics, Collection, Collection, Collection)
     */
    public static <I> Iterator<Word<I>> incrementalTransitionCoverIterator(FiniteSemantics<?, I, ?> automaton,
                                                                           Collection<? extends I> inputs,
                                                                           Collection<? extends Word<I>> transitionCover) {
        return new IncrementalTransitionCoverIterator<>(automaton, inputs, transitionCover);
    }

    /**
     * Utility method that allows to compute an incremental state and transition cover simultaneously.
     *
     * @param automaton
     *         the automaton for which the covers should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param oldStateCover
     *         the collection containing the already existing sequences of the state cover
     * @param oldTransCover
     *         the collection containing the already existing sequences of the transition cover
     * @param newStateCover
     *         the collection in which the missing state cover sequences will be stored
     * @param newTransCover
     *         the collection in which the missing transition cover sequences will be stored
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if new sequences have been added to the structural cover, {@code false} otherwise.
     *
     * @see #incrementalStateCover(FiniteSemantics, Collection, Collection, Collection)
     * @see #incrementalStateCover(FiniteSemantics, Collection, Collection, Collection)
     */
    public static <I> boolean incrementalCover(FiniteSemantics<?, I, ?> automaton,
                                               Collection<? extends I> inputs,
                                               Collection<? extends Word<I>> oldStateCover,
                                               Collection<? extends Word<I>> oldTransCover,
                                               Collection<? super Word<I>> newStateCover,
                                               Collection<? super Word<I>> newTransCover) {
        final int oldStateSize = newStateCover.size();
        final int oldTransSize = newTransCover.size();

        incrementalCover(automaton, inputs, oldStateCover, oldTransCover, newStateCover::add, newTransCover::add);

        return oldStateSize < newStateCover.size() || oldTransSize < newTransCover.size();
    }

    private static <S, I> void incrementalCover(FiniteSemantics<S, I, ?> automaton,
                                                Collection<? extends I> inputs,
                                                Collection<? extends Word<I>> oldStateCover,
                                                Collection<? extends Word<I>> oldTransCover,
                                                Consumer<? super Word<I>> newStateCover,
                                                Consumer<? super Word<I>> newTransCover) {

        S init = automaton.getInitialState();

        if (init == null) {
            return;
        }

        MutableMapping<S, Record<S, I>> reach = automaton.createStaticStateMapping();

        Queue<Record<S, I>> bfsQueue = new ArrayDeque<>();

        buildReachFromStateCover(reach,
                                 bfsQueue,
                                 automaton,
                                 oldStateCover,
                                 (s, as) -> new Record<>(s, as, new HashSet<>(HashUtil.capacity(inputs.size()))));

        if (reach.get(init) == null) {
            // apparently the initial state was not yet covered
            Record<S, I> rec = new Record<>(init, Word.epsilon(), new HashSet<>(HashUtil.capacity(inputs.size())));
            reach.put(init, rec);
            bfsQueue.add(rec);
            newStateCover.accept(Word.epsilon());
        }

        // Add transition covers
        buildReachFromTransitionCover(reach,
                                      bfsQueue,
                                      automaton,
                                      oldTransCover,
                                      (s, as) -> new Record<>(s, as, new HashSet<>(HashUtil.capacity(inputs.size()))),
                                      newStateCover);

        Record<S, I> curr;
        while ((curr = bfsQueue.poll()) != null) {
            for (I input : inputs) {
                S succ = automaton.getSuccessor(curr.state, input);

                if (succ != null) {
                    Record<S, I> succRec = reach.get(succ);
                    Word<I> newAs = curr.accessSequence.append(input);

                    if (succRec == null) {
                        // new state!
                        succRec = new Record<>(succ, newAs);
                        bfsQueue.add(succRec);
                        reach.put(succ, succRec);

                        newStateCover.accept(newAs);
                    }
                    // new transition
                    if (!curr.coveredInputs.contains(input)) {
                        newTransCover.accept(newAs);
                    }
                }
            }
        }
    }

    static <S, I> boolean buildReachFromStateCover(MutableMapping<S, Record<S, I>> reach,
                                                   Queue<Record<S, I>> bfsQueue,
                                                   FiniteSemantics<S, I, ?> automaton,
                                                   Collection<? extends Word<I>> oldStateCover,
                                                   BiFunction<S, Word<I>, Record<S, I>> recordBuilder) {

        boolean hasEpsilon = false;

        for (Word<I> oldStateAs : oldStateCover) {
            S state = automaton.getState(oldStateAs);
            if (state == null || reach.get(state) != null) {
                if (oldStateAs.isEmpty()) {
                    hasEpsilon = true;
                }
                continue; // strange, but we'll ignore it
            }

            Record<S, I> rec = recordBuilder.apply(state, oldStateAs);
            bfsQueue.add(rec);
            reach.put(state, rec);
        }

        return hasEpsilon;
    }

    static <S, I> void buildReachFromTransitionCover(MutableMapping<S, Record<S, I>> reach,
                                                     Queue<Record<S, I>> bfsQueue,
                                                     FiniteSemantics<S, I, ?> automaton,
                                                     Collection<? extends Word<I>> oldTransCover,
                                                     BiFunction<S, Word<I>, Record<S, I>> recordBuilder,
                                                     Consumer<? super Word<I>> newStateCallback) {

        for (Word<I> oldTransAs : oldTransCover) {

            if (!oldTransAs.isEmpty()) { // ignore trivial covers
                Word<I> predAs = oldTransAs.prefix(oldTransAs.length() - 1);
                S pred = automaton.getState(predAs);

                if (pred != null) { // ignore redundant covers
                    Record<S, I> predRec = reach.get(pred);

                    if (predRec == null) { // new state, use prefix as state cover sequence
                        predRec = recordBuilder.apply(pred, predAs);
                        bfsQueue.add(predRec);
                        reach.put(pred, predRec);
                        newStateCallback.accept(predAs);
                    }

                    I lastSym = oldTransAs.lastSymbol();
                    predRec.coveredInputs.add(lastSym);
                }
            }
        }
    }

}
