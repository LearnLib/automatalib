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

import java.util.Arrays;
import java.util.function.IntFunction;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.simple.SimpleDeterministicAutomaton;
import net.automatalib.common.util.function.BiIntFunction;

/**
 * This class provides methods for translating the result of a {@link Hopcroft} coarsest stable partition computation
 * into several common, more usable forms such as automata.
 * <p>
 * Most of the methods defined in this class expect the partition data to be in a certain form, and moreover may require
 * additional information, both of which is provided by corresponding methods defined in {@link HopcroftInitializers}.
 */
public final class HopcroftExtractors {

    private HopcroftExtractors() {}

    /**
     * Translates the results of the coarsest stable partition computation into a deterministic automaton.
     *
     * @param hopcroft
     *         the partition refinement data structure, after computing the coarsest stable partition
     * @param creator
     *         an {@link AutomatonCreator} for creating the resulting automaton
     * @param inputs
     *         the input alphabet to use
     * @param abs
     *         the abstraction of the original automaton that was used to build the partition refinement data structure
     * @param spExtractor
     *         the state property extractor
     * @param tpExtractor
     *         the transition property extractor
     * @param pruneUnreachable
     *         {@code true} if unreachable states should be pruned during construction, {@code false} otherwise
     * @param <I>
     *         input symbol type
     * @param <SP>
     *         state property type
     * @param <TP>
     *         transition property type
     * @param <A>
     *         automaton type
     *
     * @return an automaton created using the specified creator, over the specified input alphabet, and reflecting the
     * partition data of the specified {@link Hopcroft} object
     */
    public static <I, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministic(Hopcroft hopcroft,
                                                                                                 AutomatonCreator<A, I> creator,
                                                                                                 Alphabet<I> inputs,
                                                                                                 SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                                                                 IntFunction<? extends SP> spExtractor,
                                                                                                 BiIntFunction<? extends TP> tpExtractor,
                                                                                                 boolean pruneUnreachable) {
        return pruneUnreachable ?
                toDeterministicPruned(hopcroft, creator, inputs, abs, spExtractor, tpExtractor) :
                toDeterministicUnpruned(hopcroft, creator, inputs, abs, spExtractor, tpExtractor);
    }

    private static <I, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicPruned(Hopcroft hopcroft,
                                                                                                        AutomatonCreator<A, I> creator,
                                                                                                        Alphabet<I> inputs,
                                                                                                        SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                                                                        IntFunction<? extends SP> spExtractor,
                                                                                                        BiIntFunction<? extends TP> tpExtractor) {

        int numBlocks = hopcroft.getNumBlocks();
        int numInputs = inputs.size();
        int[] repMap = new int[numBlocks];
        int[] stateMap = new int[numBlocks];
        Arrays.fill(stateMap, -1);

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        int origInit = abs.getIntInitialState();
        SP initSp = spExtractor.apply(origInit);
        int resInit = resultAbs.addIntInitialState(initSp);

        Block initBlock = hopcroft.getBlockForState(origInit);
        stateMap[initBlock.id] = resInit;
        repMap[resInit] = origInit;

        int statesPtr = 0;
        int numStates = 1;
        while (statesPtr < numStates) {
            int resState = statesPtr++;
            int rep = repMap[resState];
            for (int i = 0; i < numInputs; i++) {
                int succ = abs.getSuccessor(rep, i);
                if (succ >= 0) {
                    TP tp = tpExtractor.apply(rep, i);
                    Block succBlock = hopcroft.getBlockForState(succ);
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

    private static <I, SP, TP, A extends MutableDeterministic<?, I, ?, SP, TP>> A toDeterministicUnpruned(Hopcroft hopcroft,
                                                                                                          AutomatonCreator<A, I> creator,
                                                                                                          Alphabet<I> inputs,
                                                                                                          SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                                                                          IntFunction<? extends SP> spExtractor,
                                                                                                          BiIntFunction<? extends TP> tpExtractor) {

        int numBlocks = hopcroft.getNumBlocks();
        int numInputs = inputs.size();

        A result = creator.createAutomaton(inputs, numBlocks);
        MutableDeterministic.FullIntAbstraction<?, SP, TP> resultAbs = result.fullIntAbstraction(inputs);

        for (int i = 0; i < numBlocks; i++) {
            resultAbs.addIntState();
        }

        for (Block curr : hopcroft.blockList()) {
            int blockId = curr.id;
            int rep = hopcroft.getRepresentative(curr);
            SP sp = spExtractor.apply(rep);
            resultAbs.setStateProperty(blockId, sp);

            for (int i = 0; i < numInputs; i++) {
                int succ = abs.getSuccessor(rep, i);
                if (succ >= 0) {
                    int resSucc = hopcroft.getBlockForState(succ).id;
                    TP tp = tpExtractor.apply(rep, i);
                    resultAbs.setTransition(blockId, i, resSucc, tp);
                }
            }
        }
        int origInit = abs.getIntInitialState();
        resultAbs.setInitialState(hopcroft.getBlockForState(origInit).id);

        return result;
    }
}
