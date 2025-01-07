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
package net.automatalib.util.partitionrefinement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.NFA;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for initializing {@link Valmari} objects from various automaton types.
 */
public final class ValmariInitializers {

    private ValmariInitializers() {
        // prevent instantiation
    }

    /**
     * Initializes the partition refinement data structure using the structural information from the given NFA and its
     * state acceptance as classifier for the initial partition.
     *
     * @param nfa
     *         the automaton from which to extract the relational information
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return the initialized partition refinement data structure
     */
    public static <S, I, A extends NFA<S, I> & InputAlphabetHolder<I>> Valmari initializeNFA(A nfa) {
        return initializeNFA(nfa, nfa.getInputAlphabet());
    }

    /**
     * Initializes the partition refinement data structure using the structural information from the given NFA and its
     * state acceptance as classifier for the initial partition.
     *
     * @param nfa
     *         the automaton from which to extract the relational information
     * @param alphabet
     *         the input symbols to consider
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     *
     * @return the initialized partition refinement data structure
     */
    public static <S, I> Valmari initializeNFA(NFA<S, I> nfa, Alphabet<I> alphabet) {
        return initializeUniversal(nfa, alphabet, nfa::getStateProperty);
    }

    /**
     * Initializes the partition refinement data structure using the structural information from the given automaton and
     * the given classifier for the initial partition.
     *
     * @param automaton
     *         the automaton from which to extract the relational information
     * @param alphabet
     *         the input symbols to consider
     * @param initialClassifier
     *         an extractor that for each automaton state gives a signature that identifies the initial partition block
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     *
     * @return the initialized partition refinement data structure
     */
    public static <S, I, T> Valmari initializeUniversal(UniversalAutomaton<S, I, T, ?, ?> automaton,
                                                        Alphabet<I> alphabet,
                                                        Function<? super S, ?> initialClassifier) {
        final int n = automaton.size();
        final int k = alphabet.size();

        final int[] blocks = new int[n];
        final StateIDs<S> stateIDs = automaton.stateIDs();
        final Map<@Nullable Object, Integer> signatures = new HashMap<>();

        int m = 0;
        int cnt = 0;

        for (int i = 0; i < n; i++) {
            final S s = stateIDs.getState(i);
            final Object sig = initialClassifier.apply(s);
            Integer id = signatures.get(sig);
            if (id == null) {
                id = cnt;
                signatures.put(sig, cnt++);
            }
            blocks[i] = id;

            for (int j = 0; j < k; j++) {
                m += automaton.getTransitions(s, alphabet.getSymbol(j)).size();
            }
        }

        final int[] tail = new int[m];
        final int[] label = new int[m];
        final int[] head = new int[m];

        cnt = 0;

        for (int i = 0; i < k; i++) {
            final I symbol = alphabet.getSymbol(i);
            for (int j = 0; j < n; j++) {
                final S state = stateIDs.getState(j);
                for (T t : automaton.getTransitions(state, symbol)) {
                    final S succ = automaton.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);

                    tail[cnt] = j;
                    label[cnt] = i;
                    head[cnt] = succId;
                    cnt++;
                }
            }
        }

        return new Valmari(blocks, tail, label, head);
    }
}
