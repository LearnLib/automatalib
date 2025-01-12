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
package net.automatalib.util.partitionrefinement;

import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;

/**
 * Utility methods for extracting various automaton types from {@link Valmari} objects.
 */
public final class ValmariExtractors {

    private ValmariExtractors() {
        // prevent instantiation
    }

    /**
     * Extracts from the given partition refinement data structure the "quotiented" NFA of the original one.
     * Automatically prunes states that are unreachable in the original automaton.
     *
     * @param valmari
     *         the partition refinement data structure
     * @param original
     *         the original automaton from which the initial partitioning has been constructed
     * @param alphabet
     *         the input symbols to consider when constructing the new automaton
     * @param <I>
     *         input symbol type
     *
     * @return the "quotiented" automaton of the original one
     */
    public static <I> CompactNFA<I> toNFA(Valmari valmari, NFA<?, I> original, Alphabet<I> alphabet) {
        return toNFA(valmari, original, alphabet, true);
    }

    /**
     * Extracts from the given partition refinement data structure the "quotiented" NFA of the original one.
     *
     * @param valmari
     *         the partition refinement data structure
     * @param original
     *         the original automaton from which the initial partitioning has been constructed
     * @param alphabet
     *         the input symbols to consider when constructing the new automaton
     * @param pruneUnreachable
     *         a flag indicating whether unreachable states (in the original automaton) should be pruned during
     *         construction.
     * @param <I>
     *         input symbol type
     *
     * @return the "quotiented" automaton of the original one
     */
    public static <I> CompactNFA<I> toNFA(Valmari valmari,
                                          NFA<?, I> original,
                                          Alphabet<I> alphabet,
                                          boolean pruneUnreachable) {
        return toNFA(valmari, original, alphabet, pruneUnreachable, new CompactNFA.Creator<>());
    }

    /**
     * Extracts from the given partition refinement data structure the "quotiented" NFA of the original one.
     *
     * @param valmari
     *         the partition refinement data structure
     * @param original
     *         the original automaton from which the initial partitioning has been constructed
     * @param alphabet
     *         the input symbols to consider when constructing the new automaton
     * @param creator
     *         the provider of the new automaton instance
     * @param pruneUnreachable
     *         a flag indicating whether unreachable states (in the original automaton) should be pruned during
     *         construction.
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return the "quotiented" automaton of the original one
     */
    public static <S, I, A extends MutableNFA<S, I>> A toNFA(Valmari valmari,
                                                             NFA<?, I> original,
                                                             Alphabet<I> alphabet,
                                                             boolean pruneUnreachable,
                                                             AutomatonCreator<A, I> creator) {
        return toUniversal(valmari, original, alphabet, creator, pruneUnreachable);
    }

    /**
     * Extracts from the given partition refinement data structure the "quotiented" automaton of the original one, using
     * its state and transition properties. Automatically prunes states that are unreachable in the original automaton.
     *
     * @param valmari
     *         the partition refinement data structure
     * @param original
     *         the original automaton from which the initial partitioning has been constructed
     * @param alphabet
     *         the input symbols to consider when constructing the new automaton
     * @param creator
     *         the provider of the new automaton instance
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     * @param <A>
     *         automaton type
     *
     * @return the "quotiented" automaton of the original one
     */
    public static <S, I, SP, TP, A extends MutableAutomaton<S, I, ?, SP, TP>> A toUniversal(Valmari valmari,
                                                                                            UniversalAutomaton<?, I, ?, SP, TP> original,
                                                                                            Alphabet<I> alphabet,
                                                                                            AutomatonCreator<A, I> creator) {
        return toUniversal(valmari, original, alphabet, creator, true);
    }

    /**
     * Extracts from the given partition refinement data structure the "quotiented" automaton of the original one, using
     * its state and transition properties.
     *
     * @param valmari
     *         the partition refinement data structure
     * @param original
     *         the original automaton from which the initial partitioning has been constructed
     * @param alphabet
     *         the input symbols to consider when constructing the new automaton
     * @param creator
     *         the provider of the new automaton instance
     * @param pruneUnreachable
     *         a flag indicating whether unreachable states (in the original automaton) should be pruned during
     *         construction.
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     * @param <A>
     *         automaton type
     *
     * @return the "quotiented" automaton of the original one
     */
    public static <S, I, SP, TP, A extends MutableAutomaton<S, I, ?, SP, TP>> A toUniversal(Valmari valmari,
                                                                                            UniversalAutomaton<?, I, ?, SP, TP> original,
                                                                                            Alphabet<I> alphabet,
                                                                                            AutomatonCreator<A, I> creator,
                                                                                            boolean pruneUnreachable) {
        return pruneUnreachable ?
                toUniversalPrune(valmari, original, alphabet, creator) :
                toUniversalNoPrune(valmari, original, alphabet, creator);
    }

    private static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, ? super SP, ? super TP>> A toUniversalPrune(
            Valmari valmari,
            UniversalAutomaton<S1, I, T, SP, TP> original,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator) {

        final int numBlocks = valmari.blocks.sets + 1;

        @SuppressWarnings("unchecked") // we only put S2s
        final S1[] stateMap = (S1[]) new Object[numBlocks];
        @SuppressWarnings("unchecked") // we only put S2s
        final S2[] repMap = (S2[]) new Object[numBlocks];
        final A result = creator.createAutomaton(alphabet, numBlocks);
        final StateIDs<S1> stateIDs = original.stateIDs();

        int idx = 0;
        for (S1 initialState : original.getInitialStates()) {
            final int id = stateIDs.getStateId(initialState);
            final int blockId = valmari.blocks.sidx[id];

            if (repMap[blockId] == null) {
                final SP sp = original.getStateProperty(initialState);
                final S2 init = result.addInitialState(sp);
                stateMap[idx++] = initialState;
                repMap[blockId] = init;
            }
        }

        int statesPtr = 0;
        int numStates = idx;

        while (statesPtr < numStates) {
            final int resState = statesPtr++;
            final S1 rep = stateMap[resState];
            final int blockId = valmari.blocks.sidx[stateIDs.getStateId(rep)];
            final S2 src = repMap[blockId];

            for (I sym : alphabet) {
                for (T t : original.getTransitions(rep, sym)) {
                    final S1 succ = original.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);
                    final int succBlockId = valmari.blocks.sidx[succId];
                    S2 tgt = repMap[succBlockId];

                    if (tgt == null) {
                        final SP sp = original.getStateProperty(succ);
                        tgt = result.addState(sp);
                        repMap[succBlockId] = tgt;
                        stateMap[numStates++] = succ;
                    }

                    result.addTransition(src, sym, tgt, original.getTransitionProperty(t));
                }
            }
        }

        return result;
    }

    private static <S1, S2, I, T, SP, TP, A extends MutableAutomaton<S2, I, ?, ? super SP, ? super TP>> A toUniversalNoPrune(
            Valmari valmari,
            UniversalAutomaton<S1, I, T, SP, TP> original,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator) {
        final int numBlocks = valmari.blocks.sets + 1;

        @SuppressWarnings("unchecked") // we only put S2s
        final S2[] stateArray = (S2[]) new Object[numBlocks];
        final A result = creator.createAutomaton(alphabet, numBlocks);
        final StateIDs<S1> stateIDs = original.stateIDs();
        final Set<S1> initialStates = original.getInitialStates();

        for (int i = 0; i < numBlocks; i++) {
            final int origId = valmari.blocks.elems[valmari.blocks.first[i]];
            final S1 s = stateIDs.getState(origId);

            if (blockContainsInitialState(valmari, i, stateIDs, initialStates)) {
                stateArray[i] = result.addInitialState(original.getStateProperty(s));
            } else {
                stateArray[i] = result.addState(original.getStateProperty(s));
            }
        }

        for (int i = 0; i < numBlocks; i++) {
            final int origId = valmari.blocks.elems[valmari.blocks.first[i]];
            final S1 s = stateIDs.getState(origId);
            for (I sym : alphabet) {
                for (T t : original.getTransitions(s, sym)) {
                    final S1 succ = original.getSuccessor(t);
                    final int succId = stateIDs.getStateId(succ);
                    final int blockId = valmari.blocks.sidx[succId];

                    result.addTransition(stateArray[i], sym, stateArray[blockId], original.getTransitionProperty(t));
                }
            }
        }

        return result;
    }

    private static <S> boolean blockContainsInitialState(Valmari valmari, int blockId, StateIDs<S> stateIDs, Set<S> initialStates) {
        for (int i = valmari.blocks.first[blockId]; i < valmari.blocks.end[blockId]; i++) {
            final S s = stateIDs.getState(valmari.blocks.elems[i]);
            if (initialStates.contains(s)) {
                return true;
            }
        }

        return false;
    }

}
