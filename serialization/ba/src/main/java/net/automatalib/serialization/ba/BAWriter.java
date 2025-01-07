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
package net.automatalib.serialization.ba;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.FiniteStateAcceptor;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.InputModelSerializer;

/**
 * A writer that exports automata to the BA format. For further information about the BA format, see <a
 * href="https://languageinclusion.org/doku.php?id=tools#the_ba_format">
 * https://languageinclusion.org/doku.php?id=tools#the_ba_format</a>.
 */
public final class BAWriter<I> implements InputModelSerializer<I, FiniteStateAcceptor<?, I>> {

    @Override
    public void writeModel(OutputStream os, FiniteStateAcceptor<?, I> model, Alphabet<I> alphabet) throws IOException {
        writeAutomaton(model, alphabet, os);
    }

    public static <S, I> void writeAutomaton(FiniteStateAcceptor<S, I> automaton, Alphabet<I> alphabet, OutputStream os)
            throws IOException {

        final StateIDs<S> stateIds = automaton.stateIDs();

        try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
            writeInitialState(automaton, stateIds, w);
            writeTransitions(automaton, stateIds, alphabet, w);
            writeFinalStates(automaton, stateIds, w);
        }
    }

    private static <S, I> void writeInitialState(FiniteStateAcceptor<S, I> automaton,
                                                 StateIDs<S> stateIds,
                                                 Appendable appendable) throws IOException {

        final Set<S> inits = automaton.getInitialStates();

        if (inits.size() == 1) {
            final S init = inits.iterator().next();
            appendable.append(Integer.toString(stateIds.getStateId(init))).append(System.lineSeparator());
        } else if (inits.size() > 1) {
            throw new IllegalArgumentException("The BA format only allows for at most one initial state");
        }
    }

    private static <S, I> void writeTransitions(FiniteStateAcceptor<S, I> automaton,
                                                StateIDs<S> stateIds,
                                                Alphabet<I> alphabet,
                                                Appendable appendable) throws IOException {
        for (S src : automaton) {
            for (I label : alphabet) {
                for (S tgt : automaton.getSuccessors(src, label)) {
                    appendable.append(Objects.toString(label))
                              .append(',')
                              .append(Integer.toString(stateIds.getStateId(src)))
                              .append("->")
                              .append(Integer.toString(stateIds.getStateId(tgt)))
                              .append(System.lineSeparator());
                }
            }
        }
    }

    private static <S, I> void writeFinalStates(FiniteStateAcceptor<S, I> automaton,
                                                StateIDs<S> stateIds,
                                                Appendable appendable) throws IOException {

        boolean allAccepting = true;
        for (S state : automaton.getStates()) {
            if (!automaton.isAccepting(state)) {
                allAccepting = false;
                break;
            }
        }

        if (!allAccepting) {
            for (S state : automaton.getStates()) {
                if (automaton.isAccepting(state)) {
                    appendable.append(Integer.toString(stateIds.getStateId(state))).append(System.lineSeparator());
                }
            }
        }
    }
}
