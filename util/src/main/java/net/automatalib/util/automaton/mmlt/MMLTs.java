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
package net.automatalib.util.automaton.mmlt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.automaton.mmlt.impl.ReducedMMLTSemantics;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides various functions that are related MMLTs.
 */
public final class MMLTs {

    private MMLTs() {
        // prevent instantiation
    }

    public static <I, O> boolean testEquivalence(MMLT<?, I, ?, O> modelA,
                                                 MMLT<?, I, ?, O> modelB,
                                                 Collection<? extends TimedInput<I>> inputs) {
        return findSeparatingWord(modelA, modelB, inputs) == null;
    }

    public static <I, O> @Nullable Word<TimedInput<I>> findSeparatingWord(MMLT<?, I, ?, O> modelA,
                                                                          MMLT<?, I, ?, O> modelB,
                                                                          Collection<? extends TimedInput<I>> inputs) {
        ReducedMMLTSemantics<?, I, O> expandedA = ReducedMMLTSemantics.forMMLT(modelA);
        ReducedMMLTSemantics<?, I, O> expandedB = ReducedMMLTSemantics.forMMLT(modelB);

        Word<TimedInput<I>> separatingWord = Automata.findSeparatingWord(expandedA, expandedB, inputs);

        assert separatingWord == null || !modelA.getSemantics()
                                                .computeOutput(separatingWord)
                                                .equals(modelB.getSemantics().computeOutput(separatingWord)) :
                "Invalid separating word";

        return separatingWord;
    }

    /**
     * Returns the number of configurations for the given location. This is defined as follows:
     * <ul>
     *     <li>When l has no timers: one</li>
     *     <li>When l has a one-shot timer: initial value of this timer</li>
     *     <li>When l only has periodic timers: least-common multiple of their initial values</li>
     * </ul>
     *
     * @param mmlt
     *         the MMLT
     * @param location
     *         considered location
     * @param <S>
     *         location type
     *
     * @return maximum configuration time. {@link Long#MAX_VALUE}, if exceeding long maximum.
     */
    public static <S> long getConfigurationCount(MMLT<S, ?, ?, ?> mmlt, S location) {
        final List<? extends TimerInfo<S, ?>> timers = mmlt.getSortedTimers(location);

        if (timers.isEmpty()) {
            return 1;
        }

        // Single timer: take that timer's value:
        if (timers.size() == 1) {
            return timers.get(0).initial();
        }

        for (TimerInfo<S, ?> t : timers) {
            if (!t.periodic()) {
                return t.initial(); // leave location at latest when one-shot expires
            }
        }

        // The lcm of multiple numbers is equal to the product of their multiple with their gcd.
        // Therefore: calculate gcd and multiple, then divide multiple by gcd.
        final List<BigInteger> bigTimeouts = new ArrayList<>(timers.size());
        for (TimerInfo<S, ?> t : timers) {
            bigTimeouts.add(BigInteger.valueOf(t.initial()));
        }

        // Calculate gcd of all timeouts:
        BigInteger gcd = bigTimeouts.get(0);
        BigInteger multiple = bigTimeouts.get(0);
        for (int i = 1; i < bigTimeouts.size(); i++) {
            gcd = gcd.gcd(bigTimeouts.get(i));
            multiple = multiple.multiply(bigTimeouts.get(i));
        }
        final BigInteger lcm = multiple.divide(gcd);

        try {
            return lcm.longValueExact();
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Returns the maximum initial value of all timers.
     *
     * @param mmlt
     *         the MMLT
     * @param <S>
     *         location type
     *
     * @return maximum initial timer value. {@code 0} if no timers are used
     */
    public static <S> long getMaximumInitialTimerValue(MMLT<S, ?, ?, ?> mmlt) {
        long maxValue = 0;
        for (S loc : mmlt) {
            for (TimerInfo<S, ?> t : mmlt.getSortedTimers(loc)) {
                maxValue = Math.max(maxValue, t.initial());
            }
        }
        return maxValue;
    }

    /**
     * Retrieves the maximum delay to the next timeout in this model.
     *
     * @param mmlt
     *         the MMLT
     * @param <S>
     *         location type
     *
     * @return maximum timeout delay (is at least one)
     */
    public static <S> long getMaximumTimeoutDelay(MMLT<S, ?, ?, ?> mmlt) {
        long maxValue = 1;

        for (S loc : mmlt) {
            List<? extends TimerInfo<S, ?>> timers = mmlt.getSortedTimers(loc);
            if (timers.isEmpty()) {
                continue;
            }

            if (timers.size() == 1) {
                maxValue = Math.max(maxValue, timers.get(0).initial());
                continue;
            }

            // Get the least common multiple of the initial times:
            long lcm = MMLTs.getConfigurationCount(mmlt, loc);

            // Calculate expiration times of all timers:
            List<Long> timeouts = new ArrayList<>();
            for (TimerInfo<S, ?> timer : mmlt.getSortedTimers(loc)) {
                long currentValue = timer.initial();
                while (currentValue < lcm) {
                    timeouts.add(currentValue);
                    currentValue += timer.initial();
                }
            }

            // Find the largest distance between two successive expirations:
            timeouts.add(0L); // need to consider time to first expiration
            Collections.sort(timeouts);
            long maxDiff = 0;
            for (int i = 0; i < timeouts.size() - 1; i++) {
                long diff = timeouts.get(i + 1) - timeouts.get(i);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            maxValue = Math.max(maxValue, maxDiff);
        }

        return maxValue;
    }

}
