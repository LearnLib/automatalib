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
package net.automatalib.util.automata.fsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;

public final class NFAs {

    private NFAs() {
    }

    public static <I> CompactDFA<I> determinize(NFA<?, I> nfa, Alphabet<I> inputAlphabet) {
        return determinize(nfa, inputAlphabet, false, true);
    }

    public static <I> CompactDFA<I> determinize(NFA<?, I> nfa,
                                                Alphabet<I> inputAlphabet,
                                                boolean partial,
                                                boolean minimize) {
        CompactDFA<I> result = new CompactDFA<>(inputAlphabet);
        determinize(nfa, inputAlphabet, result, partial, minimize);
        return result;
    }

    public static <I> void determinize(NFA<?, I> nfa,
                                       Collection<? extends I> inputs,
                                       MutableDFA<?, I> out,
                                       boolean partial,
                                       boolean minimize) {
        doDeterminize(nfa, inputs, out, partial);
        if (minimize) {
            Automata.invasiveMinimize(out, inputs);
        }
    }

    public static <I, A extends NFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> determinize(A nfa) {
        return determinize(nfa, false, true);
    }

    public static <I, A extends NFA<?, I> & InputAlphabetHolder<I>> CompactDFA<I> determinize(A nfa,
                                                                                              boolean partial,
                                                                                              boolean minimize) {
        return determinize(nfa, nfa.getInputAlphabet(), partial, minimize);
    }

    public static <I> void determinize(NFA<?, I> nfa, Collection<? extends I> inputs, MutableDFA<?, I> out) {
        determinize(nfa, inputs, out, false, true);
    }

    private static <I, SI, SO> void doDeterminize(NFA<SI, I> nfa,
                                                  Collection<? extends I> inputs,
                                                  MutableDFA<SO, I> out,
                                                  boolean partial) {

        Map<BitSet, SO> outStateMap = new HashMap<>();
        StateIDs<SI> stateIds = nfa.stateIDs();

        Deque<DeterminizeRecord<SI, SO>> stack = new ArrayDeque<>();

        List<SI> initList = new ArrayList<>(nfa.getInitialStates());
        BitSet initBs = new BitSet();
        for (SI init : initList) {
            initBs.set(stateIds.getStateId(init));
        }

        boolean initAcc = nfa.isAccepting(initList);
        SO initOut = out.addInitialState(initAcc);

        outStateMap.put(initBs, initOut);

        stack.push(new DeterminizeRecord<>(initList, initOut));

        while (!stack.isEmpty()) {
            DeterminizeRecord<SI, SO> curr = stack.pop();

            List<SI> inStates = curr.inputStates;
            SO outState = curr.outputState;

            for (I sym : inputs) {
                BitSet succBs = new BitSet();
                List<SI> succList = new ArrayList<>();

                for (SI inState : inStates) {
                    for (SI succState : nfa.getSuccessors(inState, sym)) {
                        int succId = stateIds.getStateId(succState);
                        if (!succBs.get(succId)) {
                            succBs.set(succId);
                            succList.add(succState);
                        }
                    }
                }

                if (!partial || !succList.isEmpty()) {
                    SO outSucc = outStateMap.get(succBs);
                    if (outSucc == null) {
                        outSucc = out.addState(nfa.isAccepting(succList));
                        outStateMap.put(succBs, outSucc);
                        stack.push(new DeterminizeRecord<>(succList, outSucc));
                    }
                    out.setTransition(outState, sym, outSucc);
                }
            }
        }

    }

    private static final class DeterminizeRecord<SI, SO> {

        private final List<SI> inputStates;
        private final SO outputState;

        DeterminizeRecord(List<SI> inputStates, SO outputState) {
            this.inputStates = inputStates;
            this.outputState = outputState;
        }
    }
}
