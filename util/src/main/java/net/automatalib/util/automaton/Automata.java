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
package net.automatalib.util.automaton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.graph.Graph;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.util.automaton.equivalence.CharacterizingSets;
import net.automatalib.util.automaton.equivalence.DeterministicEquivalenceTest;
import net.automatalib.util.automaton.equivalence.NearLinearEquivalenceTest;
import net.automatalib.util.automaton.minimizer.HopcroftMinimizer;
import net.automatalib.util.minimizer.Block;
import net.automatalib.util.minimizer.MinimizationResult;
import net.automatalib.util.minimizer.Minimizer;
import net.automatalib.util.ts.TS;
import net.automatalib.util.ts.TS.TransRef;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Automata {

    private Automata() {
        // prevent instantiation
    }

    public static <S, I, T> Graph<S, TransitionEdge<I, T>> asGraph(Automaton<S, I, T> automaton,
                                                                   Collection<? extends I> inputs) {
        return automaton.transitionGraphView(inputs);
    }

    /**
     * Minimizes the given automaton. Internally, this method delegates to the
     * {@link Minimizer Beal-Crochemore algorithm} which performs well especially for partial automata. If you have a
     * total (or very dense) automaton, {@link HopcroftMinimizer} may provide better performance.
     *
     * @param automaton
     *         the automaton to minimize
     * @param inputs
     *         the inputs to consider for minimization
     * @param output
     *         the object to write the minimized automaton to
     * @param <S>
     *         (input) state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         (input) transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     * @param <SO>
     *         (output) state type
     * @param <TO>
     *         (output) transition type
     * @param <A>
     *         explicit automaton type
     *
     * @return {@code output}, for convenience
     */
    public static <S, I, T, SP, TP, SO, TO, A extends MutableDeterministic<SO, ? super I, TO, ? super SP, ? super TP>> A minimize(
            UniversalDeterministicAutomaton<S, I, T, SP, TP> automaton,
            Collection<? extends I> inputs,
            A output) {

        UniversalGraph<S, TransitionEdge<I, T>, SP, TransitionEdge.Property<I, TP>> aag =
                automaton.transitionGraphView(inputs);

        MinimizationResult<S, TransitionEdge.Property<I, TP>> mr =
                Minimizer.minimize(aag, automaton.getInitialStates());
        output.clear();

        S init = automaton.getInitialState();
        Block<S, TransitionEdge.Property<I, TP>> initBlock = init == null ? null : mr.getBlockForState(init);
        ArrayStorage<SO> storage = new ArrayStorage<>(mr.getNumBlocks());

        for (Block<S, TransitionEdge.Property<I, TP>> block : mr.getBlocks()) {
            S rep = mr.getRepresentative(block);
            SO state;
            SP repProp = automaton.getStateProperty(rep);
            if (block == initBlock) {
                state = output.addInitialState(repProp);
            } else {
                state = output.addState(repProp);
            }
            storage.set(block.getId(), state);
        }

        for (Block<S, TransitionEdge.Property<I, TP>> block : mr.getBlocks()) {
            S rep = mr.getRepresentative(block);
            SO state = storage.get(block.getId());
            for (I input : inputs) {
                T trans = automaton.getTransition(rep, input);
                if (trans != null) {
                    TP prop = automaton.getTransitionProperty(trans);
                    S oldSucc = automaton.getSuccessor(trans);
                    Block<S, TransitionEdge.Property<I, TP>> succBlock = mr.getBlockForState(oldSucc);
                    SO newSucc = storage.get(succBlock.getId());
                    output.addTransition(state, input, newSucc, prop);
                }
            }
        }
        return output;
    }

    /**
     * Minimizes the given automaton in-place. Internally, this method delegates to the
     * {@link Minimizer Beal-Crochemore algorithm} which performs well especially for partial automata. If you have a
     * total automaton, {@link HopcroftMinimizer} may provide better performance.
     *
     * @param automaton
     *         the automaton to minimize
     * @param inputs
     *         the inputs to consider for minimization
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <T>
     *         transition type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     * @param <A>
     *         automaton type
     *
     * @return {@code automaton}, for convenience
     */
    public static <S, I, T, SP, TP, A extends MutableDeterministic<S, I, T, SP, TP>> A invasiveMinimize(A automaton,
                                                                                                        Collection<? extends I> inputs) {

        final List<? extends I> inputList = CollectionUtil.randomAccessList(inputs);

        int numInputs = inputs.size();

        UniversalGraph<S, TransitionEdge<I, T>, SP, TransitionEdge.Property<I, TP>> aag =
                automaton.transitionGraphView(inputs);

        MinimizationResult<S, TransitionEdge.Property<I, TP>> mr =
                Minimizer.minimize(aag, automaton.getInitialStates());

        S init = automaton.getInitialState();
        int initId = init == null ? -1 : mr.getBlockForState(init).getId();

        @SuppressWarnings("unchecked")
        ResultStateRecord<SP, TP>[] records = new ResultStateRecord[mr.getNumBlocks()];

        // Store minimized automaton data in the records array
        for (Block<S, TransitionEdge.Property<I, TP>> blk : mr.getBlocks()) {
            int id = blk.getId();
            S state = mr.getRepresentative(blk);
            SP prop = automaton.getStateProperty(state);
            ResultStateRecord<SP, TP> rec = new ResultStateRecord<>(numInputs, prop);
            records[id] = rec;
            for (int i = 0; i < numInputs; i++) {
                I input = inputList.get(i);
                T trans = automaton.getTransition(state, input);
                if (trans == null) {
                    continue;
                }

                TP transProp = automaton.getTransitionProperty(trans);
                S succ = automaton.getSuccessor(trans);
                int tgtId = mr.getBlockForState(succ).getId();
                rec.transitions[i] = new ResultTransRecord<>(tgtId, transProp);
            }
        }

        automaton.clear();

        // Add states from records
        @Nullable Object[] states = new Object[records.length];
        for (int i = 0; i < records.length; i++) {
            ResultStateRecord<SP, TP> rec = records[i];
            SP prop = rec.property;
            S state;
            if (i == initId) {
                state = automaton.addInitialState(prop);
            } else {
                state = automaton.addState(prop);
            }
            states[i] = state;
        }

        // Add transitions from records
        for (int i = 0; i < records.length; i++) {
            ResultStateRecord<SP, TP> rec = records[i];
            @SuppressWarnings("unchecked")
            S state = (S) states[i];

            for (int j = 0; j < numInputs; j++) {
                ResultTransRecord<TP> transRec = rec.transitions[j];
                if (transRec == null) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                S succ = (S) states[transRec.targetId];
                I input = inputList.get(j);
                automaton.addTransition(state, input, succ, transRec.property);
            }
        }
        return automaton;
    }

    /**
     * Tests whether two automata are equivalent, i.e. whether there exists a
     * {@link #findSeparatingWord(UniversalDeterministicAutomaton, UniversalDeterministicAutomaton, Collection)
     * separating word} for the two given automata.
     *
     * @param <I>
     *         input symbol type
     * @param reference
     *         the one automaton to consider
     * @param other
     *         the other automaton to consider
     * @param inputs
     *         the input symbols to consider
     *
     * @return {@code true} if the automata are equivalent, {@code false} otherwise.
     *
     * @see #findSeparatingWord(UniversalDeterministicAutomaton, UniversalDeterministicAutomaton, Collection)
     */
    public static <I> boolean testEquivalence(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference,
                                              UniversalDeterministicAutomaton<?, I, ?, ?, ?> other,
                                              Collection<? extends I> inputs) {
        return findSeparatingWord(reference, other, inputs) == null;
    }

    /**
     * Finds a separating word for two automata. A separating word is a word that exposes a difference (differing state
     * or transition properties, or a transition undefined in only one of the automata) between the two automata.
     *
     * @param <I>
     *         input symbol type
     * @param reference
     *         the one automaton to consider
     * @param other
     *         the other automaton to consider
     * @param inputs
     *         the input symbols to consider
     *
     * @return a separating word, or {@code null} if no such word could be found.
     */
    public static <I> @Nullable Word<I> findSeparatingWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference,
                                                           UniversalDeterministicAutomaton<?, I, ?, ?, ?> other,
                                                           Collection<? extends I> inputs) {
        return NearLinearEquivalenceTest.findSeparatingWord(reference, other, inputs);
    }

    /**
     * Finds a separating word for two states in an automaton. A separating word is a word that exposes a difference
     * (differing state or transition properties, or a transition undefined in only one of the paths) between the two
     * states.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param automaton
     *         the automaton containing the states
     * @param state1
     *         the one state
     * @param state2
     *         the other state
     * @param inputs
     *         the input symbols to consider
     *
     * @return a separating word, or {@code null} if no such word could be found
     */
    public static <S, I> @Nullable Word<I> findSeparatingWord(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                              S state1,
                                                              S state2,
                                                              Collection<? extends I> inputs) {
        return NearLinearEquivalenceTest.findSeparatingWord(automaton, state1, state2, inputs);
    }

    /**
     * Finds a shortest separating word for two automata. A separating word is a word that exposes a difference
     * (differing state or transition properties, or a transition undefined in only one of the automata) between the two
     * automata.
     *
     * @param <I>
     *         input symbol type
     * @param reference
     *         the one automaton to consider
     * @param other
     *         the other automaton to consider
     * @param inputs
     *         the input symbols to consider
     *
     * @return a separating word, or {@code null} if no such word could be found.
     */
    public static <I> @Nullable Word<I> findShortestSeparatingWord(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference,
                                                                   UniversalDeterministicAutomaton<?, I, ?, ?, ?> other,
                                                                   Collection<? extends I> inputs) {
        return DeterministicEquivalenceTest.findSeparatingWord(reference, other, inputs);
    }

    /**
     * Computes a characterizing set, and returns it as a {@link List}.
     *
     * @param <I>
     *         input symbol type
     * @param automaton
     *         the automaton for which to determine the characterizing set
     * @param inputs
     *         the input symbols to consider
     *
     * @return a list containing the characterizing words
     *
     * @see CharacterizingSets
     */
    public static <I> List<Word<I>> characterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                                      Collection<? extends I> inputs) {
        List<Word<I>> result = new ArrayList<>();
        characterizingSet(automaton, inputs, result);
        return result;
    }

    /**
     * Computes a characterizing set for the given automaton.
     * <p>
     * This is a convenience method acting as a shortcut to
     * {@link CharacterizingSets#findCharacterizingSet(UniversalDeterministicAutomaton, Collection, Collection)}.
     *
     * @param <I>
     *         input symbol type
     * @param automaton
     *         the automaton for which to determine the characterizing set
     * @param inputs
     *         the input symbols to consider
     * @param result
     *         the collection in which to store the characterizing words
     *
     * @see CharacterizingSets
     */
    public static <I> void characterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                             Collection<? extends I> inputs,
                                             Collection<? super Word<I>> result) {
        CharacterizingSets.findCharacterizingSet(automaton, inputs, result);
    }

    public static <I> boolean incrementalCharacterizingSet(UniversalDeterministicAutomaton<?, I, ?, ?, ?> automaton,
                                                           Collection<? extends I> inputs,
                                                           Collection<? extends Word<I>> oldSuffixes,
                                                           Collection<? super Word<I>> newSuffixes) {
        return CharacterizingSets.findIncrementalCharacterizingSet(automaton, inputs, oldSuffixes, newSuffixes);
    }

    /**
     * Computes a characterizing set for a single state, and returns it as a {@link List}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param automaton
     *         the automaton containing the state
     * @param inputs
     *         the input symbols to consider
     * @param state
     *         the state for which to determine a characterizing set
     *
     * @return a list containing the characterizing words
     *
     * @see CharacterizingSets
     */
    public static <S, I> List<Word<I>> stateCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                              Collection<? extends I> inputs,
                                                              S state) {
        List<Word<I>> result = new ArrayList<>();
        stateCharacterizingSet(automaton, inputs, state, result);
        return result;
    }

    /**
     * Computes a characterizing set for a single state.
     * <p>
     * This is a convenience method acting as a shortcut to
     * {@link CharacterizingSets#findCharacterizingSet(UniversalDeterministicAutomaton, Collection, Object,
     * Collection)}.
     *
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param automaton
     *         the automaton containing the state
     * @param inputs
     *         the input symbols to consider
     * @param state
     *         the state for which to determine a characterizing set
     * @param result
     *         the collection in which to store the characterizing words
     *
     * @see CharacterizingSets
     */
    public static <S, I> void stateCharacterizingSet(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                     Collection<? extends I> inputs,
                                                     S state,
                                                     Collection<? super Word<I>> result) {
        CharacterizingSets.findCharacterizingSet(automaton, inputs, state, result);
    }

    /**
     * Convenient method for computing a state cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return the state cover for the given automaton
     *
     * @see Covers#stateCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> List<Word<I>> stateCover(DeterministicAutomaton<?, I, ?> automaton,
                                               Collection<? extends I> inputs) {
        final List<Word<I>> result = new ArrayList<>(automaton.size());
        Covers.stateCover(automaton, inputs, result);
        return result;
    }

    /**
     * Convenient method for computing a state cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return the transition cover for the given automaton
     *
     * @see Covers#transitionCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> List<Word<I>> transitionCover(DeterministicAutomaton<?, I, ?> automaton,
                                                    Collection<? extends I> inputs) {
        final List<Word<I>> result = new ArrayList<>(automaton.size() * inputs.size());
        Covers.transitionCover(automaton, inputs, result);
        return result;
    }

    /**
     * Convenient method for computing a structural cover.
     *
     * @param automaton
     *         the automaton for which the cover should be computed
     * @param inputs
     *         the set of input symbols allowed in the cover sequences
     * @param <I>
     *         input symbol type
     *
     * @return the structural cover for the given automaton
     *
     * @see Covers#structuralCover(DeterministicAutomaton, Collection, Collection)
     */
    public static <I> List<Word<I>> structuralCover(DeterministicAutomaton<?, I, ?> automaton,
                                                    Collection<? extends I> inputs) {
        final List<Word<I>> result = new ArrayList<>(automaton.size() * (inputs.size() + 1));
        Covers.structuralCover(automaton, inputs, result);
        return result;
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allDefinedInputsIterator(Automaton<S, I, ?> automaton,
                                                                              Iterable<? extends I> inputs) {
        return TS.allDefinedInputsIterator(automaton, automaton.iterator(), inputs);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allDefinedInputs(Automaton<S, I, ?> automaton,
                                                                      Iterable<? extends I> inputs) {
        return TS.allDefinedInputs(automaton, automaton, inputs);
    }

    public static <S, I> Iterable<TransRef<S, I, ?>> allUndefinedInputs(Automaton<S, I, ?> automaton,
                                                                        Iterable<? extends I> inputs) {
        return TS.allUndefinedTransitions(automaton, automaton, inputs);
    }

    public static <I> boolean hasUndefinedInput(Automaton<?, I, ?> automaton, Iterable<? extends I> inputs) {
        return allUndefinedInputsIterator(automaton, inputs).hasNext();
    }

    public static <S, I> Iterator<TransRef<S, I, ?>> allUndefinedInputsIterator(Automaton<S, I, ?> automaton,
                                                                                Iterable<? extends I> inputs) {
        return TS.allUndefinedTransitionsIterator(automaton, automaton.iterator(), inputs);
    }

    private static final class ResultTransRecord<TP> {

        public final int targetId;
        public final TP property;

        ResultTransRecord(int targetId, TP property) {
            this.targetId = targetId;
            this.property = property;
        }
    }

    private static final class ResultStateRecord<SP, TP> {

        public final SP property;
        public final ResultTransRecord<TP>[] transitions;

        @SuppressWarnings("unchecked")
        ResultStateRecord(int numInputs, SP property) {
            this.property = property;
            this.transitions = new ResultTransRecord[numInputs];
        }
    }
}
