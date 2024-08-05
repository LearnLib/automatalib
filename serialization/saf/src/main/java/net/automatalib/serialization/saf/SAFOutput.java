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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.common.util.io.NonClosingOutputStream;
import net.automatalib.serialization.InputModelSerializer;

class SAFOutput<S, I, T, SP, TP, M extends UniversalAutomaton<S, I, T, SP, TP>> implements InputModelSerializer<I, M> {

    private final AutomatonType expectedType;
    private final BlockPropertyEncoder<? super SP> spEncoder;
    private final SinglePropertyEncoder<? super TP> tpEncoder;

    SAFOutput(AutomatonType expectedType,
              BlockPropertyEncoder<? super SP> spEncoder,
              SinglePropertyEncoder<? super TP> tpEncoder) {
        this.expectedType = expectedType;
        this.spEncoder = spEncoder;
        this.tpEncoder = tpEncoder;
    }

    @Override
    public void writeModel(OutputStream os, M model, Alphabet<I> alphabet) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new NonClosingOutputStream(os))) {
            writeHeader(out, expectedType);
            out.writeInt(alphabet.size());
            writeAutomatonBody(out, model, alphabet, expectedType.isDeterministic(), spEncoder, tpEncoder);
        }
    }

    private void writeHeader(DataOutput out, AutomatonType type) throws IOException {
        out.writeByte('S');
        out.writeByte('A');
        out.writeByte('F');
        out.writeByte(type.ordinal());
    }

    private void writeAutomatonBody(DataOutput out,
                                    M automaton,
                                    Alphabet<I> alphabet,
                                    boolean deterministic,
                                    BlockPropertyEncoder<? super SP> spDecoder,
                                    SinglePropertyEncoder<? super TP> tpDecoder) throws IOException {

        final int numStates = automaton.size();
        out.writeInt(numStates);

        if (deterministic) {
            encodeBodyDet(out, automaton, alphabet, spDecoder, tpDecoder);
        } else {
            encodeBodyNondet(out, automaton, alphabet, spDecoder, tpDecoder);
        }
    }

    private void encodeBodyDet(DataOutput out,
                               M result,
                               Alphabet<I> alphabet,
                               BlockPropertyEncoder<? super SP> spEncoder,
                               SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {

        final Set<S> initials = result.getInitialStates();

        if (initials.size() != 1) {
            throw new IllegalArgumentException();
        }

        final List<S> states = new ArrayList<>(result.getStates());
        final S init = initials.iterator().next();

        encodeStatesDet(out, result, init, states, spEncoder);
        encodeTransitionsDet(out, result, alphabet, states, tpEncoder);
    }

    private void encodeBodyNondet(DataOutput out,
                                  M source,
                                  Alphabet<I> alphabet,
                                  BlockPropertyEncoder<? super SP> spEncoder,
                                  SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {

        final List<S> states = new ArrayList<>(source.getStates());
        final Set<S> initials = source.getInitialStates();

        encodeStatesNondet(out, source, initials, states, spEncoder);
        encodeTransitionsNondet(out, source, alphabet, states, tpEncoder);
    }

    private void encodeStatesDet(DataOutput out,
                                 M source,
                                 S init,
                                 List<S> states,
                                 BlockPropertyEncoder<? super SP> encoder) throws IOException {
        out.writeInt(states.indexOf(init));
        encodeStateProperties(out, source, states, encoder);
    }

    private void encodeTransitionsDet(DataOutput out,
                                      M source,
                                      Alphabet<I> alphabet,
                                      List<S> stateList,
                                      SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {
        for (S state : stateList) {
            for (int j = 0; j < alphabet.size(); j++) {
                final I sym = alphabet.getSymbol(j);

                final Collection<T> succs = source.getTransitions(state, sym);

                if (succs.size() > 1) {
                    throw new IllegalArgumentException("Not deterministic");
                }

                // undefined succ
                if (succs.isEmpty()) {
                    out.writeInt(-1);
                } else {
                    final T succ = succs.iterator().next();
                    final S succState = source.getSuccessor(succ);

                    assert succState != null;

                    final int tgt = stateList.indexOf(succState);

                    out.writeInt(tgt);
                    tpEncoder.writeProperty(out, source.getTransitionProperty(succ));
                }
            }
        }
    }

    private void encodeStatesNondet(DataOutput out,
                                    M source,
                                    Collection<? extends S> initialStates,
                                    List<S> states,
                                    BlockPropertyEncoder<? super SP> encoder) throws IOException {
        // 'writeInts'
        out.writeInt(initialStates.size());

        for (S s : initialStates) {
            out.writeInt(states.indexOf(s));
        }
        // end 'writeInts'

        encodeStateProperties(out, source, states, encoder);
    }

    private void encodeTransitionsNondet(DataOutput out,
                                         M source,
                                         Alphabet<I> alphabet,
                                         List<S> stateList,
                                         SinglePropertyEncoder<? super TP> tpEncoder) throws IOException {
        for (S state : stateList) {
            for (int j = 0; j < alphabet.size(); j++) {
                final I sym = alphabet.getSymbol(j);

                final Collection<T> succs = source.getTransitions(state, sym);

                out.writeInt(succs.size());

                for (T t : succs) {
                    final S succState = source.getSuccessor(t);
                    out.writeInt(stateList.indexOf(succState));
                    tpEncoder.writeProperty(out, source.getTransitionProperty(t));
                }
            }
        }
    }

    private void encodeStateProperties(DataOutput out,
                                       M source,
                                       List<S> states,
                                       BlockPropertyEncoder<? super SP> encoder) throws IOException {
        encoder.start(out);

        for (S s : states) {
            encoder.encodeProperty(out, source.getStateProperty(s));
        }

        encoder.finish(out);
    }
}
