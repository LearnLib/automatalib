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
package net.automatalib.serialization.saf;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.common.util.io.NonClosingInputStream;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;

/**
 * Abstract deserializer for the SAF (simple automaton format).
 */
abstract class AbstractSAFInput<S, I, T, SP, TP, A extends MutableAutomaton<S, I, T, SP, TP>>
        implements InputModelDeserializer<I, A> {

    private static final AutomatonType[] TYPES = AutomatonType.values();

    private final AutomatonType expectedType;
    private final AutomatonCreator<? extends A, I> creator;
    private final BlockPropertyDecoder<? extends SP> spDecoder;
    private final SinglePropertyDecoder<? extends TP> tpDecoder;

    AbstractSAFInput(AutomatonType expectedType,
                     AutomatonCreator<? extends A, I> creator,
                     BlockPropertyDecoder<? extends SP> spDecoder,
                     SinglePropertyDecoder<? extends TP> tpDecoder) {
        this.expectedType = expectedType;
        this.creator = creator;
        this.spDecoder = spDecoder;
        this.tpDecoder = tpDecoder;
    }

    protected abstract Alphabet<I> getAlphabet(int size);

    @Override
    public InputModelData<I, A> readModel(InputStream is) throws IOException, FormatException {
        try (DataInputStream in = new DataInputStream(new NonClosingInputStream(is))) {
            final AutomatonType type = readHeader(in);

            if (type != expectedType) {
                throw new FormatException();
            }

            final int alphabetSize = in.readInt();

            if (alphabetSize <= 0) {
                throw new FormatException();
            }

            final Alphabet<I> alphabet = getAlphabet(alphabetSize);
            final A automaton = readAutomatonBody(in, alphabet, type.isDeterministic(), creator, spDecoder, tpDecoder);

            return new InputModelData<>(automaton, alphabet);
        }
    }

    private AutomatonType readHeader(DataInput in) throws IOException, FormatException {
        final int headerSize = 4;
        byte[] header = new byte[headerSize];
        in.readFully(header);
        if (header[0] != 'S' || header[1] != 'A' || header[2] != 'F') {
            throw new FormatException();
        }
        byte type = header[3];
        if (type < 0 || type >= TYPES.length) {
            throw new FormatException();
        }
        return TYPES[type];
    }

    private A readAutomatonBody(DataInput in,
                                Alphabet<I> alphabet,
                                boolean deterministic,
                                AutomatonCreator<? extends A, I> creator,
                                BlockPropertyDecoder<? extends SP> spDecoder,
                                SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
        int numStates = in.readInt();
        A result = creator.createAutomaton(alphabet, numStates);

        if (deterministic) {
            decodeBodyDet(in, result, alphabet, numStates, spDecoder, tpDecoder);
        } else {
            decodeBodyNondet(in, result, alphabet, numStates, spDecoder, tpDecoder);
        }

        return result;
    }

    private void decodeBodyDet(DataInput in,
                               MutableAutomaton<S, I, ?, SP, TP> result,
                               Alphabet<I> alphabet,
                               int numStates,
                               BlockPropertyDecoder<? extends SP> spDecoder,
                               SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {

        List<S> stateList = decodeStatesDet(in, result, numStates, spDecoder);
        decodeTransitionsDet(in, result, stateList, alphabet, tpDecoder);
    }

    private void decodeBodyNondet(DataInput in,
                                  MutableAutomaton<S, I, ?, SP, TP> result,
                                  Alphabet<I> alphabet,
                                  int numStates,
                                  BlockPropertyDecoder<? extends SP> spDecoder,
                                  SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {

        List<S> stateList = decodeStatesNondet(in, result, numStates, spDecoder);
        decodeTransitionsNondet(in, result, stateList, alphabet, tpDecoder);
    }

    private List<S> decodeStatesDet(DataInput in,
                                    MutableAutomaton<S, ?, ?, SP, ?> result,
                                    int numStates,
                                    BlockPropertyDecoder<? extends SP> decoder) throws IOException {
        int initStateId = in.readInt();

        List<S> stateList = decodeStateProperties(in, result, numStates, decoder);

        S initState = stateList.get(initStateId);

        result.setInitial(initState, true);

        return stateList;
    }

    private void decodeTransitionsDet(DataInput in,
                                      MutableAutomaton<S, I, ?, ?, TP> result,
                                      List<S> stateList,
                                      Alphabet<I> alphabet,
                                      SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
        int numStates = stateList.size();
        assert result.size() == numStates;

        int numInputs = alphabet.size();

        for (S state : stateList) {
            for (int j = 0; j < numInputs; j++) {
                int tgt = in.readInt();
                if (tgt != -1) {
                    I sym = alphabet.getSymbol(j);
                    S tgtState = stateList.get(tgt);
                    TP prop = tpDecoder.readProperty(in);
                    result.addTransition(state, sym, tgtState, prop);
                }
            }
        }
    }

    private List<S> decodeStatesNondet(DataInput in,
                                       MutableAutomaton<S, ?, ?, SP, ?> result,
                                       int numStates,
                                       BlockPropertyDecoder<? extends SP> decoder) throws IOException {
        int[] initStates = readInts(in);

        List<S> stateList = decodeStateProperties(in, result, numStates, decoder);

        for (int initId : initStates) {
            S initState = stateList.get(initId);
            result.setInitial(initState, true);
        }

        return stateList;
    }

    private void decodeTransitionsNondet(DataInput in,
                                         MutableAutomaton<S, I, ?, ?, TP> result,
                                         List<S> stateList,
                                         Alphabet<I> alphabet,
                                         SinglePropertyDecoder<? extends TP> tpDecoder) throws IOException {
        int numStates = stateList.size();
        assert result.size() == numStates;

        int numInputs = alphabet.size();

        for (S state : stateList) {
            for (int j = 0; j < numInputs; j++) {
                int numTgts = in.readInt();
                I sym = alphabet.getSymbol(j);
                for (int k = 0; k < numTgts; k++) {
                    int tgt = in.readInt();
                    TP prop = tpDecoder.readProperty(in);
                    S tgtState = stateList.get(tgt);
                    result.addTransition(state, sym, tgtState, prop);
                }
            }
        }
    }

    private List<S> decodeStateProperties(DataInput in,
                                          MutableAutomaton<S, ?, ?, SP, ?> result,
                                          int numStates,
                                          BlockPropertyDecoder<? extends SP> decoder) throws IOException {
        List<S> stateList = new ArrayList<>(numStates);

        decoder.start(in);
        for (int i = 0; i < numStates; i++) {
            SP prop = decoder.readProperty(in);
            S state = result.addState(prop);
            stateList.add(state);
        }
        decoder.finish(in);

        return stateList;
    }

    private static int[] readInts(DataInput in) throws IOException {
        int n = in.readInt();
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = in.readInt();
        }
        return result;
    }
}
