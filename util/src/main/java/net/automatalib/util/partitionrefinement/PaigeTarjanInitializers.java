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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import lombok.EqualsAndHashCode;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.words.Alphabet;

/**
 * This class provides several methods to initialize a {@link PaigeTarjan} partition refinement data structure from
 * common sources, e.g., automata.
 * <p>
 * The counterpart of this class is {@link PaigeTarjanExtractors}, which provides methods to translate the contents of
 * the partition refinement data structure after coarsest stable partition computation back to such structures.
 *
 * @author Malte Isberner
 */
public final class PaigeTarjanInitializers {

    private PaigeTarjanInitializers() {
    }

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, using a
     * predefined initial partitioning mode.
     *
     * @param pt
     *         the partition refinement data structure
     * @param absAutomaton
     *         the abstraction of the input automaton
     * @param ip
     *         the initial partitioning mode
     * @param pruneUnreachable
     *         whether or not to prune unreachable states during initialization
     */
    public static void initCompleteDeterministic(PaigeTarjan pt,
                                                 UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> absAutomaton,
                                                 AutomatonInitialPartitioning ip,
                                                 boolean pruneUnreachable) {
        initCompleteDeterministic(pt, absAutomaton, ip.initialClassifier(absAutomaton), pruneUnreachable);
    }

    /**
     * Initializes the partition refinement data structure from a given abstracted deterministic automaton, partitioning
     * states according to the given classification function.
     * <p>
     * The return value of the {@code initialClassification} function can be any valid {@code Object} reference (even
     * {@code null}). States are initially placed in the same partition class if their return values from applying
     * {@code initialClassification} have the same hash code (determined according to {@link Objects#hashCode(Object)})
     * and are equal (determined according to {@link Objects#equals(Object, Object)}.
     *
     * @param pt
     *         the partition refinement data structure
     * @param absAutomaton
     *         the abstraction of the input automaton
     * @param initialClassification
     *         the function determining the initial classification
     * @param pruneUnreachable
     *         whether or not to prune unreachable states during initialization
     */
    public static void initCompleteDeterministic(PaigeTarjan pt,
                                                 SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                 IntFunction<?> initialClassification,
                                                 boolean pruneUnreachable) {

        if (pruneUnreachable) {
            initCompleteDeterministicPrune(pt, absAutomaton, initialClassification);
        } else {
            initCompleteDeterministicNoPrune(pt, absAutomaton, initialClassification);
        }
    }

    private static void initCompleteDeterministicPrune(PaigeTarjan pt,
                                                       SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                       IntFunction<?> initialClassification) {

        int numStates = absAutomaton.size();
        int numInputs = absAutomaton.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<Object, Block> blockMap = new HashMap<>();

        int init = absAutomaton.getIntInitialState();
        Object initClass = initialClassification.apply(init);

        Block initBlock = pt.createBlock();
        initBlock.high = 1;
        blockForState[init] = initBlock;
        blockMap.put(initClass, initBlock);

        int[] statesBuff = new int[numStates];
        statesBuff[0] = init;

        int statesPtr = 0;
        int reachableStates = 1;

        while (statesPtr < reachableStates) {
            int curr = statesBuff[statesPtr++];
            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {
                int succ = absAutomaton.getSuccessor(curr, i);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                Block succBlock = blockForState[succ];
                if (succBlock == null) {
                    Object succClass = initialClassification.apply(succ);
                    succBlock = blockMap.get(succClass);
                    if (succBlock == null) {
                        succBlock = pt.createBlock();
                        succBlock.high = 0;
                        blockMap.put(succClass, succBlock);
                    }
                    succBlock.high++;
                    blockForState[succ] = succBlock;
                    statesBuff[reachableStates++] = succ;
                }
                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            Block b = blockForState[stateId];
            int pos = --b.low;
            data[pos] = stateId;
            data[posDataLow + stateId] = pos;

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(stateId, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = stateId;
                predOfsBase += numStates;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }

    private static void initCompleteDeterministicNoPrune(PaigeTarjan pt,
                                                         SimpleDeterministicAutomaton.FullIntAbstraction absAutomaton,
                                                         IntFunction<?> initialClassification) {
        int numStates = absAutomaton.size();
        int numInputs = absAutomaton.numInputs();

        int posDataLow = numStates;
        int predOfsDataLow = posDataLow + numStates;
        int numTransitions = numStates * numInputs;
        int predDataLow = predOfsDataLow + numTransitions + 1;
        int dataSize = predDataLow + numTransitions;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStates];

        Map<Object, Block> blockMap = new HashMap<>();

        for (int i = 0; i < numStates; i++) {
            Object classification = initialClassification.apply(i);
            Block block = blockMap.get(classification);
            if (block == null) {
                block = pt.createBlock();
                block.high = 0;
                blockMap.put(classification, block);
            }
            block.high++;
            blockForState[i] = block;

            int predCountBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(i, j);
                if (succ < 0) {
                    throw new IllegalArgumentException("Automaton must not be partial");
                }

                data[predCountBase + succ]++;
                predCountBase += numStates;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < numStates; i++) {
            Block b = blockForState[i];
            int pos = --b.low;
            data[pos] = i;
            data[posDataLow + i] = pos;
            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                int succ = absAutomaton.getSuccessor(i, j);
                assert succ >= 0;

                data[--data[predOfsBase + succ]] = i;
                predOfsBase += numStates;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStates, numInputs);
    }

    public static void prefixSum(int[] array, int startInclusive, int endExclusive) {
        int curr = array[startInclusive];
        for (int i = startInclusive + 1; i < endExclusive; i++) {
            curr += array[i];
            array[i] = curr;
        }
    }

    /**
     * Initializes the partition refinement data structure from a given deterministic automaton, initializing the
     * initial partition according to the given classification function.
     * <p>
     * This method can be used for automata with partially defined transition functions.
     *
     * @param pt
     *         the partition refinement data structure
     * @param automaton
     *         the input automaton
     * @param inputs
     *         the input alphabet to consider, which also determines the mapping from {@code int}s to input symbols in
     *         the data structure
     * @param initialClassification
     *         the initial classification function
     * @param sinkClassification
     *         determines how a sink is being classified.
     *
     * @return the state ID mapping relating {@code int}s representing states in the partition refinement data structure
     * to states of the automaton, and vice versa
     */
    public static <S, I> StateIDs<S> initDeterministic(PaigeTarjan pt,
                                                       SimpleDeterministicAutomaton<S, I> automaton,
                                                       Alphabet<I> inputs,
                                                       Function<? super S, ?> initialClassification,
                                                       Object sinkClassification) {
        int numStates = automaton.size();
        int numInputs = inputs.size();

        int sinkId = numStates;
        int numStatesWithSink = numStates + 1;
        int posDataLow = numStatesWithSink;
        int predOfsDataLow = posDataLow + numStatesWithSink;
        int numTransitionsFull = numStatesWithSink * numInputs;
        int predDataLow = predOfsDataLow + numTransitionsFull + 1;
        int dataSize = predDataLow + numTransitionsFull;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStatesWithSink];

        StateIDs<S> ids = automaton.stateIDs();

        Map<Object, Block> blockMap = new HashMap<>();

        S init = automaton.getInitialState();
        int initId = ids.getStateId(init);

        Object initClass = initialClassification.apply(init);

        Block initBlock = pt.createBlock();
        initBlock.high = 1;
        blockForState[initId] = initBlock;
        blockMap.put(initClass, initBlock);

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
            S curr = ids.getState(currId);

            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {
                I sym = inputs.getSymbol(i);

                S succ = automaton.getSuccessor(curr, sym);
                int succId;
                if (succ != null) {
                    succId = ids.getStateId(succ);
                } else {
                    succId = sinkId;
                    partial = true;
                }
                Block succBlock = blockForState[succId];
                if (succBlock == null) {
                    Object succClass;
                    if (succ != null) {
                        succClass = initialClassification.apply(succ);
                    } else {
                        succClass = sinkClassification;
                    }
                    succBlock = blockMap.get(succClass);
                    if (succBlock == null) {
                        succBlock = pt.createBlock();
                        succBlock.high = 0;
                        blockMap.put(succClass, succBlock);
                    }
                    succBlock.high++;
                    blockForState[succId] = succBlock;
                    statesBuff[reachableStates++] = succId;
                }
                data[predCountBase + succId]++;
                predCountBase += numStatesWithSink;
            }
        }

        if (partial) {
            int predCountIdx = predOfsDataLow + sinkId;
            for (int i = 0; i < numInputs; i++) {
                data[predCountIdx]++;
                predCountIdx += numStatesWithSink;
            }
        }

        int curr = 0;
        for (Block b : pt.blockList()) {
            curr += b.high;
            b.high = curr;
            b.low = curr;
        }

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < reachableStates; i++) {
            int stateId = statesBuff[i];
            Block b = blockForState[stateId];
            int pos = --b.low;
            data[pos] = stateId;
            data[posDataLow + stateId] = pos;

            S state = ids.getState(stateId);

            int predOfsBase = predOfsDataLow;

            for (int j = 0; j < numInputs; j++) {
                I sym = inputs.getSymbol(j);
                S succ = automaton.getSuccessor(state, sym);
                int succId;
                if (succ == null) {
                    succId = sinkId;
                } else {
                    succId = ids.getStateId(succ);
                }

                data[--data[predOfsBase + succId]] = stateId;
                predOfsBase += numStatesWithSink;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setSize(numStatesWithSink, numInputs);
        pt.setBlockForState(blockForState);

        pt.removeEmptyBlocks();

        return ids;
    }

    /**
     * Initializes the partition refinement data structure from a given deterministic automaton, initializing the
     * initial partition according to the given classification predicate (i.e., assuming a binary initial
     * partitioning).
     * <p>
     * This method can be used for automata with partially defined transition functions.
     *
     * @param pt
     *         the partition refinement data structure
     * @param automaton
     *         the input automaton
     * @param inputs
     *         the input alphabet to consider, which also determines the mapping from {@code int}s to input symbols in
     *         the data structure
     * @param initialClassification
     *         the initial classification predicate
     * @param sinkClassification
     *         determines how a sink is being classified.
     *
     * @return the state ID mapping relating {@code int}s representing states in the partition refinement data structure
     * to states of the automaton, and vice versa
     */
    public static <S, I, T> StateIDs<S> initDeterministic(PaigeTarjan pt,
                                                          DeterministicAutomaton<S, I, T> automaton,
                                                          Alphabet<I> inputs,
                                                          Predicate<? super S> initialClassification,
                                                          boolean sinkClassification) {
        int numStates = automaton.size();
        int numInputs = inputs.size();

        int numStatesWithSink = numStates + 1;
        int posDataLow = numStatesWithSink;
        int predOfsDataLow = posDataLow + numStatesWithSink;
        int numTransitionsFull = numStatesWithSink * numInputs;
        int predDataLow = predOfsDataLow + numTransitionsFull + 1;
        int dataSize = predDataLow + numTransitionsFull;

        int[] data = new int[dataSize];
        Block[] blockForState = new Block[numStatesWithSink];

        StateIDs<S> ids = automaton.stateIDs();

        S init = automaton.getInitialState();
        int initId = ids.getStateId(init);

        int falsePtr = 0;
        int truePtr = numStatesWithSink;

        Block falseBlock = pt.createBlock();
        Block trueBlock = pt.createBlock();

        boolean initClass = initialClassification.test(init);

        int initPos;
        if (initClass) {
            blockForState[initId] = trueBlock;
            initPos = --truePtr;
        } else {
            blockForState[initId] = falseBlock;
            initPos = falsePtr++;
        }
        data[initPos] = initId;
        data[posDataLow + initId] = initPos;

        int currFalse = 0;
        int currTrue = numStatesWithSink;

        int sinkId = numStates;
        int pending = 1;
        boolean partial = false;

        while (pending-- > 0) {
            int stateId;
            if (currFalse < falsePtr) {
                stateId = data[currFalse++];
            } else if (currTrue > truePtr) {
                stateId = data[--currTrue];
            } else {
                throw new AssertionError();
            }

            S state = ids.getState(stateId);

            int predCountBase = predOfsDataLow;

            for (int i = 0; i < numInputs; i++) {
                I sym = inputs.getSymbol(i);
                T trans = automaton.getTransition(state, sym);
                final int succId;

                if (trans == null) {
                    succId = sinkId;
                    partial = true;
                } else {
                    S succ = automaton.getSuccessor(trans);
                    succId = ids.getStateId(succ);

                    if (blockForState[succId] == null) {
                        boolean succClass = initialClassification.test(succ);
                        int succPos;
                        if (succClass) {
                            blockForState[succId] = trueBlock;
                            succPos = --truePtr;
                        } else {
                            blockForState[succId] = falseBlock;
                            succPos = falsePtr++;
                        }
                        data[succPos] = succId;
                        data[posDataLow + succId] = succPos;
                        pending++;
                    }
                }

                data[predCountBase + succId]++;
                predCountBase += numStatesWithSink;
            }
        }

        if (partial) {
            int pos;
            if (sinkClassification) {
                blockForState[sinkId] = trueBlock;
                pos = --truePtr;
            } else {
                blockForState[sinkId] = falseBlock;
                pos = falsePtr++;
            }
            data[pos] = sinkId;
            data[posDataLow + sinkId] = pos;

            int predCountIdx = predOfsDataLow + sinkId;

            for (int i = 0; i < numInputs; i++) {
                data[predCountIdx]++;
                predCountIdx += numStatesWithSink;
            }
        }

        falseBlock.low = 0;
        falseBlock.high = falsePtr;
        trueBlock.low = truePtr;
        trueBlock.high = numStatesWithSink;

        data[predOfsDataLow] += predDataLow;
        prefixSum(data, predOfsDataLow, predDataLow);

        for (int i = 0; i < falsePtr; i++) {
            int stateId = data[i];
            S state = ids.getState(stateId);
            int predOfsBase = predOfsDataLow;
            for (int j = 0; j < numInputs; j++) {
                I sym = inputs.getSymbol(j);
                T trans = automaton.getTransition(state, sym);
                int succId;
                if (trans == null) {
                    succId = sinkId;
                } else {
                    S succ = automaton.getSuccessor(trans);
                    succId = ids.getStateId(succ);
                }
                data[--data[predOfsBase + succId]] = stateId;

                predOfsBase += numStatesWithSink;
            }
        }

        for (int i = truePtr; i < numStatesWithSink; i++) {
            int stateId = data[i];
            S state = ids.getState(stateId);
            int predOfsBase = predOfsDataLow;
            for (int j = 0; j < numInputs; j++) {
                I sym = inputs.getSymbol(j);
                T trans = automaton.getTransition(state, sym);
                int succId;
                if (trans == null) {
                    succId = sinkId;
                } else {
                    S succ = automaton.getSuccessor(trans);
                    succId = ids.getStateId(succ);
                }
                data[--data[predOfsBase + succId]] = stateId;

                predOfsBase += numStatesWithSink;
            }
        }

        pt.setBlockData(data);
        pt.setPosData(data, posDataLow);
        pt.setPredOfsData(data, predOfsDataLow);
        pt.setPredData(data);
        pt.setBlockForState(blockForState);
        pt.setSize(numStatesWithSink, numInputs);

        pt.removeEmptyBlocks();

        return ids;
    }

    /**
     * This enum allows to conveniently specify how the states of a deterministic automaton are initially partitioned
     * when initializing the partition refinement data structure.
     *
     * @author Malte Isberner
     */
    public enum AutomatonInitialPartitioning {
        /**
         * States are initially partitioned by their state property, i.e., states with the same state property are
         * initially placed in the same partition class.
         */
        BY_STATE_PROPERTY {
            @Override
            public IntFunction<Object> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
                return automaton::getStateProperty;
            }

            @Override
            public <S, I> Function<S, Object> initialClassifier(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                Alphabet<I> alphabet) {
                return automaton::getStateProperty;
            }
        },

        /**
         * States are initially partitioned by all of their transition properties, i.e., states with the same input
         * symbol/transition property combinations are initially placed in the same partition class. Note that if
         * transitions are missing, a {@code null} property is assumed.
         */
        BY_TRANSITION_PROPERTIES {
            @Override
            public IntFunction<Object> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
                return (s) -> CompleteStateSignature.buildFromTransitions(automaton, s);
            }

            @Override
            public <S, I> Function<S, Object> initialClassifier(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                Alphabet<I> alphabet) {
                return (s) -> CompleteStateSignature.buildFromTransitions(automaton, alphabet, s);
            }
        },

        /**
         * States are initially partitioned by both their state properties and their transition properties. This mode
         * can be regarded as a combination of {@link #BY_STATE_PROPERTY} and {@link #BY_TRANSITION_PROPERTIES},
         * resulting in the coarsest initial partition that refines both partitions obtained using the other modes.
         */
        BY_FULL_SIGNATURE {
            @Override
            public IntFunction<Object> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton) {
                return (s) -> CompleteStateSignature.build(automaton, s);
            }

            @Override
            public <S, I> Function<S, Object> initialClassifier(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                Alphabet<I> alphabet) {
                return (s) -> CompleteStateSignature.build(automaton, alphabet, s);
            }
        };

        public abstract IntFunction<Object> initialClassifier(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton);

        public abstract <S, I> Function<S, Object> initialClassifier(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                     Alphabet<I> alphabet);
    }

    @EqualsAndHashCode
    private static final class CompleteStateSignature {

        private final Object[] properties;

        CompleteStateSignature(Object[] properties) {
            this.properties = properties;
        }

        public static CompleteStateSignature build(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                   int state) {
            int numInputs = automaton.numInputs();
            Object[] properties = new Object[numInputs + 1];
            fillTransitionProperties(automaton, state, properties);
            properties[numInputs] = automaton.getStateProperty(state);
            return new CompleteStateSignature(properties);
        }

        public static <S, I> CompleteStateSignature build(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                          Alphabet<I> alphabet,
                                                          S state) {
            int numInputs = alphabet.size();
            Object[] properties = new Object[numInputs + 1];
            fillTransitionProperties(automaton, alphabet, state, properties);
            properties[numInputs] = automaton.getStateProperty(state);
            return new CompleteStateSignature(properties);
        }

        private static void fillTransitionProperties(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                     int state,
                                                     Object[] properties) {
            int numInputs = automaton.numInputs();
            for (int i = 0; i < numInputs; i++) {
                properties[i] = automaton.getTransitionProperty(state, i);
            }
        }

        private static <S, I> void fillTransitionProperties(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                            Alphabet<I> alphabet,
                                                            S state,
                                                            Object[] properties) {
            int numInputs = alphabet.size();
            for (int i = 0; i < numInputs; i++) {
                I sym = alphabet.getSymbol(i);
                properties[i] = automaton.getTransitionProperty(state, sym);
            }
        }

        public static CompleteStateSignature buildFromTransitions(UniversalDeterministicAutomaton.FullIntAbstraction<?, ?, ?> automaton,
                                                                  int state) {
            int numInputs = automaton.numInputs();
            Object[] properties = new Object[numInputs];
            fillTransitionProperties(automaton, state, properties);
            return new CompleteStateSignature(properties);
        }

        public static <S, I> CompleteStateSignature buildFromTransitions(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
                                                                         Alphabet<I> alphabet,
                                                                         S state) {
            int numInputs = alphabet.size();
            Object[] properties = new Object[numInputs];
            for (int i = 0; i < numInputs; i++) {
                I sym = alphabet.getSymbol(i);
                properties[i] = automaton.getTransitionProperty(state, sym);
            }
            return new CompleteStateSignature(properties);
        }
    }

}
