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
package net.automatalib.serialization.learnlibv2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.annotation.WillNotClose;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelSerializationProvider;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class LearnLibV2Serialization
        implements InputModelSerializationProvider<Integer, DFA<?, Integer>, DFA<Integer, Integer>> {

    private static final LearnLibV2Serialization INSTANCE = new LearnLibV2Serialization();

    public static LearnLibV2Serialization getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeModel(OutputStream os, DFA<?, Integer> model, Alphabet<Integer> alphabet) {
        doWriteDFA(model, alphabet, os);
    }

    public <I> void writeGenericModel(OutputStream os, DFA<?, I> model, Alphabet<I> alphabet) {
        doWriteDFA(model, alphabet, os);
    }

    @Override
    public InputModelData<Integer, DFA<Integer, Integer>> readModel(InputStream is) throws IOException {
        final CompactDFA<Integer> automaton = readGenericDFA(is);
        return new InputModelData<>(automaton, automaton.getInputAlphabet());
    }

    public CompactDFA<Integer> readGenericDFA(@WillNotClose InputStream is) throws IOException {
        // we DO NOT want to close the input stream
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(IOUtil.asUncompressedInputStream(is), StandardCharsets.UTF_8.toString());

        int numStates = sc.nextInt();
        int numSymbols = sc.nextInt();

        Alphabet<Integer> alphabet = Alphabets.integers(0, numSymbols - 1);

        CompactDFA<Integer> result = new CompactDFA<>(alphabet, numStates);

        // This is redundant in practice, but it is in fact not specified by CompactDFA
        // how state IDs are assigned
        int[] states = new int[numStates];

        // Parse states
        states[0] = result.addIntInitialState(sc.nextInt() != 0);

        for (int i = 1; i < numStates; i++) {
            states[i] = result.addIntState(sc.nextInt() != 0);
        }

        // Parse transitions
        for (int i = 0; i < numStates; i++) {
            int state = states[i];
            for (int j = 0; j < numSymbols; j++) {
                int succ = states[sc.nextInt()];
                result.setTransition(state, j, succ);
            }
        }

        return result;
    }

    private <S, I> void doWriteDFA(DFA<S, I> dfa, Alphabet<I> alphabet, OutputStream os) {
        boolean partial = Automata.hasUndefinedInput(dfa, alphabet);
        int numDfaStates = dfa.size();
        int numStates = numDfaStates;
        if (partial) {
            numStates++;
        }
        PrintStream ps;
        try {
            ps = new PrintStream(os, false, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // this should in theory never happen
            throw new IllegalStateException(e);
        }
        int numInputs = alphabet.size();
        ps.printf("%d %d%n", numStates, numInputs);

        StateIDs<S> stateIds = dfa.stateIDs();

        S initState = dfa.getInitialState();
        int initId = stateIds.getStateId(initState);

        List<S> orderedStates = new ArrayList<>(numDfaStates);
        orderedStates.add(initState);

        ps.printf("%d ", dfa.isAccepting(initState) ? 1 : 0);

        for (int i = 1; i < numDfaStates; i++) {
            S state = stateIds.getState(i);
            if (i == initId) {
                state = stateIds.getState(0);
            }
            ps.printf("%d ", dfa.isAccepting(state) ? 1 : 0);
            orderedStates.add(state);
        }
        if (partial) {
            ps.print("0");
        }
        ps.println();
        for (S state : orderedStates) {
            for (I sym : alphabet) {
                S target = dfa.getSuccessor(state, sym);
                int targetId = numDfaStates;
                if (target != null) {
                    targetId = stateIds.getStateId(target);
                    if (targetId == initId) {
                        targetId = 0;
                    } else if (targetId == 0) {
                        targetId = initId;
                    }
                }
                ps.printf("%d ", targetId);
            }
            ps.println();
        }
        if (partial) {
            for (int i = 0; i < numInputs; i++) {
                ps.printf("%d ", numDfaStates);
            }
            ps.println();
        }
    }
}
