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
package net.automatalib.serialization.ba;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
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

    public static <S, I> void writeAutomaton(FiniteStateAcceptor<S, I> automaton,
                                             Alphabet<I> alphabet,
                                             OutputStream os) throws IOException {

        final Set<TransitionTriple<S, I>> transitions = new HashSet<>();

        for (S s : automaton.getStates()) {
            for (I i : alphabet) {
                for (S succ : automaton.getSuccessors(s, i)) {
                    transitions.add(new TransitionTriple<>(s, i, succ));
                }
            }
        }

        try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
            writeInitialState(automaton, w);
            writeTransitions(automaton, transitions, w);
            writeFinalStates(automaton, w);
        }
    }

    private static <S, I> void writeInitialState(FiniteStateAcceptor<S, I> automaton,
                                           Appendable appendable) throws IOException {

        final Set<S> inits = automaton.getInitialStates();

        if (inits.size() != 1) {
            throw new IllegalArgumentException("Automaton needs to exactly specify a single initial state");
        }

        final S init = inits.iterator().next();
        final StateIDs<S> stateIds = automaton.stateIDs();
        appendable.append(Integer.toString(stateIds.getStateId(init)));
        appendable.append(System.lineSeparator());
    }

    private static <S, I> void writeTransitions(FiniteStateAcceptor<S, I> automaton,
                                                Set<TransitionTriple<S, I>> transitions,
                                                Appendable appendable) throws IOException {

        final StateIDs<S> stateIds = automaton.stateIDs();

        for (TransitionTriple<S, I> trans : transitions) {
            appendable.append(trans.input.toString());
            appendable.append(",");
            appendable.append(Integer.toString(stateIds.getStateId(trans.src)));
            appendable.append("->");
            appendable.append(Integer.toString(stateIds.getStateId(trans.dest)));
            appendable.append(System.lineSeparator());
        }
    }

    private static <S, I> void writeFinalStates(FiniteStateAcceptor<S, I> automaton,
                                                Appendable appendable) throws IOException {

        boolean allAccepting = true;
        for(S state: automaton.getStates()) {
            if (!automaton.isAccepting(state)) {
                allAccepting = false;
                break;
            }
        }

        if (allAccepting) {
            return; // if all states are accepting, don't append
        }

        final StateIDs<S> stateIds = automaton.stateIDs();
        for (S state: automaton.getStates()) {
            if (automaton.isAccepting(state)) {
                appendable.append(Integer.toString(stateIds.getStateId(state)));
                appendable.append(System.lineSeparator());
            }
        }
    }

    private static class TransitionTriple<S, I> {

        final S src;
        final I input;
        final S dest;

        TransitionTriple(S src, I input, S dest) {
            this.src = src;
            this.input = input;
            this.dest = dest;
        }
    }

}
