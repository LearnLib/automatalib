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
package net.automatalib.automaton.mmlt;

import java.util.Collections;
import java.util.List;

import net.automatalib.automaton.MutableDeterministic;

/**
 * A mutable extension of {@link MMLT} that allows for modifying transition structure and timers.
 *
 * @param <S>
 *         location type
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 */
public interface MutableMMLT<S, I, T, O> extends MMLT<S, I, T, O>, MutableDeterministic<S, I, T, Void, O> {

    /**
     * Adds a new periodic timer to the provided location. Throws an error if
     * <ul>
     *     <li>the outputs are empty, contain silence, or combined output symbols,</li>
     *     <li>the initial value is less zero or less,</li>
     *     <li>the initial value exceeds that of a one-shot timer (-> timer never expires),</li>
     *     <li>the timer will time out at the same time as a one-shot timer.</li>
     * </ul>
     *
     * @param location
     *         the location of the timer
     * @param name
     *         the timer name
     * @param initial
     *         the initial value
     * @param outputs
     *         the outputs at timeout
     */
    void addPeriodicTimer(S location, String name, long initial, List<O> outputs);

    /**
     * Convenience method for {@link #addPeriodicTimer(Object, String, long, List)} that wraps {@code output} in a
     * {@link Collections#singletonList(Object)}.
     *
     * @param location
     *         the location of the timer
     * @param name
     *         the timer name
     * @param initial
     *         the initial value
     * @param output
     *         the output at timeout
     */
    default void addPeriodicTimer(S location, String name, long initial, O output) {
        addPeriodicTimer(location, name, initial, Collections.singletonList(output));
    }

    /**
     * Adds a new one-shot timer to the provided location. Removes all timers of that location with higher initial
     * value, as these can no longer time out. Throws an error if
     * <ul>
     *     <li>the outputs are empty or contain silence, or combined output symbols,</li>
     *     <li>the initial value is less zero or less,</li>
     *     <li>the initial value exceeds that of a one-shot timer (-> timer never expires),</li>
     *     <li>the timer will time out at the same time as a periodic timer.</li>
     * </ul>
     *
     * @param location
     *         the location of the timer
     * @param name
     *         the timer name
     * @param initial
     *         the initial value
     * @param outputs
     *         the outputs at timeout
     * @param target
     *         the target location when timing out
     */
    void addOneShotTimer(S location, String name, long initial, List<O> outputs, S target);

    /**
     * Convenience method for {@link #addOneShotTimer(Object, String, long, List, Object)} that wraps {@code output} in
     * a {@link Collections#singletonList(Object)}.
     *
     * @param location
     *         the location of the timer
     * @param name
     *         the timer name
     * @param initial
     *         the initial value
     * @param output
     *         the output at timeout
     * @param target
     *         the target location when timing out
     */
    default void addOneShotTimer(S location, String name, long initial, O output, S target) {
        addOneShotTimer(location, name, initial, Collections.singletonList(output), target);
    }

    /**
     * Removes the timer with the provided name. No effect if the location has no such timer.
     *
     * @param location
     *         the location of the timer
     * @param timerName
     *         the name of the timer
     */
    void removeTimer(S location, String timerName);

    /**
     * Adds a local reset at the provided input in the provided location. Throws an error if the transition does not
     * self-loop.
     *
     * @param location
     *         the source location
     * @param input
     *         the input of the transition that should perform a local reset
     */
    void addLocalReset(S location, I input);

    /**
     * Removes a local reset at the provided input in the provided location. No effect if the input does not trigger a
     * local reset.
     *
     * @param location
     *         the source location
     * @param input
     *         the input of the transition that performs a local reset.
     */
    void removeLocalReset(S location, I input);
}
