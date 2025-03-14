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
package net.automatalib.serialization.etf.writer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.Pair;

/**
 * Write a Mealy machine with alternating edge semantics.
 * Alternating means that a new edge (and state) is added to the LTS. So, instead of having two labels on one edge,
 * input and output are alternated. Having alternating edge semantics may change the outcomes of temporal formulae.
 *
 * @see <a href="http://rers-challenge.org/2017/index.php?page=problemDescS#">RERS 2017</a>
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public final class Mealy2ETFWriterAlternating<I, O> extends AbstractETFWriter<I, MealyMachine<?, I, ?, O>> {

    private static final Mealy2ETFWriterAlternating<?, ?> INSTANCE = new Mealy2ETFWriterAlternating<>();

    private Mealy2ETFWriterAlternating() {
        // prevent instantiation
    }

    /**
     * With alternating edge semantics, there are only edges with one label. Both input and output of the Mealy
     * machine is generalized to a label named 'letter', of type 'letter'.
     *
     * @param pw the Writer.
     */
    @Override
    protected void writeEdge(PrintWriter pw) {
        pw.println("begin edge");
        pw.println("letter:letter");
        pw.println("end edge");
    }

    /**
     * Write the specific parts of the ETF for Mealy machines with alternating edge semantics.
     * <p>
     * Writes:
     *  - the initial state,
     *  - the transitions,
     *  - the valuations for the state ids,
     *  - the letters from the alphabet.
     * <p>
     * Note that in this context, the alphabet that is written to ETF is not just the inputs, it is the union of
     * inputs and outputs, of type 'letter'.
     *
     * @param pw the Writer.
     * @param mealy the MealyMachine to write to ETF.
     * @param inputs the alphabet, the input alphabet.
     */
    @Override
    protected void writeETF(PrintWriter pw, MealyMachine<?, I, ?, O> mealy, Alphabet<I> inputs) {
        writeETFInternal(pw, mealy, inputs);
    }

    private <S, T> void writeETFInternal(PrintWriter pw, MealyMachine<S, I, T, O> mealy, Alphabet<I> inputs) {

        final StateIDs<S> oldStateIDs = mealy.stateIDs();
        final S init = mealy.getInitialState();

        // write the initial state, using the bi-map
        pw.println("begin init");
        if (init != null) {
            pw.printf("%d%n", oldStateIDs.getStateId(init));
        }
        pw.println("end init");

        // create a (insertion stable) map for transitions containing output
        final Map<Pair<O, S>, Integer> outputTransitions = new LinkedHashMap<>();

        // create a (insertion stable) map that maps output to integers
        final Map<O, Integer> outputIndices = new LinkedHashMap<>();

        /*
         Write the transitions (here is where the horror begins).
         The key to writing transitions with alternating semantics is that one have of see if appropriate
         intermediate states, and output transitions have already been created. If this is the case, that state, and
         output transitions has to be reused.
         */
        pw.println("begin trans");
        for (S s : mealy.getStates()) {
            for (I i : inputs) {
                T t = mealy.getTransition(s, i);
                if (t != null) {
                    final S n = mealy.getSuccessor(t);
                    final O o = mealy.getTransitionOutput(t);

                    // construct a triple that serves as a key in the outputTransitions bi-map
                    final Pair<O, S> outputTransition = Pair.of(o, n);

                    // compute the integer value of the intermediate state (this may be a new state)
                    final Integer intermediateState = outputTransitions.computeIfAbsent(
                            outputTransition, ii -> {

                                // the output may also be a new letter in the alphabet.
                                final Integer outputIndex = outputIndices.computeIfAbsent(
                                        o,
                                        iii -> inputs.size() + outputIndices.size());

                                /*
                                Write the output transition. Note that this will only be done if the output
                                transition was not written before.
                                */
                                final Integer res = mealy.size() + outputTransitions.size();
                                pw.printf("%d/%d %d%n", res, oldStateIDs.getStateId(n), outputIndex);
                                return res;
                            });

                    // always write the input transition to the output transition
                    pw.printf(
                            "%d/%d %d%n",
                            oldStateIDs.getStateId(s),
                            intermediateState,
                            inputs.getSymbolIndex(i));
                }
            }
        }
        pw.println("end trans");

        // write all state ids, including the newly created intermediate states
        pw.println("begin sort id");
        for (int i = 0; i < mealy.size(); i++) {
            pw.printf("\"%s\"%n", oldStateIDs.getState(i));
        }

        for (Pair<O, S> t : outputTransitions.keySet()) {
            pw.printf("\"(%s,%s)\"%n", t.getFirst(), t.getSecond());
        }
        pw.println("end sort");

        // write all the letters in the new alphabet
        pw.println("begin sort letter");
        inputs.forEach(i -> pw.printf("\"%s\"%n", i));
        for (O o : outputIndices.keySet()) {
            pw.printf("\"%s\"%n", o);
        }
        pw.println("end sort");
    }

    @Override
    public void writeModel(OutputStream os, MealyMachine<?, I, ?, O> model, Alphabet<I> alphabet) {
        try (PrintWriter pw = new PrintWriter(IOUtil.asNonClosingUTF8Writer(os))) {
            write(pw, model, alphabet);
        }
    }

    @SuppressWarnings("unchecked")
    public static <I, O> Mealy2ETFWriterAlternating<I, O> getInstance() {
        return (Mealy2ETFWriterAlternating<I, O>) INSTANCE;
    }
}
