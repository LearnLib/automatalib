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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.MapAlphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.ts.AcceptorPowersetViewTS;
import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.ts.acceptor.AcceptanceCombiner;
import net.automatalib.util.ts.acceptor.Acceptors;
import net.automatalib.util.ts.copy.TSCopy;
import net.automatalib.util.ts.traversal.TSTraversal;
import net.automatalib.util.ts.traversal.TSTraversalMethod;

/**
 * Operations on {@link NFA}s.
 * <p>
 * Note that the methods provided by this class do not modify their input arguments. Furthermore, results are copied
 * into new datastructures. For read-only views you may use the more generic {@link Acceptors} factory.
 */
public final class NFAs {

    private NFAs() {
        // prevent instantiation
    }

    /**
     * Calculates the conjunction ("and") of two NFAs via product construction and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> and(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return and(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the conjunction ("and") of two NFAs via product construction and stores the result in a given mutable
     * NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A and(NFA<?, I> nfa1,
                                                           NFA<?, I> nfa2,
                                                           Collection<? extends I> inputs,
                                                           A out) {
        AcceptorTS<?, I> acc = Acceptors.combine(nfa1, nfa2, AcceptanceCombiner.AND);

        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, acc, TSTraversal.NO_LIMIT, inputs, out);
        return out;
    }

    /**
     * Calculates the disjunction ("or") of two NFAs by merging their states and transitions. Returns the result as a
     * new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> or(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return or(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the disjunction ("or") of two NFAs by merging their states and transitions. Stores the result in a
     * given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A or(NFA<?, I> nfa1,
                                                          NFA<?, I> nfa2,
                                                          Collection<? extends I> inputs,
                                                          A out) {
        MutableNFAs.or(out, nfa1, inputs);
        MutableNFAs.or(out, nfa2, inputs);

        return out;
    }

    /**
     * Creates an NFA for the reverse language of the given input NFA.
     *
     * @param nfa
     *         the input NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return the reversed NFA
     */
    public static <I> CompactNFA<I> reverse(NFA<?, I> nfa, Alphabet<I> inputAlphabet) {
        final CompactNFA<I> result = new CompactNFA<>(inputAlphabet, nfa.size());
        reverse(nfa, inputAlphabet, result);
        return result;
    }

    /**
     * Writes an NFA for the reverse language of the given input NFA into the given output NFA.
     *
     * @param nfa
     *         the input NFA
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
     * @return a mapping from output states to their original input states
     */
    public static <SI, I, SO, A extends MutableNFA<SO, I>> Mapping<SO, SI> reverse(NFA<SI, I> nfa,
                                                                                   Collection<? extends I> inputs,
                                                                                   A out) {
        final Set<SI> initialStates = nfa.getInitialStates();
        final MutableMapping<SI, SO> mapping = nfa.createStaticStateMapping();
        final MutableMapping<SO, SI> reverseMapping = out.createDynamicStateMapping();

        // accepting states are initial states and vice versa
        for (SI si : nfa) {
            final SO so = out.addState(initialStates.contains(si));
            out.setInitial(so, nfa.isAccepting(si));
            mapping.put(si, so);
            reverseMapping.put(so, si);
        }

        // reverse transitions
        for (SI s1 : nfa) {
            for (I a : inputs) {
                for (SI s2 : nfa.getTransitions(s1, a)) {
                    out.addTransition(mapping.get(s2), a, mapping.get(s1));
                }
            }
        }

        return reverseMapping;
    }

    /**
     * Creates a trim NFA from the given input NFA. An NFA is trim if all of its states are accessible and
     * co-accessible.
     *
     * @param nfa
     *         the input NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return the trim NFA
     *
     * @see #accessibleStates(NFA, Collection)
     * @see #coaccessibleStates(NFA, Collection)
     */
    public static <I> CompactNFA<I> trim(NFA<?, I> nfa, Alphabet<I> inputAlphabet) {
        return trim(nfa, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Creates a trim NFA from the given input NFA and writes it to the given output NFA. An NFA is trim if all of its
     * states are accessible and co-accessible.
     *
     * @param nfa
     *         the input NFA
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
     * @see #accessibleStates(NFA, Collection)
     * @see #coaccessibleStates(NFA, Collection)
     */
    public static <SI, I, SO, A extends MutableNFA<SO, I>> A trim(NFA<SI, I> nfa,
                                                                  Collection<? extends I> inputs,
                                                                  A out) {
        final MutableMapping<SI, SO> mapping = nfa.createStaticStateMapping();
        final Set<SI> inits = nfa.getInitialStates();

        final Set<SI> states = accessibleStates(nfa, inputs);
        states.retainAll(coaccessibleStates(nfa, inputs));

        for (SI s : states) {
            final SO so = out.addState(nfa.isAccepting(s));
            out.setInitial(so, inits.contains(s));
            mapping.put(s, so);
        }

        for (SI s : states) {
            for (I i : inputs) {
                for (SI t : nfa.getTransitions(s, i)) {
                    if (states.contains(t)) {
                        out.addTransition(mapping.get(s), i, mapping.get(t));
                    }
                }
            }
        }

        return out;
    }

    /**
     * Returns for a given NFA the set of accessible states. A state is accessible if it can be reached by an initial
     * state.
     *
     * @param nfa
     *         the input NFA
     * @param inputs
     *         the input symbols to consider
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     *
     * @return the set of accessible states
     */
    public static <S, I> Set<S> accessibleStates(NFA<S, I> nfa, Collection<? extends I> inputs) {

        final Set<S> inits = nfa.getInitialStates();
        final Deque<S> deque = new ArrayDeque<>(inits);
        final Set<S> found = new HashSet<>(inits);

        while (!deque.isEmpty()) {
            final S curr = deque.pop();
            for (I sym : inputs) {
                for (S succ : nfa.getSuccessors(curr, sym)) {
                    if (found.add(succ)) {
                        deque.push(succ);
                    }
                }
            }
        }

        return found;
    }

    /**
     * Returns for a given NFA the set of co-accessible states. A state is co-accessible if it reaches an accepting
     * state.
     *
     * @param nfa
     *         the input NFA
     * @param inputs
     *         the input symbols to consider
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     *
     * @return the set of co-accessible states
     */
    public static <S, I> Set<S> coaccessibleStates(NFA<S, I> nfa, Collection<? extends I> inputs) {

        final CompactNFA<I> out = new CompactNFA<>(new MapAlphabet<>(inputs), nfa.size());
        final Mapping<Integer, S> mapping = reverse(nfa, inputs, out);
        final Set<Integer> states = accessibleStates(out, inputs);

        final Set<S> result = new HashSet<>(HashUtil.capacity(states.size()));

        for (Integer s : states) {
            result.add(mapping.get(s));
        }

        return result;
    }

    /**
     * Determinizes the given NFA, and returns the result as a new complete DFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return the determinized NFA
     */
    public static <I> CompactDFA<I> determinize(NFA<?, I> nfa, Alphabet<I> inputAlphabet) {
        return determinize(nfa, inputAlphabet, false, true);
    }

    /**
     * Determinizes the given NFA, and returns the result as a new DFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param partial
     *         allows the new DFA to be partial
     * @param minimize
     *         whether to minimize the DFA
     * @param <I>
     *         input symbol type
     *
     * @return the determinized NFA
     */
    public static <I> CompactDFA<I> determinize(NFA<?, I> nfa,
                                                Alphabet<I> inputAlphabet,
                                                boolean partial,
                                                boolean minimize) {
        CompactDFA<I> result = new CompactDFA<>(inputAlphabet);
        determinize(nfa, inputAlphabet, result, partial, minimize);
        return result;
    }

    /**
     * Determinizes the given NFA, and stores the result in a given mutable DFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param partial
     *         allows the new DFA to be partial
     * @param minimize
     *         whether to minimize the DFA
     * @param <I>
     *         input symbol type
     */
    public static <I> void determinize(NFA<?, I> nfa,
                                       Collection<? extends I> inputs,
                                       MutableDFA<?, I> out,
                                       boolean partial,
                                       boolean minimize) {
        doDeterminize(nfa.powersetView(), inputs, out, partial);
        if (minimize) {
            HopcroftMinimizer.minimizeDFAInvasive(out, inputs);
        }
    }

    /**
     * Determinizes the given NFA, and returns the result as a new DFA.
     *
     * @param nfa
     *         the original NFA
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return the determinized NFA
     */
    public static <I, A extends NFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> determinize(A nfa) {
        return determinize(nfa, false, true);
    }

    /**
     * Determinizes the given NFA, and returns the result as a new DFA.
     *
     * @param nfa
     *         the original NFA
     * @param partial
     *         allows the new DFA to be partial
     * @param minimize
     *         whether to minimize the DFA
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return the determinized NFA
     */
    public static <I, A extends NFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> determinize(A nfa,
                                                                                              boolean partial,
                                                                                              boolean minimize) {
        return determinize(nfa, nfa.getInputAlphabet(), partial, minimize);
    }

    /**
     * Determinizes the given NFA, and stores the result in a given mutable DFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param <I>
     *         input symbol type
     */
    public static <I> void determinize(NFA<?, I> nfa, Collection<? extends I> inputs, MutableDFA<?, I> out) {
        determinize(nfa, inputs, out, false, true);
    }

    private static <I, SI, SO> void doDeterminize(AcceptorPowersetViewTS<SI, I, ?> powerset,
                                                  Collection<? extends I> inputs,
                                                  MutableDFA<SO, I> out,
                                                  boolean partial) {
        Map<SI, SO> outStateMap = new HashMap<>();
        Deque<DeterminizeRecord<SI, SO>> stack = new ArrayDeque<>();

        final SI init = powerset.getInitialState();

        if (init == null) {
            return;
        }

        final boolean initAcc = powerset.isAccepting(init);
        final SO initOut = out.addInitialState(initAcc);

        outStateMap.put(init, initOut);

        stack.push(new DeterminizeRecord<>(init, initOut));

        while (!stack.isEmpty()) {
            DeterminizeRecord<SI, SO> curr = stack.pop();

            SI inState = curr.inputState;
            SO outState = curr.outputState;

            for (I sym : inputs) {
                final SI succ = powerset.getSuccessor(inState, sym);

                if (succ != null) {
                    SO outSucc = outStateMap.get(succ);
                    if (outSucc == null) {
                        // add new state to DFA and to stack
                        outSucc = out.addState(powerset.isAccepting(succ));
                        outStateMap.put(succ, outSucc);
                        stack.push(new DeterminizeRecord<>(succ, outSucc));
                    }
                    out.setTransition(outState, sym, outSucc);
                } else if (!partial) {
                    throw new IllegalStateException("Cannot create a total DFA from a partial powerset view");
                }
            }
        }
    }

    private static final class DeterminizeRecord<SI, SO> {

        private final SI inputState;
        private final SO outputState;

        DeterminizeRecord(SI inputState, SO outputState) {
            this.inputState = inputState;
            this.outputState = outputState;
        }
    }
}
