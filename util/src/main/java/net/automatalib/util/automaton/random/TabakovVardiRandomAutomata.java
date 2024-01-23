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
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.common.util.random.RandomUtil;

public final class TabakovVardiRandomAutomata {
    private TabakovVardiRandomAutomata() {
        // prevent instantiation
    }

    /**
     * Generate random NFA using Tabakov and Vardi's approach, described in the paper
     * <a href="https://doi.org/10.1007/11591191_28">Experimental Evaluation of Classical Automata Constructions</a>
     * by Deian Tabakov and Moshe Y. Vardi.
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
     *      alphabet
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <I> CompactNFA<I> generateNFA(
            Random r, int size, float td, float ad, Alphabet<I> alphabet) {
        return generateNFA(r, size, Math.round(td * size), Math.round(ad * size), alphabet);
    }

    /**
     * Generate random NFA, with fixed number of accept states and edges (per letter).
     *
     * @param r
     *      random instance
     * @param size
     *      number of states
     * @param edgeNum
     *      number of edges (per letter)
     * @param acceptNum
     *      number of accepting states (at least one)
     * @param alphabet
     *      alphabet
     * @return
     *      a random NFA, not necessarily connected
     */
    public static <I> CompactNFA<I> generateNFA(
            Random r, int size, int edgeNum, int acceptNum, Alphabet<I> alphabet) {
        assert acceptNum > 0 && acceptNum <= size;
        assert edgeNum >= 0 && edgeNum <= size*size;

        CompactNFA<I> nfa = basicNFA(size, alphabet);

        // Set final states other than the initial state.
        // We want exactly acceptNum-1 of them, from the elements [1,size).
        // This works even if acceptNum == 1.
        int[] finalStates = RandomUtil.distinctIntegers(r, acceptNum - 1, 1, size);
        for (int f : finalStates) {
            nfa.setAccepting(f, true);
        }

        // For each letter, add edgeNum transitions.
        for (I a: alphabet) {
            for (int edgeIndex: RandomUtil.distinctIntegers(r, edgeNum, size*size)) {
                nfa.addTransition(edgeIndex / size, a, edgeIndex % size);
            }
        }

        return nfa;
    }

    // Helper method to generate NFA with initial accepting state.
    private static <I> CompactNFA<I> basicNFA(int size, Alphabet<I> alphabet) {
        CompactNFA<I> basicNFA = new CompactNFA<>(alphabet);

        // Create states
        for (int i = 0; i < size; i++) {
            basicNFA.addState(false);
        }
        // per the paper, the first state is always initial and accepting
        basicNFA.setInitial(0, true);
        basicNFA.setAccepting(0, true);
        return basicNFA;
    }
}

