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
package net.automatalib.alphabet;

import java.util.List;

import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A specialized version of a {@link VPAlphabet} that is tailored towards procedural systems. Specifically, it only
 * supports a {@link #getReturnSymbol() single return symbol}.
 *
 * @param <I>
 *         input symbol type
 */
public interface ProceduralInputAlphabet<I> extends VPAlphabet<I> {

    /**
     * Returns the union of {@link #getCallAlphabet() call} and {@link #getInternalAlphabet() internal} symbols.
     *
     * @return the procedural alphabet
     */
    Alphabet<I> getProceduralAlphabet();

    /**
     * Returns the single return symbol.
     *
     * @return the single return symbol
     */
    default I getReturnSymbol() {
        return getReturnSymbol(0);
    }

    /**
     * Convenience method for {@link #findCallIndex(List, int)} that transforms the given input {@link Word word} into a
     * {@link Word#asList() list}.
     *
     * @param input
     *         the input word
     * @param idx
     *         the index of the currently executing symbol for which the call index should be determined.
     *
     * @return the index of the respective call symbol or {@code -1} if this index doesn't exist
     *
     * @see #findCallIndex(List, int)
     */
    default int findCallIndex(Word<I> input, int idx) {
        return findCallIndex(input.asList(), idx);
    }

    /**
     * Returns the index of the call symbol of the procedure currently executing the symbol at pos {@code idx}.
     *
     * @param input
     *         the input sequence
     * @param idx
     *         the index of the currently executing symbol for which the call index should be determined.
     *
     * @return the index of the call symbol or {@code -1} if this index doesn't exist or {@code idx} has invalid range
     */
    default int findCallIndex(List<I> input, int idx) {

        if (idx > input.size()) {
            return -1;
        }

        int balance = 0;

        for (int i = idx - 1; i >= 0; i--) {
            final I sym = input.get(i);

            if (isReturnSymbol(sym)) {
                balance++;
            }

            if (isCallSymbol(sym)) {
                if (balance > 0) {
                    balance--;
                } else {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Convenience method for {@link #findReturnIndex(List, int)} that transforms the given input {@link Word word} into
     * a {@link Word#asList() list}.
     *
     * @param input
     *         the input word
     * @param idx
     *         the index of the symbol that is about to be executed by the current procedure for which the return symbol
     *         should be determined.
     *
     * @return the index of the return symbol or {@code -1} if this index doesn't exist or {@code idx} has invalid range
     *
     * @see #findReturnIndex(List, int)
     */
    default int findReturnIndex(Word<I> input, int idx) {
        return findReturnIndex(input.asList(), idx);
    }

    /**
     * Returns the index of the return symbol of the procedure that is about to execute the symbol at pos {@code idx}
     * (i.e. before input[idx] has been executed).
     *
     * @param input
     *         the input sequence
     * @param idx
     *         the index of the symbol that is about to be executed by the current procedure for which the return symbol
     *         should be determined.
     *
     * @return the index of the return symbol or {@code -1} if this index doesn't exist
     */
    default int findReturnIndex(List<I> input, int idx) {

        if (idx < 0) {
            return -1;
        }

        int balance = 0;

        for (int i = idx; i < input.size(); i++) {
            final I sym = input.get(i);

            if (isCallSymbol(sym)) {
                balance++;
            }

            if (isReturnSymbol(sym)) {
                if (balance > 0) {
                    balance--;
                } else {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Replaces all occurrences of {@link #getCallAlphabet() call symbols} in {@code input} with an embedded terminating
     * sequence provided by {@code terminatingSequences}.
     *
     * @param input
     *         the input sequence to analyze
     * @param terminatingSequences
     *         a mapping of terminating sequence
     *
     * @return a transformed word where all occurrences of {@link #getCallAlphabet() calls symbols} have been replaced
     * with embedded terminating sequences.
     */
    default Word<I> expand(Iterable<I> input, Mapping<I, Word<I>> terminatingSequences) {
        final WordBuilder<I> wb = new WordBuilder<>();

        for (I sym : input) {
            if (isCallSymbol(sym)) {
                wb.append(sym);
                wb.append(terminatingSequences.get(sym));
                wb.append(getReturnSymbol());
            } else {
                wb.append(sym);
            }
        }

        return wb.toWord();
    }

    /**
     * Replaces all well-matched occurrences of procedural invocations in {@code input} with the single respective
     * {@link #getCallAlphabet() call symbol}.
     *
     * @param input
     *         the input word to analyze
     * @param idx
     *         the index from {@code input} will be analyzed
     *
     * @return a transformed word where all well-matched occurrences of procedural invocations have been replaced with
     * the single respective {@link #getCallAlphabet() call symbols}.
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables") // we want to skip indices here
    default Word<I> project(Word<I> input, int idx) {
        final WordBuilder<I> wb = new WordBuilder<>(input.size());

        for (int i = Math.max(0, idx); i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            wb.append(input.getSymbol(i));

            if (isCallSymbol(sym)) {
                final int returnIdx = findReturnIndex(input, i + 1);
                if (returnIdx > -1) { // found matching return
                    i = returnIdx;
                }
            }
        }

        return wb.toWord();
    }

    /**
     * A generalization of {@link #project(Word, int)} which applies the transformation to the input word as well as an
     * output word (in a symbol-wise fashion).
     *
     * @param input
     *         the input word to analyze
     * @param output
     *         the output word to transform as well
     * @param idx
     *         the index from {@code input} will be analyzed
     * @param <O>
     *         output symbol type
     *
     * @return a pair of transformed words where all well-matched occurrences of procedural invocations have been
     * replaced with the single respective {@link #getCallAlphabet() call symbol} / call symbol output.
     *
     * @see #project(Word, int)
     */
    @SuppressWarnings("PMD.AvoidReassigningLoopVariables") // we want to skip indices here
    default <O> Pair<Word<I>, Word<O>> project(Word<I> input, Word<O> output, int idx) {
        assert input.size() == output.size();
        final WordBuilder<I> inBuilder = new WordBuilder<>(input.size() - idx);
        final WordBuilder<O> outBuilder = new WordBuilder<>(input.size() - idx);

        for (int i = Math.max(0, idx); i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            inBuilder.append(input.getSymbol(i));
            outBuilder.append(output.getSymbol(i));

            if (isCallSymbol(sym)) {
                final int returnIdx = findReturnIndex(input, i + 1);
                if (returnIdx > -1) { // found matching return
                    i = returnIdx;
                }
            }
        }

        return Pair.of(inBuilder.toWord(), outBuilder.toWord());
    }

}
