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
package net.automatalib.util.automaton.fsa;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.ts.acceptor.AcceptanceCombiner;
import net.automatalib.util.ts.acceptor.Acceptors;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalMethod;

/**
 * Operations on {@link DFA}s.
 * <p>
 * Note that the methods provided by this class do not modify their input arguments. Such methods are instead provided
 * by the {@link MutableDFAs} class. Furthermore, results are copied into new datastructures. For read-only views you
 * may use the more generic {@link Acceptors} factory.
 */
public final class DFAs {

    private DFAs() {
        // prevent instantiation
    }

    /**
     * Most general way of combining two DFAs via product construction. The behavior is the same as of the above
     * {@link #combine(DFA, DFA, Collection, MutableDFA, AcceptanceCombiner)}, but the result automaton is automatically
     * created as a {@link CompactDFA}.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param combiner
     *         combination method for acceptance values
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the combination of the specified DFA
     */
    public static <I> CompactDFA<I> combine(DFA<?, I> dfa1,
                                            DFA<?, I> dfa2,
                                            Alphabet<I> inputAlphabet,
                                            AcceptanceCombiner combiner) {
        return combine(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet), combiner);
    }

    /**
     * Most general way of combining two DFAs via product construction. The {@link AcceptanceCombiner} specified via the
     * {@code combiner} parameter specifies how acceptance values of the DFAs will be combined to an acceptance value in
     * the result DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         the mutable DFA for storing the result
     * @param combiner
     *         combination method for acceptance values
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A combine(DFA<?, I> dfa1,
                                                               DFA<?, I> dfa2,
                                                               Collection<? extends I> inputs,
                                                               A out,
                                                               AcceptanceCombiner combiner) {
        DeterministicAcceptorTS<?, I> acc = Acceptors.combine(dfa1, dfa2, combiner);

        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, acc, TSTraversal.NO_LIMIT, inputs, out);
        return out;
    }

    /**
     * Calculates the conjunction ("and") of two DFAs via product construction and returns the result as a new DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the conjunction of the specified DFA
     */
    public static <I> CompactDFA<I> and(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return and(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the conjunction ("and") of two DFAs via product construction and stores the result in a given mutable
     * DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A and(DFA<?, I> dfa1,
                                                           DFA<?, I> dfa2,
                                                           Collection<? extends I> inputs,
                                                           A out) {
        return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.AND);
    }

    /**
     * Calculates the disjunction ("or") of two DFAs via product construction and returns the result as a new DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the conjunction of the specified DFA
     */
    public static <I> CompactDFA<I> or(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return or(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the disjunction ("or") of two DFAs via product construction and stores the result in a given mutable
     * DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A or(DFA<?, I> dfa1,
                                                          DFA<?, I> dfa2,
                                                          Collection<? extends I> inputs,
                                                          A out) {
        return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.OR);
    }

    /**
     * Calculates the exclusive-or ("xor") of two DFAs via product construction and returns the result as a new DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the conjunction of the specified DFA
     */
    public static <I> CompactDFA<I> xor(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return xor(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the exclusive-or ("xor") of two DFAs via product construction and stores the result in a given mutable
     * DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A xor(DFA<?, I> dfa1,
                                                           DFA<?, I> dfa2,
                                                           Collection<? extends I> inputs,
                                                           A out) {
        return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.XOR);
    }

    /**
     * Calculates the equivalence ("&lt;=&gt;") of two DFAs via product construction and returns the result as a new
     * DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the conjunction of the specified DFA
     */
    public static <I> CompactDFA<I> equiv(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return equiv(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the equivalence ("&lt;=&gt;") of two DFAs via product construction and stores the result in a given
     * mutable DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A equiv(DFA<?, I> dfa1,
                                                             DFA<?, I> dfa2,
                                                             Collection<? extends I> inputs,
                                                             A out) {
        return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.EQUIV);
    }

    /**
     * Calculates the implication ("=&gt;") of two DFAs via product construction and returns the result as a new DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the conjunction of the specified DFA
     */
    public static <I> CompactDFA<I> impl(DFA<?, I> dfa1, DFA<?, I> dfa2, Alphabet<I> inputAlphabet) {
        return impl(dfa1, dfa2, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the implication ("=&gt;") of two DFAs via product construction and stores the result in a given
     * mutable DFA.
     *
     * @param dfa1
     *         the first DFA
     * @param dfa2
     *         the second DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A impl(DFA<?, I> dfa1,
                                                            DFA<?, I> dfa2,
                                                            Collection<? extends I> inputs,
                                                            A out) {
        return combine(dfa1, dfa2, inputs, out, AcceptanceCombiner.IMPL);
    }

    /**
     * Creates a trim DFA from the given input DFA. A DFA is trim if all of its states are accessible and
     * co-accessible.
     *
     * @param dfa
     *         the input DFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return the trim DFA
     *
     * @see NFAs#accessibleStates(NFA, Collection)
     * @see NFAs#coaccessibleStates(NFA, Collection)
     */
    public static <I> CompactDFA<I> trim(DFA<?, I> dfa, Alphabet<I> inputAlphabet) {
        return trim(dfa, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Creates a trim DFA from the given input DFA and writes it to the given output DFA. A DFA is trim if all of its
     * states are accessible and co-accessible.
     *
     * @param dfa
     *         the input DFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         the output NFA
     * @param <SI>
     *         (input) state type
     * @param <I>
     *         input symbol type
     * @param <SO>
     *         (output) state type
     * @param <A>
     *         (output) automaton type
     *
     * @return {@code out} for convenience
     *
     * @see NFAs#accessibleStates(NFA, Collection)
     * @see NFAs#coaccessibleStates(NFA, Collection)
     */
    public static <SI, I, SO, A extends MutableDFA<SO, I>> A trim(DFA<SI, I> dfa,
                                                                  Collection<? extends I> inputs,
                                                                  A out) {
        final MutableMapping<SI, SO> mapping = dfa.createStaticStateMapping();
        final SI init = dfa.getInitialState();

        final Set<SI> states = NFAs.accessibleStates(dfa, inputs);
        states.retainAll(NFAs.coaccessibleStates(dfa, inputs));

        for (SI s : states) {
            final SO so = out.addState(dfa.isAccepting(s));
            out.setInitial(so, Objects.equals(init, s));
            mapping.put(s, so);
        }

        for (SI s : states) {
            for (I i : inputs) {
                final SI t = dfa.getSuccessor(s, i);
                if (states.contains(t)) {
                    out.addTransition(mapping.get(s), i, mapping.get(t));
                }
            }
        }

        return out;
    }

    /**
     * Calculates the complement (negation) of a DFA and returns the result as a new DFA.
     * <p>
     * Note that unlike {@link MutableDFA#flipAcceptance()}, undefined transitions are treated as leading to a rejecting
     * sink state (and are thus turned into an accepting sink).
     *
     * @param dfa
     *         the DFA to complement
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new DFA representing the complement of the specified DFA
     */
    public static <I> CompactDFA<I> complement(DFA<?, I> dfa, Alphabet<I> inputAlphabet) {
        return complement(dfa, inputAlphabet, new CompactDFA<>(inputAlphabet));
    }

    /**
     * Calculates the complement (negation) of a DFA, and stores the result in a given mutable DFA.
     * <p>
     * Note that unlike {@link MutableDFA#flipAcceptance()}, undefined transitions are treated as leading to a rejecting
     * sink state (and are thus turned into an accepting sink).
     *
     * @param dfa
     *         the DFA to complement
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableDFA<S, I>> A complement(DFA<?, I> dfa,
                                                                  Collection<? extends I> inputs,
                                                                  A out) {
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE,
                                   dfa,
                                   inputs,
                                   out,
                                   b -> b == null || !b,
                                   t -> null);
        MutableDFAs.complete(out, inputs, false, true);
        return out;
    }

    public static <I> CompactDFA<I> complete(DFA<?, I> dfa, Alphabet<I> inputs) {
        return complete(dfa, inputs, new CompactDFA<>(inputs));
    }

    public static <I, S, A extends MutableDFA<S, I>> A complete(DFA<?, I> dfa, Collection<? extends I> inputs, A out) {
        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.DFS, dfa, inputs, out);
        MutableDFAs.complete(out, inputs, true);
        return out;
    }

    /**
     * Minimizes the given DFA over the given alphabet. This method does not modify the given DFA, but returns the
     * minimized version as a new instance.
     * <p>
     * <b>Note:</b> the DFA must be completely specified.
     *
     * @param dfa
     *         the DFA to be minimized
     * @param alphabet
     *         the input alphabet to consider for minimization (this will also be the input alphabet of the resulting
     *         automaton)
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimize(DFA<?, I> dfa, Alphabet<I> alphabet) {
        return HopcroftMinimizer.minimizeDFA(dfa, alphabet);
    }

    /**
     * Minimizes the given DFA. This method does not modify the given DFA, but returns the minimized version as a new
     * instance.
     * <p>
     * <b>Note:</b> the DFA must be completely specified
     *
     * @param dfa
     *         the DFA to be minimized
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified DFA
     */
    public static <S, I, A extends DFA<S, I> & InputAlphabetHolder<I>> CompactDFA<I> minimize(A dfa) {
        return HopcroftMinimizer.minimizeDFA(dfa);
    }

    /**
     * Computes whether the language of the given DFA is prefix-closed.
     * <p>
     * Assumes all states in the given {@link DFA} are reachable from the initial state.
     *
     * @param dfa
     *         the DFA to check
     * @param inputs
     *         the input symbols to consider
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     *
     * @return whether the DFA is prefix-closed
     */
    public static <S, I> boolean isPrefixClosed(DFA<S, I> dfa, Collection<I> inputs) {
        for (S s : dfa) {
            if (!dfa.isAccepting(s)) {
                for (I i : inputs) {
                    final S succ = dfa.getSuccessor(s, i);
                    if (succ != null && dfa.isAccepting(succ)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Computes whether the given {@link DFA} accepts the empty language.
     * <p>
     * Assumes all states in the given {@link DFA} are reachable from the initial state.
     *
     * @param dfa
     *         the {@link DFA} to check
     * @param <S>
     *         state type
     *
     * @return whether the given {@link DFA} accepts the empty language
     */
    public static <S> boolean acceptsEmptyLanguage(DFA<S, ?> dfa) {
        for (S s : dfa) {
            if (dfa.isAccepting(s)) {
                return false;
            }
        }
        return true;
    }
}
