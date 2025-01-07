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
package net.automatalib.modelchecking.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.concept.DetOutputAutomaton;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractLasso<I, D> implements Lasso<I, D> {

    public static final String NO_LASSO = "Automaton is not lasso shaped";

    private final Word<I> word;
    private final Word<I> loop;
    private final Word<I> prefix;
    private final D output;
    private final Alphabet<I> inputAlphabet;
    private final int unfolds;
    private final SortedSet<Integer> loopBeginIndices = new TreeSet<>();
    private final DetOutputAutomaton<?, I, ?, D> automaton;

    /**
     * Constructs a finite representation of a given automaton (that contains a lasso), by unrolling the loop
     * {@code unfoldTimes}.
     *
     * @param automaton
     *         the automaton containing the lasso.
     * @param inputs
     *         the input alphabet.
     * @param unfoldTimes
     *         the number of times the loop needs to be unrolled, must be {@code > 0}.
     * @param <S>
     *         the state type
     */
    public <S> AbstractLasso(DetOutputAutomaton<S, I, ?, D> automaton,
                             Collection<? extends I> inputs,
                             int unfoldTimes) {
        this(validateLassoShape(automaton, inputs, unfoldTimes), automaton, inputs, unfoldTimes);
    }

    // utility constructor to prevent finalizer attacks, see SEI CERT Rule OBJ-11
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private <S> AbstractLasso(boolean valid,
                              DetOutputAutomaton<S, I, ?, D> automaton,
                              Collection<? extends I> inputs,
                              int unfoldTimes) {
        // save the original automaton
        this.automaton = automaton;

        this.unfolds = unfoldTimes;

        // construct the input alphabet
        inputAlphabet = Alphabets.fromCollection(inputs);

        // create a map for the visited states
        final Map<S, Integer> states = new HashMap<>();

        // create a WordBuilder, for the finite representation of the lasso
        final WordBuilder<I> wb = new WordBuilder<>();

        // start visiting the initial state
        @SuppressWarnings("nullness") // we have checked non-nullness of the initial state
        @NonNull S current = automaton.getInitialState();

        // index for the current state
        int i = 0;
        do {
            // create a mapping from the current state to the state index
            states.put(current, i++);

            // find the input that leads to the next state
            for (I in : inputAlphabet) {
                final S succ = automaton.getSuccessor(current, in);
                if (succ != null) {
                    // append the input to the finite representation
                    wb.append(in);

                    // continue with the next state.
                    current = succ;
                    break;
                }
            }
        } while (!states.containsKey(current));

        // save the state index at which the loop begins
        final int loopBegin = states.get(current);

        // determine the loop of the lasso
        loop = wb.toWord(loopBegin, wb.size());

        // determine the prefix of the lasso
        prefix = wb.toWord(0, loopBegin);

        // append the loop several times to the finite representation
        for (int u = 1; u < unfoldTimes; u++) {
            wb.append(loop);
        }

        // store the entire finite representation of the lasso
        word = wb.toWord();

        // store the finite representation of output of the lasso
        output = automaton.computeOutput(word);

        // store all the symbol indices after which the beginning of the loop is visited.
        for (int l = prefix.length(); l <= word.length(); l += loop.length()) {
            loopBeginIndices.add(l);
        }
    }

    @Override
    public DetOutputAutomaton<?, I, ?, D> getAutomaton() {
        return automaton;
    }

    @Override
    public int getUnfolds() {
        return unfolds;
    }

    @Override
    public Word<I> getWord() {
        return word;
    }

    @Override
    public Word<I> getLoop() {
        return loop;
    }

    @Override
    public Word<I> getPrefix() {
        return prefix;
    }

    @Override
    public D getOutput() {
        return output;
    }

    @Override
    public SortedSet<Integer> getLoopBeginIndices() {
        return loopBeginIndices;
    }

    @Override
    public Integer getInitialState() {
        return 0;
    }

    /**
     * Get the successor state of a given state, or {@code null} when no such successor exists.
     *
     * @see SimpleDTS#getSuccessor(Object, Object)
     */
    @Override
    public @Nullable Integer getSuccessor(Integer state, I input) {
        final Integer result;
        if (state < word.length() && Objects.equals(input, word.getSymbol(state))) {
            result = state + 1;
        } else {
            result = null;
        }

        return result;
    }

    @Override
    public Collection<Integer> getStates() {
        return CollectionUtil.intRange(0, word.length());
    }

    /**
     * Gets the input alphabet of this automaton.
     *
     * @return the Alphabet.
     */
    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public @Nullable Integer getTransition(Integer state, I input) {
        return getSuccessor(state, input);
    }

    private static <S, I, D> boolean validateLassoShape(DetOutputAutomaton<S, I, ?, D> automaton,
                                                        Collection<? extends I> inputs,
                                                        int unfoldTimes) {
        if (unfoldTimes <= 0) {
            throw new AssertionError();
        }

        if (automaton.getInitialState() == null) {
            throw new IllegalArgumentException(NO_LASSO);
        }

        states:
        for (S s : automaton) {
            for (I i : inputs) {
                if (automaton.getSuccessor(s, i) != null) {
                    continue states;
                }
            }
            throw new IllegalArgumentException(NO_LASSO);
        }

        return true;
    }
}
