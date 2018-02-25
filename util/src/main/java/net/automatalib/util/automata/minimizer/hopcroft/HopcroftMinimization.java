/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.util.automata.minimizer.hopcroft;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.partitionrefinement.PaigeTarjan;
import net.automatalib.util.partitionrefinement.PaigeTarjanExtractors;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers;
import net.automatalib.util.partitionrefinement.PaigeTarjanInitializers.AutomatonInitialPartitioning;
import net.automatalib.words.Alphabet;

/**
 * Versions of Hopcroft's minimization algorithm for deterministic finite automata.
 * <p>
 * Hopcroft's algorithm is a special case of the Paige/Tarjan partition refinement algorithm (see {@link PaigeTarjan})
 * for the case of deterministic automata. Its running time is {@code O(nk log n)}, where {@code n} is the size of the
 * input DFA and {@code k} the size of the input alphabet.
 * <p>
 * <b>Important note:</b> Hopcroft's minimization algorithm works for complete automata only. If the automaton is
 * partial, an explicit sink must be inserted. This is done on-the-fly when using one of the {@code minimizePartial...}
 * methods. If any other method is invoked with a partial automaton as its argument, this will result in a {@link
 * IllegalArgumentException} at runtime.
 * <p>
 * Note that the partition refinement step only calculates classes of equivalent states. However, minimization also
 * requires pruning of states that cannot be reached from the initial states. Most methods in this class have a variable
 * called {@code pruningMode} of type {@link PruningMode} that controls if and when pruning is performed: if the
 * automaton to be minimized is known to be <i>initially connected</i> (i.e., it contains no unreachable states),
 * pruning can be omitted completely (by specifying {@link PruningMode#DONT_PRUNE}) without impairing correctness.
 * Otherwise, pruning can be chosen to be performed on the automaton to be minimized ({@link PruningMode#PRUNE_BEFORE}),
 * or on the calculated state partition ({@link PruningMode#PRUNE_AFTER}). For methods that do not provide a {@code
 * pruningMode} parameter, the default is {@link PruningMode#PRUNE_AFTER}.
 *
 * @author Malte Isberner
 */
public final class HopcroftMinimization {

    private HopcroftMinimization() {
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}, and pruning (see
     * above) is performed after computing state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy, Alphabet<I> alphabet) {
        return minimizeMealy(mealy, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param pruningMode
     *         the pruning mode (see above)
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy,
                                                          Alphabet<I> alphabet,
                                                          PruningMode pruningMode) {
        return doMinimizeMealy(mealy, alphabet, new CompactMealy.Creator<>(), pruningMode);
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}, using the
     * alphabet obtained via <code>mealy.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>.
     * Pruning (see above) is performed after computing state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <S, I, T, O, A extends MealyMachine<S, I, T, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizeMealy(A mealy) {
        return minimizeMealy(mealy, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given Mealy machine. The result is returned in the form of a {@link CompactMealy}, using the
     * alphabet obtained via <code>mealy.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param pruningMode
     *         the pruning mode (see above)
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <S, I, T, O, A extends MealyMachine<S, I, T, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizeMealy(
            A mealy,
            PruningMode pruningMode) {
        return doMinimizeMealy(mealy, mealy.getInputAlphabet(), new CompactMealy.Creator<>(), pruningMode);
    }

    private static <S, I, T, O, A extends MutableDeterministic<?, I, ?, Void, O>> A doMinimizeMealy(MealyMachine<S, I, T, O> mealy,
                                                                                                    Alphabet<I> alphabet,
                                                                                                    AutomatonCreator<? extends A, I> creator,
                                                                                                    PruningMode pruning) {

        PaigeTarjan pt = new PaigeTarjan();

        UniversalDeterministicAutomaton.FullIntAbstraction<T, Void, O> abs = mealy.fullIntAbstraction(alphabet);
        PaigeTarjanInitializers.initCompleteDeterministic(pt,
                                                          abs,
                                                          AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                                                          pruning == PruningMode.PRUNE_BEFORE);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        return PaigeTarjanExtractors.toDeterministic(pt,
                                                     creator,
                                                     alphabet,
                                                     abs,
                                                     null,
                                                     abs::getTransitionProperty,
                                                     pruning == PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}, and pruning (see above) is
     * performed after computing state equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizeDFA(DFA<?, I> dfa, Alphabet<I> alphabet) {
        return minimizeDFA(dfa, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param pruningMode
     *         the pruning mode (see above)
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizeDFA(DFA<?, I> dfa, Alphabet<I> alphabet, PruningMode pruningMode) {
        return doMinimizeDFA(dfa, alphabet, new CompactDFA.Creator<>(), pruningMode);
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}, using the input alphabet
     * obtained via <code>dfa.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>. Pruning (see
     * above) is performed after computing state equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     *
     * @return a minimized version of the specified DFA
     */
    public static <S, I, A extends DFA<S, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizeDFA(A dfa) {
        return minimizeDFA(dfa, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given DFA. The result is returned in the form of a {@link CompactDFA}, using the input alphabet
     * obtained via <code>dfa.{@link InputAlphabetHolder#getInputAlphabet() getInputAlphabet()}</code>.
     *
     * @param dfa
     *         the DFA to minimize
     * @param pruningMode
     *         the pruning mode (see above)
     *
     * @return a minimized version of the specified DFA
     */
    public static <S, I, A extends DFA<S, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizeDFA(A dfa,
                                                                                                 PruningMode pruningMode) {
        return doMinimizeDFA(dfa, dfa.getInputAlphabet(), new CompactDFA.Creator<>(), pruningMode);
    }

    private static <S, I, A extends MutableDeterministic<?, I, ?, Boolean, Void>> A doMinimizeDFA(DFA<S, I> dfa,
                                                                                                  Alphabet<I> alphabet,
                                                                                                  AutomatonCreator<A, I> creator,
                                                                                                  PruningMode pruning) {

        PaigeTarjan pt = new PaigeTarjan();
        UniversalDeterministicAutomaton.FullIntAbstraction<?, Boolean, Void> absDfa = dfa.fullIntAbstraction(alphabet);
        PaigeTarjanInitializers.initCompleteDeterministic(pt,
                                                          absDfa,
                                                          AutomatonInitialPartitioning.BY_STATE_PROPERTY,
                                                          pruning == PruningMode.PRUNE_BEFORE);

        pt.initWorklist(false);
        pt.computeCoarsestStablePartition();

        return PaigeTarjanExtractors.toDeterministic(pt,
                                                     creator,
                                                     alphabet,
                                                     absDfa,
                                                     absDfa::getStateProperty,
                                                     null,
                                                     pruning == PruningMode.PRUNE_AFTER);
    }

    /**
     * Allows for controlling how automata are pruned during minimization.
     *
     * @author Malte Isberner
     */
    public enum PruningMode {
        /**
         * Prune the automaton <i>before</i> the computation of equivalent states. This might be more efficient if the
         * automaton contains a large number of unreachable states, as it reduces the number of states on which
         * equivalence needs to be computed. However, since the equivalence computation is practically extremely fast,
         * {@link #PRUNE_AFTER} is usually the better choice. This value, however, always guarantees a correct (i.e.,
         * minimal and initially connected) result.
         */
        PRUNE_BEFORE,
        /**
         * Prune after the computation of equivalent states. Since the number of equivalence classes is usually smaller
         * than the number of states of the original automaton, this usually is more efficient (unless the automaton
         * contains many unreachable states), and guarantees a correct result.
         */
        PRUNE_AFTER,
        /**
         * Do not prune at all. Note that if the automaton to minimize is not initially connected (i.e., there are
         * states which cannot be reached from the initial state), the returned automaton might or might not be
         * initially connected, meaning it is possibly non-minimal.
         */
        DONT_PRUNE
    }

}
