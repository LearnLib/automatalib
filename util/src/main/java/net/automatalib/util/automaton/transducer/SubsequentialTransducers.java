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
package net.automatalib.util.automaton.transducer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.automatalib.automaton.transducer.MutableSubsequentialTransducer;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.word.Word;

/**
 * Utility methods of {@link SubsequentialTransducer}s.
 */
public final class SubsequentialTransducers {

    private SubsequentialTransducers() {
        // prevent initialization
    }

    /**
     * Constructs a new <i>onward</i> subsequential transducer for a given {@link SubsequentialTransducer SST}. In an
     * onward SST, for each state except the initial state, the longest common prefix over the state output and the
     * outputs of all outgoing transitions of a state is {@link Word#epsilon() epsilon}. This can be achieved by pushing
     * back the longest common prefix to the transition outputs of the incoming transitions of each state.
     *
     * @param sst
     *         the original SST
     * @param inputs
     *         the alphabet symbols to consider for this transformation
     * @param out
     *         the target automaton to write the onward form to
     * @param <S>
     *         state type (of the output SST)
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type (of the output SST)
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <S, I, T, O, A extends MutableSubsequentialTransducer<S, I, T, O>> A toOnwardSST(
            SubsequentialTransducer<?, I, ?, O> sst,
            Collection<? extends I> inputs,
            A out) {
        return toOnwardSST(sst, inputs, out, true);
    }

    /**
     * Constructs a new <i>onward</i> subsequential transducer for a given {@link SubsequentialTransducer SST}. In an
     * onward SST, for each state except the initial state, the longest common prefix over the state output and the
     * outputs of all outgoing transitions of a state is {@link Word#epsilon() epsilon}. This can be achieved by pushing
     * back the longest common prefix to the transition outputs of the incoming transitions of each state.
     *
     * @param sst
     *         the original SST
     * @param inputs
     *         the alphabet symbols to consider for this transformation
     * @param out
     *         the target automaton to write the onward form to
     * @param minimize
     *         a flag indicating whether the final result should be minimized
     * @param <S>
     *         state type (of the output SST)
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type (of the output SST)
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <S, I, T, O, A extends MutableSubsequentialTransducer<S, I, T, O>> A toOnwardSST(
            SubsequentialTransducer<?, I, ?, O> sst,
            Collection<? extends I> inputs,
            A out,
            boolean minimize) {

        assert out.size() == 0;
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, sst, inputs, out);

        final Mapping<S, Set<Pair<S, I>>> incomingTransitions = getIncomingTransitions(out, inputs);
        final Deque<S> queue = new ArrayDeque<>(out.getStates());

        final S oldInit = out.getInitialState();

        if (oldInit != null && !incomingTransitions.get(oldInit).isEmpty()) {
            // copy initial state to prevent push-back of prefixes for the initial state.
            out.setInitial(oldInit, false);
            final S newInit = out.addInitialState(out.getStateProperty(oldInit));

            for (I i : inputs) {
                final T oldT = out.getTransition(oldInit, i);
                if (oldT != null) {
                    final S succ = out.getSuccessor(oldT);
                    out.addTransition(newInit, i, succ, out.getTransitionProperty(oldT));
                    incomingTransitions.get(succ).add(Pair.of(newInit, i));
                }
            }
        }

        while (!queue.isEmpty()) {
            final S s = queue.pop();
            if (Objects.equals(s, out.getInitialState())) {
                continue;
            }
            final Word<O> lcp = computeLCP(out, inputs, s);

            if (!lcp.isEmpty()) {
                final Word<O> oldStateProperty = out.getStateProperty(s);
                final Word<O> newStateProperty = oldStateProperty.subWord(lcp.length());

                out.setStateProperty(s, newStateProperty);

                for (I i : inputs) {
                    final T t = out.getTransition(s, i);
                    if (t != null) {
                        final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                        final Word<O> newTransitionProperty = oldTransitionProperty.subWord(lcp.length());

                        out.setTransitionProperty(t, newTransitionProperty);
                    }
                }

                for (Pair<S, I> trans : incomingTransitions.get(s)) {
                    final S src = trans.getFirst();
                    final T t = out.getTransition(src, trans.getSecond());
                    assert t != null;

                    final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                    final Word<O> newTransitionProperty = oldTransitionProperty.concat(lcp);

                    out.setTransitionProperty(t, newTransitionProperty);
                    if (!queue.contains(src)) {
                        queue.add(src);
                    }
                }
            }
        }

        return minimize ? HopcroftMinimizer.minimizeUniversalInvasive(out, inputs) : out;
    }

    /**
     * Checks whether a given {@link SubsequentialTransducer} is <i>onward</i>, i.e. if for each state (sans initial
     * state) the longest common prefix over its state property and the transition properties of its outgoing edges
     * equals {@link Word#epsilon() epsilon}.
     *
     * @param sst
     *         the SST to check
     * @param inputs
     *         the input symbols to consider for this check
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return {@code true} if {@code sst} is onward, {@code false} otherwise
     */
    public static <S, I, O> boolean isOnwardSST(SubsequentialTransducer<S, I, ?, O> sst,
                                                Collection<? extends I> inputs) {

        for (S s : sst) {
            if (Objects.equals(s, sst.getInitialState())) {
                continue;
            }

            final Word<O> lcp = computeLCP(sst, inputs, s);
            if (!lcp.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static <S, I, T> Mapping<S, Set<Pair<S, I>>> getIncomingTransitions(SubsequentialTransducer<S, I, T, ?> sst,
                                                                                Collection<? extends I> inputs) {

        final MutableMapping<S, Set<Pair<S, I>>> result = sst.createStaticStateMapping();

        for (S s : sst) {
            result.put(s, new HashSet<>());
        }

        for (S s : sst) {
            for (I i : inputs) {
                final T t = sst.getTransition(s, i);

                if (t != null) {
                    final S succ = sst.getSuccessor(t);
                    result.get(succ).add(Pair.of(s, i));
                }
            }
        }

        return result;
    }

    private static <S, I, T, O> Word<O> computeLCP(SubsequentialTransducer<S, I, T, O> sst,
                                                   Collection<? extends I> inputs,
                                                   S s) {

        Word<O> lcp = sst.getStateProperty(s);

        for (I i : inputs) {
            T t = sst.getTransition(s, i);
            if (t != null) {
                lcp = lcp.longestCommonPrefix(sst.getTransitionProperty(t));
            }
        }

        return lcp;
    }

}
