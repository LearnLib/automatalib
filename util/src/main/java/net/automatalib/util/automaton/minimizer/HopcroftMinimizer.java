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
package net.automatalib.util.automaton.minimizer;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.minimizer.Minimizer;
import net.automatalib.util.partitionrefinement.AutomatonInitialPartitioning;
import net.automatalib.util.partitionrefinement.Hopcroft;
import net.automatalib.util.partitionrefinement.HopcroftExtractors;
import net.automatalib.util.partitionrefinement.HopcroftInitializers;
import net.automatalib.util.partitionrefinement.PruningMode;
import net.automatalib.util.partitionrefinement.StateSignature;

/**
 * Hopcroft's minimization algorithm for deterministic finite automata.
 * <p>
 * Hopcroft's algorithm solves the functional coarsest partition problem for finite automata in running time
 * {@code O(nk log n)}, where {@code n} is the size of the input DFA and {@code k} the size of the input alphabet.
 * <p>
 * <b>Important note:</b> Hopcroft's minimization algorithm works for complete automata only. If the automaton is
 * partial, you may use the {@link #minimizePartialDFA(DFA) minimizePartial...} utility methods that automatically add a
 * sink state when encountering an undefined transitions. If you have very sparse automata, {@link Minimizer} may yield
 * better performance.
 * <p>
 * Note that the partition refinement step only calculates classes of equivalent states. However, minimization also
 * requires pruning of states that cannot be reached from the initial state. Most methods in this class support a
 * variable of type {@link PruningMode} that controls if and when pruning is performed: if the automaton to be minimized
 * is known to be <i>initially connected</i> (i.e., it contains no unreachable states), pruning can be omitted
 * completely (by specifying {@link PruningMode#DONT_PRUNE}) without impairing correctness. Otherwise, pruning can be
 * chosen to be performed on the automaton to be minimized ({@link PruningMode#PRUNE_BEFORE}), or on the calculated
 * state partition ({@link PruningMode#PRUNE_AFTER}). Note that using {@link PruningMode#PRUNE_BEFORE} with the
 * {@link #minimizePartialDFA(DFA) minimizePartial...} methods may result in the artificial sink state being included in
 * the final automaton. For methods that do not provide a {@code pruningMode} parameter, the default is
 * {@link PruningMode#PRUNE_AFTER}.
 */
public final class HopcroftMinimizer {

    private HopcroftMinimizer() {}

    /**
     * Minimizes the given, complete DFA. The result is returned in the form of a {@link CompactDFA}, using the input
     * alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}. Pruning is performed after computing state
     * equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I, A extends DFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizeDFA(A dfa) {
        return minimizeDFA(dfa, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, complete DFA. The result is returned in the form of a {@link CompactDFA}, using the input
     * alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I, A extends DFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizeDFA(A dfa,
                                                                                              PruningMode pruningMode) {
        return minimizeDFA(dfa, dfa.getInputAlphabet(), pruningMode);
    }

    /**
     * Minimizes the given, complete DFA. The result is returned in the form of a {@link CompactDFA}, and pruning is
     * performed after computing state equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizeDFA(DFA<?, I> dfa, Alphabet<I> alphabet) {
        return minimizeDFA(dfa, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, complete DFA. The result is returned in the form of a {@link CompactDFA}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizeDFA(DFA<?, I> dfa, Alphabet<I> alphabet, PruningMode pruningMode) {
        return minimizeDFA(dfa, alphabet, pruningMode, new CompactDFA.Creator<>());
    }

    /**
     * Minimizes the given, complete DFA. The result is returned in the form of the automaton created by the given
     * creator.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param pruningMode
     *         the pruning mode
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param <A>
     *         automaton type
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <A extends MutableDFA<?, I>, I> A minimizeDFA(DFA<?, I> dfa,
                                                                Alphabet<I> alphabet,
                                                                PruningMode pruningMode,
                                                                AutomatonCreator<A, I> creator) {
        return minimizeUniversal(dfa, alphabet, creator, AutomatonInitialPartitioning.BY_STATE_PROPERTY, pruningMode);
    }

    /**
     * Minimizes the given, potentially partial DFA. The result is returned in the form of a {@link CompactDFA}, using
     * the input alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}. Pruning is performed after
     * computing state equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I, A extends DFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizePartialDFA(A dfa) {
        return minimizePartialDFA(dfa, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, potentially partial DFA. The result is returned in the form of a {@link CompactDFA}, using
     * the input alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I, A extends DFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> minimizePartialDFA(A dfa,
                                                                                                     PruningMode pruningMode) {
        return minimizePartialDFA(dfa, dfa.getInputAlphabet(), pruningMode);
    }

    /**
     * Minimizes the given, potentially partial DFA. The result is returned in the form of a {@link CompactDFA}, and
     * pruning is performed after computing state equivalences.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizePartialDFA(DFA<?, I> dfa, Alphabet<I> alphabet) {
        return minimizePartialDFA(dfa, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, potentially partial DFA. The result is returned in the form of a {@link CompactDFA}.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <I> CompactDFA<I> minimizePartialDFA(DFA<?, I> dfa, Alphabet<I> alphabet, PruningMode pruningMode) {
        return minimizePartialDFA(dfa, alphabet, pruningMode, new CompactDFA.Creator<>());
    }

    /**
     * Minimizes the given, potentially partial DFA. The result is returned in the form of the automaton created by the
     * given creator.
     *
     * @param dfa
     *         the DFA to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the returned DFA)
     * @param pruningMode
     *         the pruning mode
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param <A>
     *         automaton type
     * @param <I>
     *         input symbol type
     *
     * @return a minimized version of the specified DFA
     */
    public static <A extends MutableDFA<?, I>, I> A minimizePartialDFA(DFA<?, I> dfa,
                                                                       Alphabet<I> alphabet,
                                                                       PruningMode pruningMode,
                                                                       AutomatonCreator<A, I> creator) {
        return minimizePartialUniversal(dfa,
                                        alphabet,
                                        creator,
                                        AutomatonInitialPartitioning.BY_STATE_PROPERTY,
                                        Boolean.FALSE,
                                        pruningMode);
    }

    /**
     * Minimizes the given, complete Mealy machine. The result is returned in the form of a {@link CompactMealy}, using
     * the alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}. Pruning is performed after computing
     * state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O, A extends MealyMachine<?, I, ?, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizeMealy(A mealy) {
        return minimizeMealy(mealy, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, complete Mealy machine. The result is returned in the form of a {@link CompactMealy}, using
     * the alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O, A extends MealyMachine<?, I, ?, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizeMealy(A mealy,
                                                                                                                       PruningMode pruningMode) {
        return minimizeMealy(mealy, mealy.getInputAlphabet(), pruningMode);
    }

    /**
     * Minimizes the given, complete Mealy machine. The result is returned in the form of a {@link CompactMealy}, and
     * pruning is performed after computing state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy, Alphabet<I> alphabet) {
        return minimizeMealy(mealy, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, complete Mealy machine. The result is returned in the form of a {@link CompactMealy}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizeMealy(MealyMachine<?, I, ?, O> mealy,
                                                          Alphabet<I> alphabet,
                                                          PruningMode pruningMode) {
        return minimizeMealy(mealy, alphabet, pruningMode, new CompactMealy.Creator<>());
    }

    /**
     * Minimizes the given, complete Mealy machine. The result is returned in the form of the automaton created by the
     * given creator.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param pruningMode
     *         the pruning mode
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param <A>
     *         automaton type
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <A extends MutableMealyMachine<?, I, ?, O>, I, O> A minimizeMealy(MealyMachine<?, I, ?, O> mealy,
                                                                                    Alphabet<I> alphabet,
                                                                                    PruningMode pruningMode,
                                                                                    AutomatonCreator<A, I> creator) {
        return minimizeUniversal(mealy,
                                 alphabet,
                                 creator,
                                 AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                                 pruningMode);
    }

    /**
     * Minimizes the given, potentially partial Mealy machine. The result is returned in the form of a
     * {@link CompactMealy}, using the alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}. Pruning is
     * performed after computing state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O, A extends MealyMachine<?, I, ?, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizePartialMealy(
            A mealy) {
        return minimizePartialMealy(mealy, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, potentially partial Mealy machine. The result is returned in the form of a
     * {@link CompactMealy}, using the alphabet obtained via {@link InputAlphabetHolder#getInputAlphabet()}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     * @param <A>
     *         automaton type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O, A extends MealyMachine<?, I, ?, O> & InputAlphabetHolder<I>> CompactMealy<I, O> minimizePartialMealy(
            A mealy,
            PruningMode pruningMode) {
        return minimizePartialMealy(mealy, mealy.getInputAlphabet(), pruningMode);
    }

    /**
     * Minimizes the given, potentially partial Mealy machine. The result is returned in the form of a
     * {@link CompactMealy}, and pruning is performed after computing state equivalences.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizePartialMealy(MealyMachine<?, I, ?, O> mealy, Alphabet<I> alphabet) {
        return minimizePartialMealy(mealy, alphabet, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, potentially partial Mealy machine. The result is returned in the form of a
     * {@link CompactMealy}.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param pruningMode
     *         the pruning mode
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <I, O> CompactMealy<I, O> minimizePartialMealy(MealyMachine<?, I, ?, O> mealy,
                                                                 Alphabet<I> alphabet,
                                                                 PruningMode pruningMode) {
        return minimizePartialMealy(mealy, alphabet, pruningMode, new CompactMealy.Creator<>());
    }

    /**
     * Minimizes the given, potentially partial Mealy machine. The result is returned in the form of the automaton
     * created by the given creator.
     *
     * @param mealy
     *         the Mealy machine to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param pruningMode
     *         the pruning mode
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param <A>
     *         automaton type
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a minimized version of the specified Mealy machine
     */
    public static <A extends MutableMealyMachine<?, I, ?, O>, I, O> A minimizePartialMealy(MealyMachine<?, I, ?, O> mealy,
                                                                                           Alphabet<I> alphabet,
                                                                                           PruningMode pruningMode,
                                                                                           AutomatonCreator<A, I> creator) {
        return minimizePartialUniversal(mealy,
                                        alphabet,
                                        creator,
                                        AutomatonInitialPartitioning.BY_TRANSITION_PROPERTIES,
                                        StateSignature.byTransitionProperties(new Object[alphabet.size()]),
                                        pruningMode);
    }

    /**
     * Minimizes the given, complete automaton depending on the given partitioning function. Pruning is performed after
     * computing state equivalences.
     *
     * @param automaton
     *         the automaton to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param ip
     *         the initial partitioning function, determining how states will be distinguished
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
     * @return the minimized automaton, initially constructed from the given {@code creator}.
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A minimizeUniversal(
            UniversalDeterministicAutomaton<?, I, T, SP, TP> automaton,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator,
            AutomatonInitialPartitioning ip) {
        return minimizeUniversal(automaton, alphabet, creator, ip, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, complete automaton depending on the given partitioning function.
     *
     * @param automaton
     *         the automaton to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param ip
     *         the initial partitioning function, determining how states will be distinguished
     * @param pruningMode
     *         the pruning mode
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
     * @return the minimized automaton, initially constructed from the given {@code creator}.
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A minimizeUniversal(
            UniversalDeterministicAutomaton<?, I, T, SP, TP> automaton,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator,
            AutomatonInitialPartitioning ip,
            PruningMode pruningMode) {

        final FullIntAbstraction<T, SP, TP> abs = automaton.fullIntAbstraction(alphabet);
        final Hopcroft hopcroft =
                HopcroftInitializers.initializeComplete(abs, ip, pruningMode == PruningMode.PRUNE_BEFORE);

        hopcroft.computeCoarsestStablePartition();

        return HopcroftExtractors.toDeterministic(hopcroft,
                                                  creator,
                                                  alphabet,
                                                  abs,
                                                  abs::getStateProperty,
                                                  abs::getTransitionProperty,
                                                  pruningMode == PruningMode.PRUNE_AFTER);

    }

    /**
     * Minimizes the given, potentially partial depending on the given partitioning function. Pruning is performed after
     * computing state equivalences.
     *
     * @param automaton
     *         the automaton to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param ip
     *         the initial partitioning function, determining how states will be distinguished
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
     * @return the minimized automaton, initially constructed from the given {@code creator}.
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A minimizePartialUniversal(
            UniversalDeterministicAutomaton<?, I, T, SP, TP> automaton,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator,
            AutomatonInitialPartitioning ip,
            Object sinkClassification) {

        return minimizePartialUniversal(automaton, alphabet, creator, ip, sinkClassification, PruningMode.PRUNE_AFTER);
    }

    /**
     * Minimizes the given, potentially partial automaton depending on the given partitioning function.
     *
     * @param automaton
     *         the automaton to minimize
     * @param alphabet
     *         the input alphabet (this will be the input alphabet of the resulting Mealy machine)
     * @param creator
     *         the creator for constructing the automata instance to return
     * @param ip
     *         the initial partitioning function, determining how states will be distinguished
     * @param pruningMode
     *         the pruning mode
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
     * @return the minimized automaton, initially constructed from the given {@code creator}.
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A minimizePartialUniversal(
            UniversalDeterministicAutomaton<?, I, T, SP, TP> automaton,
            Alphabet<I> alphabet,
            AutomatonCreator<A, I> creator,
            AutomatonInitialPartitioning ip,
            Object sinkClassification,
            PruningMode pruningMode) {

        final FullIntAbstraction<T, SP, TP> abs = automaton.fullIntAbstraction(alphabet);
        final Hopcroft hopcroft = HopcroftInitializers.initializePartial(abs,
                                                                         ip,
                                                                         sinkClassification,
                                                                         pruningMode == PruningMode.PRUNE_BEFORE);

        hopcroft.computeCoarsestStablePartition();

        return HopcroftExtractors.toDeterministic(hopcroft,
                                                  creator,
                                                  alphabet,
                                                  abs,
                                                  abs::getStateProperty,
                                                  abs::getTransitionProperty,
                                                  pruningMode == PruningMode.PRUNE_AFTER);

    }
}
