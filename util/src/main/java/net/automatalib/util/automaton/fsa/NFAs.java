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
package net.automatalib.util.automaton.fsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.util.automaton.Automata;
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
     * Most general way of combining two NFAs. The behavior is the same as of the above {@link #combine(NFA, NFA,
     * Collection, MutableNFA, AcceptanceCombiner)}, but the result automaton is automatically created as a {@link
     * CompactNFA}.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param combiner
     *         combination method for acceptance values
     *
     * @return a new NFA representing the combination of the specified NFAs
     */
    public static <I> CompactNFA<I> combine(NFA<?, I> nfa1,
                                            NFA<?, I> nfa2,
                                            Alphabet<I> inputAlphabet,
                                            AcceptanceCombiner combiner) {
        return combine(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet), combiner);
    }

    /**
     * Most general way of combining two NFAs. The {@link AcceptanceCombiner} specified via the {@code combiner}
     * parameter specifies how acceptance values of the NFAs will be combined to an acceptance value in the result NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         the mutable NFA for storing the result
     * @param combiner
     *         combination method for acceptance values
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A combine(NFA<?, I> nfa1,
                                                               NFA<?, I> nfa2,
                                                               Collection<? extends I> inputs,
                                                               A out,
                                                               AcceptanceCombiner combiner) {
        AcceptorTS<?, I> acc = Acceptors.combine(nfa1, nfa2, combiner);

        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, acc, TSTraversal.NO_LIMIT, inputs, out);
        return out;
    }

    /**
     * Calculates the conjunction ("and") of two NFAs, and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> and(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return and(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the conjunction ("and") of two NFAs, and stores the result in a given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A and(NFA<?, I> nfa1,
                                                           NFA<?, I> nfa2,
                                                           Collection<? extends I> inputs,
                                                           A out) {
        return combine(nfa1, nfa2, inputs, out, AcceptanceCombiner.AND);
    }

    /**
     * Calculates the disjunction ("or") of two NFAs, and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> or(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return or(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the disjunction ("or") of two NFAs, and stores the result in a given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A or(NFA<?, I> nfa1,
                                                          NFA<?, I> nfa2,
                                                          Collection<? extends I> inputs,
                                                          A out) {
        return combine(nfa1, nfa2, inputs, out, AcceptanceCombiner.OR);
    }

    /**
     * Calculates the exclusive-or ("xor") of two NFAs, and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> xor(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return xor(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the exclusive-or ("xor") of two NFAs, and stores the result in a given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A xor(NFA<?, I> nfa1,
                                                           NFA<?, I> nfa2,
                                                           Collection<? extends I> inputs,
                                                           A out) {
        return combine(nfa1, nfa2, inputs, out, AcceptanceCombiner.XOR);
    }

    /**
     * Calculates the equivalence ("&lt;=&gt;") of two NFAs, and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> equiv(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return equiv(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the equivalence ("&lt;=&gt;") of two NFAs, and stores the result in a given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A equiv(NFA<?, I> nfa1,
                                                             NFA<?, I> nfa2,
                                                             Collection<? extends I> inputs,
                                                             A out) {
        return combine(nfa1, nfa2, inputs, out, AcceptanceCombiner.EQUIV);
    }

    /**
     * Calculates the implication ("=&gt;") of two NFAs, and returns the result as a new NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a new NFA representing the conjunction of the specified NFA
     */
    public static <I> CompactNFA<I> impl(NFA<?, I> nfa1, NFA<?, I> nfa2, Alphabet<I> inputAlphabet) {
        return impl(nfa1, nfa2, inputAlphabet, new CompactNFA<>(inputAlphabet));
    }

    /**
     * Calculates the implication ("=&gt;") of two NFAs, and stores the result in a given mutable NFA.
     *
     * @param nfa1
     *         the first NFA
     * @param nfa2
     *         the second NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable NFA for storing the result
     *
     * @return {@code out}, for convenience
     */
    public static <I, S, A extends MutableNFA<S, I>> A impl(NFA<?, I> nfa1,
                                                            NFA<?, I> nfa2,
                                                            Collection<? extends I> inputs,
                                                            A out) {
        return combine(nfa1, nfa2, inputs, out, AcceptanceCombiner.IMPL);
    }

    /**
     * Determinizes the given NFA, and returns the result as a new complete DFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     *
     * @return a DFA
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
     *
     * @return a DFA
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
     */
    public static <I> void determinize(NFA<?, I> nfa,
                                       Collection<? extends I> inputs,
                                       MutableDFA<?, I> out,
                                       boolean partial,
                                       boolean minimize) {
        doDeterminize(nfa, inputs, out, partial);
        if (minimize) {
            Automata.invasiveMinimize(out, inputs);
        }
    }

    /**
     * Determinizes the given NFA, and returns the result as a new DFA.
     *
     * @param nfa
     *        the original NFA
     * @return a DFA
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
     * @return a DFA
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
     *        the original NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     */
    public static <I> void determinize(NFA<?, I> nfa, Collection<? extends I> inputs, MutableDFA<?, I> out) {
        determinize(nfa, inputs, out, false, true);
    }

    /**
     * Determinize the given NFA via the Subset (also called Powerset) Construction.
     *
     * @param nfa
     *         the original NFA
     * @param inputs
     *         the input symbols to consider
     * @param out
     *         a mutable DFA for storing the result
     * @param partial
     *         allows the new DFA to be partial
     */
    private static <I, SI, SO> void doDeterminize(NFA<SI, I> nfa,
                                                  Collection<? extends I> inputs,
                                                  MutableDFA<SO, I> out,
                                                  boolean partial) {

        Map<BitSet, SO> outStateMap = new HashMap<>();
        StateIDs<SI> stateIds = nfa.stateIDs();

        Deque<DeterminizeRecord<SI, SO>> stack = new ArrayDeque<>();

        // Add union of initial states to DFA and to stack
        List<SI> initList = new ArrayList<>(nfa.getInitialStates());
        BitSet initBs = new BitSet();
        for (SI init : initList) {
            initBs.set(stateIds.getStateId(init));
        }
        SO initOut = out.addInitialState(nfa.isAccepting(initList));
        outStateMap.put(initBs, initOut);
        stack.push(new DeterminizeRecord<>(initList, initOut));

        while (!stack.isEmpty()) {
            DeterminizeRecord<SI, SO> curr = stack.pop();

            List<SI> inStates = curr.inputStates;
            SO outState = curr.outputState;

            for (I sym : inputs) {
                BitSet succBs = new BitSet();
                List<SI> succList = new ArrayList<>();

                // Determine the union of the successors of the given state and symbol
                for (SI inState : inStates) {
                    for (SI succState : nfa.getSuccessors(inState, sym)) {
                        int succId = stateIds.getStateId(succState);
                        if (!succBs.get(succId)) {
                            succBs.set(succId);
                            succList.add(succState);
                        }
                    }
                }

                if (!partial || !succList.isEmpty()) {
                    SO outSucc = outStateMap.get(succBs);
                    if (outSucc == null) {
                        // add new state to DFA and to stack
                        outSucc = out.addState(nfa.isAccepting(succList));
                        outStateMap.put(succBs, outSucc);
                        stack.push(new DeterminizeRecord<>(succList, outSucc));
                    }
                    out.setTransition(outState, sym, outSucc);
                }
            }
        }

    }

    private static final class DeterminizeRecord<SI, SO> {

        private final List<SI> inputStates;
        private final SO outputState;

        DeterminizeRecord(List<SI> inputStates, SO outputState) {
            this.inputStates = inputStates;
            this.outputState = outputState;
        }
    }
}
