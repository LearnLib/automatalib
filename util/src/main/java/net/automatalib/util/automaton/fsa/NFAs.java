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
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.ts.AcceptorPowersetViewTS;
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
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
     * @param <I>
     *         input symbol type
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
     * @param <I>
     *         input symbol type
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
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
     * Reverse the specified NFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     *
     * @return the reversed NFA
     */
    public static <I> CompactNFA<I> reverse(MutableNFA<Integer, I> nfa, Alphabet<I> inputAlphabet) {
        CompactNFA<I> result = new CompactNFA<>(inputAlphabet);
        reverse(nfa, inputAlphabet, result);
        return result;
    }

    /**
     * Reverse the specified NFA, and stores the result in a given mutable NFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputs
     *         the input symbols to consider
     * @param rNFA
     *         a mutable NFA for storing the result
     * @param <S>
     *         state type of the automata
     * @param <I>
     *         input symbol type
     * @param <A>
     *         reversed NFA type
     */
    public static <S, I, A extends MutableNFA<S, I>> void reverse(MutableNFA<S, I> nfa,
                                      Collection<? extends I> inputs,
                                      A rNFA) {
        Set<S> initialStates = nfa.getInitialStates();

        // Accepting are initial states and vice versa
        for(S q: nfa.getStates()) {
            rNFA.addState(initialStates.contains(q));
            if (nfa.isAccepting(q)) {
                rNFA.setInitial(q, true);
            }
        }
        // reverse transitions
        for(S q: nfa.getStates()) {
            for(I a: inputs) {
                for(S s: nfa.getTransitions(q,a)) {
                    rNFA.addTransition(s, a, q);
                }
            }
        }
    }

    /**
     * Create a trim (co-accessible) NFA from the specified NFA.
     *
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param <I>
     *         input symbol type
     * @return the trim NFA
     */
    public static <I> CompactNFA<I> trim(CompactNFA<I> nfa, Alphabet<I> inputAlphabet) {
        CompactNFA<I> result = new CompactNFA<>(inputAlphabet);
        trim(nfa, inputAlphabet, result);
        return result;
    }

    /**
     * Create a trim (co-accessible) NFA from the specified NFA, and store the result in a given mutable NFA.
     * @param nfa
     *         the original NFA
     * @param inputAlphabet
     *         the input alphabet
     * @param trimNFA
     *         a mutable NFA for storing the result
     * @param <S>
     *         state type of the automata
     * @param <I>
     *         input symbol type
     * @param <A>
     *         trim NFA type
     */
    @SuppressWarnings("unchecked")
    public static <S, I, A extends MutableNFA<S, I>> void trim(CompactNFA<I> nfa,
                                Alphabet<I> inputAlphabet,
                                A trimNFA) {
        Set<Integer> coAccessibleStates = new HashSet<>(nfa.size());
        for (int i = 0; i < nfa.size(); i++) {
            coAccessibleStates.add(i);
        }
        // right trim
        coAccessibleStates.retainAll(rightTrimHelper(nfa, inputAlphabet));
        // left trim
        coAccessibleStates.retainAll(rightTrimHelper(reverse(nfa, inputAlphabet), inputAlphabet));

        // Quotient based upon co-accessible states
        // determine mapping of old states to new ones
        Object[] oldToNewMap = new Object[nfa.size()];
        // Add new states -- initial, accepting properties
        for (int i = 0; i < nfa.size(); i++) {
            if (!coAccessibleStates.contains(i)) {
                continue;
            }
            S newState = trimNFA.addState(nfa.isAccepting(i));
            oldToNewMap[i] = newState;
            if (nfa.getInitialStates().contains(i)) {
                trimNFA.setInitial(newState, true);
            }
        }
        // Add transitions to co-accessible states
        for (int i = 0; i < nfa.size(); i++) {
            if (!coAccessibleStates.contains(i)) {
                continue;
            }
            for (I j : inputAlphabet) {
                for (int k : nfa.getTransitions(i, j)) {
                    if (coAccessibleStates.contains(k)) {
                        trimNFA.addTransition((S) oldToNewMap[i], j, (S) oldToNewMap[k]);
                    }
                }
            }
        }
    }

    static <S, I> Set<S> rightTrimHelper(NFA<S, I> nfa, Collection<? extends I> inputs) {
        Set<S> found = new HashSet<>();
        Deque<S> stack = new ArrayDeque<>();

        for (S init : nfa.getInitialStates()) {
            stack.push(init);
            found.add(init);
        }

        while (!stack.isEmpty()) {
            S curr = stack.pop();
            for (I sym : inputs) {
                for (S succState : nfa.getSuccessors(curr, sym)) {
                    if (found.add(succState)) {
                        // Add states to stack if they haven't been visited
                        stack.push(succState);
                    }
                }
            }
        }
        return found;
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
            Automata.invasiveMinimize(out, inputs);
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

        // Add union of initial states to DFA and to stack
        final SI init = powerset.getInitialState();
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

                if (!partial || succ != null) {
                    SO outSucc = outStateMap.get(succ);
                    if (outSucc == null) {
                        // add new state to DFA and to stack
                        outSucc = out.addState(powerset.isAccepting(succ));
                        outStateMap.put(succ, outSucc);
                        stack.push(new DeterminizeRecord<>(succ, outSucc));
                    }
                    out.setTransition(outState, sym, outSucc);
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
