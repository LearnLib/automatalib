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
package net.automatalib.util.partitionrefinement;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.IntFunction;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.commons.util.functions.FunctionsUtil;
import net.automatalib.words.Alphabet;

/**
 * This class provides methods for translating the result of a {@link PaigeTarjan} coarsest stable partition computation
 * into several common, more usable forms such as automata.
 * <p>
 * Most of the methods defined in this class expect the partition data to be in a certain form, and moreover may require
 * additional information, both of which is provided by corresponding methods defined in {@link
 * PaigeTarjanInitializers}.
 *
 * @author Malte Isberner
 */
public final class PaigeTarjanExtractors {

    private PaigeTarjanExtractors() {
    }

    /**
     * Translates the results of a coarsest stable partition computation into a deterministic automaton.
     * <p>
     * This method is designed to match the following methods from {@link PaigeTarjanInitializers}: <ul>
     *     <li> {@link PaigeTarjanInitializers#initDeterministic(PaigeTarjan,
     *     net.automatalib.automata.simple.SimpleDeterministicAutomaton,Alphabet, Function, Object)}
     *     </li>
     *     <li> {@link PaigeTarjanInitializers#initDeterministic(PaigeTarjan,DeterministicAutomaton, Alphabet,
     *     java.util.function.Predicate, boolean)}
     *     </li>
     * </ul>
     * <p>
     * Both the {@code spExtractor} and the {@code tpExtractor} can be {@code null}, in which case they are replaced by
     * a function always returning {@code null}.
     *
     * @param pt
     *         the partition refinement data structure, after computing the coarsest stable partition
     * @param creator
     *         an {@link AutomatonCreator} for creating the resulting automaton
     * @param inputs
     *         the input alphabet to use
     * @param original
     *         the original automaton on which the partition was computed
     * @param origIds
     *         the {@link StateIDs} that translate the {@code int}s from the {@link PaigeTarjan} to states of {@code
     *         original} (e.g., obtained as the result from {@link PaigeTarjanInitializers#initDeterministic(PaigeTarjan,
     *         DeterministicAutomaton, Alphabet, java.util.function.Predicate, boolean)}
     * @param spExtractor
     *         the state property extractor, or {@code null}
     * @param tpExtractor
     *         the transition property extractor, or {@code null}
     * @param pruneUnreachable
     *         {@code true} if unreachable states should be pruned during construction, {@code false} otherwise
     *
     * @return an automaton created using the specified creator, over the specified input alphabet, and reflecting the
     * partition data of the specified {@link PaigeTarjan} object
     */
    public static <S1, S2, I, T1, T2, SP, TP, A extends MutableDeterministic<S2, I, T2, SP, TP>> A toDeterministic(
            PaigeTarjan pt,
            AutomatonCreator<A, I> creator,
            Alphabet<I> inputs,
            DeterministicAutomaton<S1, I, T1> original,
            StateIDs<S1> origIds,
            Function<? super S1, ? extends SP> spExtractor,
            Function<? super T1, ? extends TP> tpExtractor,
            boolean pruneUnreachable) {

        final Function<? super S1, ? extends SP> safeSpExtractor = FunctionsUtil.safeDefault(spExtractor);
        final Function<? super T1, ? extends TP> safeTpExtractor = FunctionsUtil.safeDefault(tpExtractor);

        if (pruneUnreachable) {
            return toDeterministicPruned(pt, creator, inputs, original, origIds, safeSpExtractor, safeTpExtractor);
        }
        return toDeterministicUnpruned(pt, creator, inputs, original, origIds, safeSpExtractor, safeTpExtractor);
    }

    /**
     * Translates the results of a coarsest stable partition computation into a deterministic automaton.
     * <p>
     * This method is designed to match the following methods from {@link PaigeTarjanInitializers}: <ul>
     *     <li> {@link PaigeTarjanInitializers#initCompleteDeterministic(PaigeTarjan,
     *     net.automatalib.automata.simple.SimpleDeterministicAutomaton.FullIntAbstraction, IntFunction, boolean)}
     *     </li>
     *     <li> {@link PaigeTarjanInitializers#initCompleteDeterministic(PaigeTarjan,
     *     net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction,
     *     PaigeTarjanInitializers.AutomatonInitialPartitioning, boolean)}
     *     </li>
     * </ul>
     * <p>
     * Both the {@code spExtractor} and the {@code tpExtractor} can be {@code null}, in which case they are replaced by
     * a function always returning {@code null}.
     *
     * @param pt
     *         the partition refinement data structure, after computing the coarsest stable partition
     * @param creator
     *         an {@link AutomatonCreator} for creating the resulting automaton
     * @param inputs
     *         the input alphabet to use
     * @param absOriginal
     *         the abstraction of the original automaton that was used to build the partition refinement data structure
     * @param spExtractor
     *         the state property extractor, or {@code null}
     * @param tpExtractor
     *         the transition property extractor, or {@code null}
     * @param pruneUnreachable
     *         {@code true} if unreachable states should be pruned during construction, {@code false} otherwise
     *
     * @return an automaton created using the specified creator, over the specified input alphabet, and reflecting the
     * partition data of the specified {@link PaigeTarjan} object
     */
    public static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministic(PaigeTarjan pt,
                                                                                                    AutomatonCreator<A, I> creator,
                                                                                                    Alphabet<I> inputs,
                                                                                                    DeterministicAutomaton.FullIntAbstraction<T> absOriginal,
                                                                                                    IntFunction<? extends SP> spExtractor,
                                                                                                    Function<? super T, ? extends TP> tpExtractor,
                                                                                                    boolean pruneUnreachable) {

        final IntFunction<? extends SP> safeSpExtractor = FunctionsUtil.safeDefault(spExtractor);
        final Function<? super T, ? extends TP> safeTpExtractor = FunctionsUtil.safeDefault(tpExtractor);

        if (pruneUnreachable) {
            return toDeterministicPruned(pt, creator, inputs, absOriginal, safeSpExtractor, safeTpExtractor);
        }
        return toDeterministicUnpruned(pt, creator, inputs, absOriginal, safeSpExtractor, safeTpExtractor);
    }

    private static <S1, S2, I, T1, T2, SP, TP, A extends MutableDeterministic<S2, I, T2, SP, TP>> A toDeterministicPruned(
            PaigeTarjan pt,
            AutomatonCreator<A, I> creator,
            Alphabet<I> inputs,
            DeterministicAutomaton<S1, I, T1> original,
            StateIDs<S1> origIds,
            Function<? super S1, ? extends SP> spExtractor,
            Function<? super T1, ? extends TP> tpExtractor) {
        int numBlocks = pt.getNumBlocks();

        A result = creator.createAutomaton(inputs, numBlocks);

        S1 init = original.getInitialState();
        int initId = origIds.getStateId(init);
        SP initSp = spExtractor.apply(init);
        S2 resInit = result.addInitialState(initSp);
        RichArray<S2> states = new RichArray<>(numBlocks);
        Block initBlock = pt.getBlockForState(initId);
        states.update(initBlock.id, resInit);

        Deque<Pair<S1, S2>> queue = new ArrayDeque<>();
        queue.add(new Pair<>(init, resInit));

        Pair<S1, S2> curr;
        while ((curr = queue.poll()) != null) {
            S1 state = curr.getFirst();
            S2 resState = curr.getSecond();

            for (I sym : inputs) {
                T1 trans = original.getTransition(state, sym);
                if (trans != null) {
                    TP tp = tpExtractor.apply(trans);
                    S1 succ = original.getSuccessor(trans);
                    int succId = origIds.getStateId(succ);
                    Block succBlock = pt.getBlockForState(succId);
                    int succBlockId = succBlock.id;
                    S2 resSucc = states.get(succBlockId);
                    if (resSucc == null) {
                        SP succSp = spExtractor.apply(succ);
                        resSucc = result.addState(succSp);
                        states.update(succBlockId, resSucc);
                    }
                    result.setTransition(resState, sym, resSucc, tp);
                }
            }
        }

        return result;
    }

    private static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicPruned(PaigeTarjan pt,
                                                                                                           AutomatonCreator<A, I> creator,
                                                                                                           Alphabet<I> inputs,
                                                                                                           DeterministicAutomaton.FullIntAbstraction<T> absOriginal,
                                                                                                           IntFunction<? extends SP> spExtractor,
                                                                                                           Function<? super T, ? extends TP> tpExtractor) {

        int numBlocks = pt.getNumBlocks();
        int numInputs = inputs.size();
        int[] repMap = new int[numBlocks];
        int[] stateMap = new int[numBlocks];
        Arrays.fill(stateMap, -1);

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        int origInit = absOriginal.getIntInitialState();
        SP initSp = spExtractor.apply(origInit);
        int resInit = resultAbs.addIntInitialState(initSp);

        Block initBlock = pt.getBlockForState(origInit);
        stateMap[initBlock.id] = resInit;
        repMap[resInit] = origInit;

        int statesPtr = 0;
        int numStates = 1;
        while (statesPtr < numStates) {
            int resState = statesPtr++;
            int rep = repMap[resState];
            for (int i = 0; i < numInputs; i++) {
                T trans = absOriginal.getTransition(rep, i);
                if (trans != null) {
                    TP tp = tpExtractor.apply(trans);
                    int succ = absOriginal.getIntSuccessor(trans);
                    Block succBlock = pt.getBlockForState(succ);
                    int succBlockId = succBlock.id;
                    int resSucc = stateMap[succBlockId];
                    if (resSucc < 0) {
                        SP sp = spExtractor.apply(succ);
                        resSucc = resultAbs.addIntState(sp);
                        stateMap[succBlockId] = resSucc;
                        repMap[resSucc] = succ;
                        numStates++;
                    }
                    resultAbs.setTransition(resState, i, resSucc, tp);
                }
            }
        }

        return result;
    }

    private static <S1, S2, I, T1, T2, SP, TP, A extends MutableDeterministic<S2, I, T2, SP, TP>> A toDeterministicUnpruned(
            PaigeTarjan pt,
            AutomatonCreator<A, I> creator,
            Alphabet<I> inputs,
            DeterministicAutomaton<S1, I, T1> original,
            StateIDs<S1> origIds,
            Function<? super S1, ? extends SP> spExtractor,
            Function<? super T1, ? extends TP> tpExtractor) {
        int numBlocks = pt.getNumBlocks();

        A result = creator.createAutomaton(inputs, numBlocks);
        RichArray<S2> states = new RichArray<>(numBlocks);

        for (Block curr : pt.blockList()) {
            int blockId = curr.id;
            S1 rep = origIds.getState(pt.getRepresentative(curr));
            SP sp = spExtractor.apply(rep);
            S2 resState = result.addState(sp);
            states.update(blockId, resState);
        }
        for (Block curr : pt.blockList()) {
            int blockId = curr.id;
            S1 rep = origIds.getState(pt.getRepresentative(curr));
            S2 resultState = states.get(blockId);

            for (I sym : inputs) {
                T1 origTrans = original.getTransition(rep, sym);
                TP tp;
                S2 resultSucc;
                if (origTrans != null) {
                    tp = tpExtractor.apply(origTrans);
                    S1 origSucc = original.getSuccessor(origTrans);
                    int origSuccId = origIds.getStateId(origSucc);
                    resultSucc = states.get(pt.getBlockForState(origSuccId).id);
                } else {
                    resultSucc = null;
                    tp = null;
                }
                result.setTransition(resultState, sym, resultSucc, tp);
            }
        }

        S1 origInit = original.getInitialState();
        int origInitId = origIds.getStateId(origInit);
        S2 resInit = states.get(pt.getBlockForState(origInitId).id);
        result.setInitialState(resInit);

        return result;
    }

    private static <I, T, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicUnpruned(PaigeTarjan pt,
                                                                                                             AutomatonCreator<A, I> creator,
                                                                                                             Alphabet<I> inputs,
                                                                                                             DeterministicAutomaton.FullIntAbstraction<T> absOriginal,
                                                                                                             IntFunction<? extends SP> spExtractor,
                                                                                                             Function<? super T, ? extends TP> tpExtractor) {

        int numBlocks = pt.getNumBlocks();
        int numInputs = inputs.size();

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        for (int i = 0; i < numBlocks; i++) {
            resultAbs.addIntState();
        }

        for (Block curr : pt.blockList()) {
            int blockId = curr.id;
            int rep = pt.getRepresentative(curr);
            SP sp = spExtractor.apply(rep);
            resultAbs.setStateProperty(blockId, sp);

            for (int i = 0; i < numInputs; i++) {
                T trans = absOriginal.getTransition(rep, i);
                if (trans != null) {
                    int succ = absOriginal.getIntSuccessor(trans);
                    int resSucc = pt.getBlockForState(succ).id;
                    TP tp = tpExtractor.apply(trans);
                    resultAbs.setTransition(blockId, i, resSucc, tp);
                }
            }
        }
        int origInit = absOriginal.getIntInitialState();
        resultAbs.setInitialState(pt.getBlockForState(origInit).id);

        return result;
    }
}
