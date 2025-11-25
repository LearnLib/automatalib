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
package net.automatalib.util.automaton.cover;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MMLTCover {

    private MMLTCover() {
        // prevent instantiation
    }

    /**
     * Calculates a location cover for an MMLT. Convenience method for
     * {@link #getMMLTLocationCover(MMLT, Collection, boolean)} with {@code allowIncomplete} set to {@code true}.
     *
     * @param mmlt
     *         the MMLT
     * @param inputs
     *         the inputs to use when calculating the cover
     * @param <S>
     *         location type
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     * @param <O>
     *         output symbol type
     *
     * @return a location cover as mapping from location to prefix
     */
    public static <S, I, O> Map<S, Word<TimedInput<I>>> getMMLTLocationCover(MMLT<S, I, ?, O> mmlt,
                                                                             Collection<TimedInput<I>> inputs) {
        return getMMLTLocationCover(mmlt, inputs, true);
    }

    /**
     * Calculates a location cover for an MMLT.
     * <p>
     * The cover provides one prefix for each location of the MMLT. The returned prefixes use only the provided inputs.
     * Time steps are not needed to calculate a full cover. If set, they are ignored when calculating the cover, even if
     * they are part of the provided inputs.
     * <p>
     * If some locations are isolated, they are excluded from the cover. If some locations cannot be reached with the
     * provided inputs, they are also excluded.
     *
     * @param mmlt
     *         the MMLT
     * @param inputs
     *         the inputs to use when calculating the cover
     * @param allowIncomplete
     *         a flag indicating whether an error should be thrown if some locations are unreachable
     * @param <S>
     *         location type
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     * @param <O>
     *         output symbol type
     *
     * @return a location cover as mapping from location to prefix
     */
    public static <S, I, O> Map<S, Word<TimedInput<I>>> getMMLTLocationCover(MMLT<S, I, ?, O> mmlt,
                                                                             Collection<TimedInput<I>> inputs,
                                                                             boolean allowIncomplete) {
        return getMMLTLocationCover(mmlt, mmlt.getSemantics(), inputs, allowIncomplete);
    }

    private static <S, I, T, O> Map<S, Word<TimedInput<I>>> getMMLTLocationCover(MMLT<S, I, ?, O> automaton,
                                                                                 MMLTSemantics<S, I, T, O> semantics,
                                                                                 Collection<TimedInput<I>> inputs,
                                                                                 boolean allowIncomplete) {

        Map<S, Word<TimedInput<I>>> locPrefixes = new LinkedHashMap<>();
        Map<State<S, O>, Word<TimedInput<I>>> cfgPrefixes = new LinkedHashMap<>();
        Queue<State<S, O>> queue = new ArrayDeque<>();

        State<S, O> init = semantics.getInitialState();

        if (init != null) {
            queue.add(init);
            cfgPrefixes.put(init, Word.epsilon());
            locPrefixes.put(automaton.getInitialState(), Word.epsilon());
        }

        while (!queue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull State<S, O> current = queue.poll();
            for (TimedInput<I> symbol : inputs) {
                if (symbol instanceof TimeStepSequence<I>) {
                    continue;
                }

                T trans = semantics.getTransition(current, symbol);
                if (trans != null) {
                    State<S, O> succ = semantics.getSuccessor(trans);
                    if (succ.equals(current)) {
                        continue; // self-loop
                    }
                    if (!cfgPrefixes.containsKey(succ)) {
                        Word<TimedInput<I>> prefix = cfgPrefixes.get(current);
                        assert prefix != null;
                        Word<TimedInput<I>> newPrefix = prefix.append(symbol);
                        cfgPrefixes.put(succ, newPrefix);
                        queue.add(succ);

                        if (succ.isEntryConfig()) {
                            locPrefixes.put(succ.getLocation(), newPrefix);
                        }
                    }
                }
            }
        }

        if (!allowIncomplete && locPrefixes.size() != automaton.getStates().size()) {
            throw new IllegalStateException("Incomplete state cover");
        }

        return locPrefixes;
    }
}
