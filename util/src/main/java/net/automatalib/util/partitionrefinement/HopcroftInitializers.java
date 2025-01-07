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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.simple.SimpleDeterministicAutomaton;
import net.automatalib.common.util.array.ArrayUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class provides several methods to initialize a {@link Hopcroft} partition refinement data structure from common
 * sources, e.g., automata.
 * <p>
 * The counterpart of this class is {@link HopcroftExtractors}, which provides methods to translate the contents of the
 * partition refinement data structure after coarsest stable partition computation back to such structures.
 */
public final class HopcroftInitializers {

    private HopcroftInitializers() {}

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, using a
     * predefined initial partitioning mode.
     *
     * @param abs
     *         the abstraction of the input automaton
     * @param ip
     *         the initial partitioning mode
     * @param pruneUnreachable
     *         whether to prune unreachable states during initialization
     *
     * @return the initialized partition refinement data structure
     */
    public static Hopcroft initializeComplete(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> abs,
                                              AutomatonInitialPartitioning ip,
                                              boolean pruneUnreachable) {
        return initializeComplete(abs, ip.initialClassifier(abs), pruneUnreachable);
    }

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, partitioning
     * states according to the given classification function.
     * <p>
     * The return value of the {@code initialClassification} function can be any valid {@code Object} reference (even
     * {@code null}). States are initially placed in the same partition class if their return values from applying
     * {@code initialClassification} have the same hash code (determined according to {@link Objects#hashCode(Object)})
     * and are equal (determined according to {@link Objects#equals(Object, Object)}).
     *
     * @param abs
     *         the abstraction of the input automaton
     * @param initialClassification
     *         the function determining the initial classification
     * @param pruneUnreachable
     *         whether to prune unreachable states during initialization
     *
     * @return the initialized partition refinement data structure
     */
    public static Hopcroft initializeComplete(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                              IntFunction<?> initialClassification,
                                              boolean pruneUnreachable) {

        return pruneUnreachable ?
                initializeCompletePrune(abs, initialClassification) :
                initializeCompleteNoPrune(abs, initialClassification);
    }

    private static Hopcroft initializeCompletePrune(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                    IntFunction<?> initialClassification) {

        Hopcroft pt = new Hopcroft();
        int numStates = abs.size();
        int numInputs = abs.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        int init = abs.getIntInitialState();
        Object initClass = initialClassification.apply(init);

        blockForState[init] = getOrCreateBlock(blockMap, initClass, pt);

        int[] statesBuff = new int[numStates];
        statesBuff[0] = init;

        int statesPtr = 0;
        int reachableStates = 1;

        while (statesPtr < reachableStates) {
            int curr = statesBuff[statesPtr++];
            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {
                int succ = abs.getSuccessor(curr, i);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                Block succBlock = blockForState[succ];
                if (succBlock == null) {
                    Object succClass = initialClassification.apply(succ);
                    blockForState[succ] = getOrCreateBlock(blockMap, succClass, pt);
                    statesBuff[reachableStates++] = succ;
                }
                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        pt.canonizeBlocks();

        data[predOfsDataLow] += predDataLow;
        ArrayUtil.prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            updateBlockAndPosData(blockForState, stateId, data, posDataLow);

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = abs.getSuccessor(stateId, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = stateId;
                predOfsBase += numStates;
            }
        }

        updatePTFields(pt, data, posDataLow, predOfsDataLow, blockForState, numStates, numInputs);

        return pt;
    }

    private static Hopcroft initializeCompleteNoPrune(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                      IntFunction<?> initialClassification) {
        Hopcroft pt = new Hopcroft();
        int numStates = abs.size();
        int numInputs = abs.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        for (int i = 0; i < numStates; i++) {
            Object classification = initialClassification.apply(i);
            blockForState[i] = getOrCreateBlock(blockMap, classification, pt);

            int predCountBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = abs.getSuccessor(i, j);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        pt.canonizeBlocks();

        data[predOfsDataLow] += predDataLow;
        ArrayUtil.prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < numStates; i++) {
            updateBlockAndPosData(blockForState, i, data, posDataLow);
            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = abs.getSuccessor(i, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = i;
                predOfsBase += numStates;
            }
        }

        updatePTFields(pt, data, posDataLow, predOfsDataLow, blockForState, numStates, numInputs);

        return pt;
    }

    public static Hopcroft initializePartial(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> abs,
                                             AutomatonInitialPartitioning ip,
                                             Object sinkClassification,
                                             boolean pruneUnreachable) {
        return initializePartial(abs, ip.initialClassifier(abs), sinkClassification, pruneUnreachable);
    }

    /**
     * Initializes the partition refinement data structure from a given deterministic automaton, initializing the
     * initial partition according to the given classification function.
     * <p>
     * This method can be used for automata with partially defined transition functions.
     *
     * @param abs
     *         the abstraction of the input automaton
     * @param initialClassification
     *         the initial classification function
     * @param sinkClassification
     *         determines how a sink is being classified
     * @param pruneUnreachable
     *         whether to prune unreachable states during initialization
     *
     * @return the initialized partition refinement data structure
     */
    public static Hopcroft initializePartial(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                             IntFunction<?> initialClassification,
                                             Object sinkClassification,
                                             boolean pruneUnreachable) {
        return pruneUnreachable ?
                initializePartialPrune(abs, initialClassification, sinkClassification) :
                initializePartialNoPrune(abs, initialClassification, sinkClassification);
    }

    private static Hopcroft initializePartialPrune(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                   IntFunction<?> initialClassification,
                                                   Object sinkClassification) {

        Hopcroft pt = new Hopcroft();
        int numStates = abs.size();
        int numInputs = abs.numInputs();

        int sinkId = numStates;
        int numStatesWithSink = numStates + 1;
        int posDataLow = numStatesWithSink;
        int predOfsDataLow = posDataLow + numStatesWithSink;
        int numTransitionsFull = numStatesWithSink * numInputs;
        int predDataLow = predOfsDataLow + numTransitionsFull + 1;
        int dataSize = predDataLow + numTransitionsFull;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStatesWithSink];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        int initId = abs.getIntInitialState();

        Object initClass = initialClassification.apply(initId);

        blockForState[initId] = getOrCreateBlock(blockMap, initClass, pt);

        int[] statesBuff = new int[numStatesWithSink];
        statesBuff[0] = initId;

        int statesPtr = 0;
        int reachableStates = 1;

        boolean partial = false;
        while (statesPtr < reachableStates) {
            int currId = statesBuff[statesPtr++];
            if (currId == sinkId) {
                continue;
            }

            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {

                int succ = abs.getSuccessor(currId, i);
                int succId;
                if (succ < 0) {
                    succId = sinkId;
                    partial = true;
                } else {
                    succId = succ;
                }
                Block succBlock = blockForState[succId];
                if (succBlock == null) {
                    Object succClass;
                    if (succ < 0) {
                        succClass = sinkClassification;
                    } else {
                        succClass = initialClassification.apply(succ);
                    }
                    blockForState[succId] = getOrCreateBlock(blockMap, succClass, pt);
                    statesBuff[reachableStates++] = succId;
                }
                data[predCountBase + succId]++; // predOfsData
                predCountBase += numStatesWithSink;
            }
        }

        if (partial) {
            int predCountIdx = predOfsDataLow + sinkId;
            for (int i = 0; i < numInputs; i++) {
                data[predCountIdx]++; // predOfsData - sink state has all its symbols pointing to itself
                predCountIdx += numStatesWithSink;
            }
        }

        // data[predOfsDataLow + j*numStatesWithSink+i] now contains the count of transitions to state i from input j

        pt.canonizeBlocks();

        // Make predOfsData cumulative
        data[predOfsDataLow] += predDataLow;
        ArrayUtil.prefixSum(data, predOfsDataLow, predDataLow);

        // data[predOfsDataLow + j*numStatesWithSink+i] now contains
        // the final predOfsData value plus the count of transitions to state i from input j

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            updateBlockAndPosData(blockForState, stateId, data, posDataLow);

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                final int succId;

                if (stateId == sinkId) { // state is new artificial sink
                    succId = sinkId;
                } else {
                    int succ = abs.getSuccessor(stateId, j);
                    if (succ < 0) {
                        succId = sinkId;
                    } else {
                        succId = succ;
                    }
                }

                data[--data[predOfsBase + succId]] = stateId; // decrement predOfsData, set predData
                predOfsBase += numStatesWithSink;
            }
        }

        updatePTFields(pt, data, posDataLow, predOfsDataLow, blockForState, numStatesWithSink, numInputs);

        pt.removeEmptyBlocks();

        return pt;
    }

    private static Hopcroft initializePartialNoPrune(SimpleDeterministicAutomaton.FullIntAbstraction abs,
                                                     IntFunction<?> initialClassification,
                                                     Object sinkClassification) {

        Hopcroft pt = new Hopcroft();
        int numStates = abs.size();
        int numInputs = abs.numInputs();

        int sinkId = numStates;
        int numStatesWithSink = numStates + 1;
        int posDataLow = numStatesWithSink;
        int predOfsDataLow = posDataLow + numStatesWithSink;
        int numTransitionsFull = numStatesWithSink * numInputs;
        int predDataLow = predOfsDataLow + numTransitionsFull + 1;
        int dataSize = predDataLow + numTransitionsFull;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStatesWithSink];

        Map<@Nullable Object, Block> blockMap = new HashMap<>();

        boolean partial = false;
        for (int i = 0; i < numStates; i++) {
            Object classification = initialClassification.apply(i);
            blockForState[i] = getOrCreateBlock(blockMap, classification, pt);

            int predCountBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = abs.getSuccessor(i, j);
                int succId;
                if (succ < 0) {
                    succId = sinkId;
                    partial = true;
                } else {
                    succId = succ;
                }

                data[predCountBase + succId]++;
                predCountBase += numStatesWithSink;
            }
        }

        if (partial) {
            blockForState[sinkId] = getOrCreateBlock(blockMap, sinkClassification, pt);
            int predCountIdx = predOfsDataLow + sinkId;
            for (int i = 0; i < numInputs; i++) {
                data[predCountIdx]++; // predOfsData - sink state has all its symbols pointing to itself
                predCountIdx += numStatesWithSink;
            }
        }

        pt.canonizeBlocks();

        data[predOfsDataLow] += predDataLow;
        ArrayUtil.prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < numStates; i++) {
            updateBlockAndPosData(blockForState, i, data, posDataLow);
            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = abs.getSuccessor(i, j);

                final int succId;
                if (succ < 0) {
                    succId = sinkId;
                } else {
                    succId = succ;
                }

                data[--data[predOfsBase + succId]] = i;
                predOfsBase += numStatesWithSink;
            }
        }

        if (partial) {
            updateBlockAndPosData(blockForState, sinkId, data, posDataLow);
            int predOfsBase = predOfsDataLow;
            for (int i = 0; i < numInputs; i++) {
                data[--data[predOfsBase + sinkId]] = sinkId;
                predOfsBase += numStatesWithSink;
            }
        }

        updatePTFields(pt, data, posDataLow, predOfsDataLow, blockForState, numStatesWithSink, numInputs);

        pt.removeEmptyBlocks();

        return pt;
    }

    private static void updateBlockAndPosData(Block[] blockForState, int i, int[] data, int posDataLow) {
        Block b = blockForState[i];
        int pos = --b.low;
        data[pos] = i;
        data[posDataLow + i] = pos;
    }

    private static Block getOrCreateBlock(Map<@Nullable Object, Block> blockMap,
                                          @Nullable Object classification,
                                          Hopcroft pt) {
        Block block = blockMap.get(classification);
        if (block == null) {
            block = pt.createBlock();
            block.high = 0;
            blockMap.put(classification, block);
        }
        block.high++;
        return block;
    }

    private static void updatePTFields(Hopcroft pt,
                                       int[] data,
                                       int posDataLow,
                                       int predOfsDataLow,
                                       Block[] blockForState,
                                       int numStates,
                                       int numInputs) {
        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }
}
