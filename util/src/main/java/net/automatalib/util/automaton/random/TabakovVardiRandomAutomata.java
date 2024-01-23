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
package net.automatalib.util.automaton.random;

import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.random.RandomUtil;

/**
 * A generator for random {@link NFA}s as described in the paper
 * <a href="https://doi.org/10.1007/11591191_28">Experimental Evaluation of Classical Automata Constructions</a>
 * by Deian Tabakov and Moshe Y&nbsp;Vardi.
 */
public final class TabakovVardiRandomAutomata {
    private TabakovVardiRandomAutomata() {
        // prevent instantiation
    }

    /**
     * Generates a random {@link NFA} with the given size, transition density, and acceptance density.
     *
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param td
     *      transition density, in [0,size]
     * @param ad
     *      acceptance density, in (0,1]. 0.5 is the usual value
     * @param alphabet
     *      the input symbols to consider when determining successors
     * @param <I>
     *     input symbol type
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <I> CompactNFA<I> generateNFA(
            Random r, int size, float td, float ad, Alphabet<I> alphabet) {
        return generateNFA(r, size, Math.round(td * size), Math.round(ad * size), alphabet);
    }

    /**
     * Generates a random {@link NFA} with the given size, number of edges (per letter), number of accepting states.
     *
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param edgeNum
     *      number of edges (per input)
     * @param acceptNum
     *      number of accepting states (at least one)
     * @param alphabet
     *      the input symbols to consider when determining successors
     * @param <I>
     *     input symbol type
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <I> CompactNFA<I> generateNFA(
            Random r, int size, int edgeNum, int acceptNum, Alphabet<I> alphabet) {
        return generateNFA(r, size, edgeNum, acceptNum, alphabet, new CompactNFA<>(alphabet));
    }

    /**
     * Generates a random NFA with the given size, number of edges (per letter), and number of accepting states, written to the given {@link MutableNFA}.
     * Note that the output automaton must be empty.
     *
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param edgeNum
     *      number of edges (per input)
     * @param acceptNum
     *      number of accepting states (at least one)
     * @param alphabet
     *      the input symbols to consider when determining successors
     * @param out
     *      the (mutable) automaton to write the random structure to
     * @param <S>
     *     state type
     * @param <I>
     *     input symbol type
     * @param <A>
     *     automaton type
     * @return
     *      the {@code out} parameter after the contents have been written to it
     */
    public static <S, I, A extends MutableNFA<S, I>> A generateNFA(
            Random r, int size, int edgeNum, int acceptNum, Alphabet<I> alphabet, A out) {
        assert acceptNum > 0 && acceptNum <= size;
        assert edgeNum >= 0 && edgeNum <= size*size;

        initNFA(size, out);
        final StateIDs<S> stateIDs = out.stateIDs();

        // Set final states other than the initial state.
        // We want exactly acceptNum-1 of them, from the elements [1,size).
        // This works even if acceptNum == 1.
        int[] finalStates = RandomUtil.distinctIntegers(r, acceptNum - 1, 1, size);
        for (int f : finalStates) {
            out.setAccepting(stateIDs.getState(f), true);
        }

        // For each letter, add edgeNum transitions.
        for (I a: alphabet) {
            for (int edgeIndex: RandomUtil.distinctIntegers(r, edgeNum, size*size)) {
                out.addTransition(stateIDs.getState(edgeIndex / size), a, stateIDs.getState(edgeIndex % size));
            }
        }

        return out;
    }

    // Helper method to generate NFA with initial accepting state.
    private static <S, A extends MutableNFA<S, ?>> void initNFA(int size, A nfa) {
        assert nfa.size() == 0;

        // per the paper, the first state is always initial and accepting
        final S init = nfa.addInitialState(true);
        nfa.setAccepting(init, true);

        // Create remaining states
        for (int i = 1; i < size; i++) {
            nfa.addState(false);
        }
    }
}

