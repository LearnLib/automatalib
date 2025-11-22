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
     *     <li>the output is silent</li>
     *     <li>the initial value is less zero or less</li>
     *     <li>the initial value exceeds that of a one-shot timer (-> timer never expires)</li>
     *     <li>the timer will time out at the same time as a one-shot timer</li>
     * </ul>
     *
     * @param location
     *         location of the timer
     * @param name
     *         timer name
     * @param initial
     *         initial value
     * @param output
     *         output at timeout
     */
    void addPeriodicTimer(S location, String name, long initial, O output);

    /**
     * Adds a new one-shot timer to the provided location. Removes all timers of that location with higher initial
     * value, as these can no longer time out. Throws an error if
     * <ul>
     *     <li>the output is silent</li>
     *     <li>the initial value is less zero or less</li>
     *     <li>the initial value exceeds that of a one-shot timer (-> timer never expires)</li>
     *     <li>the timer will time out at the same time as a periodic timer</li>
     * </ul>
     *
     * @param location
     *         location of the timer
     * @param name
     *         timer name
     * @param initial
     *         initial value
     * @param output
     *         output at timeout
     * @param target
     *         target location when timing out
     */
    void addOneShotTimer(S location, String name, long initial, O output, S target);

    /**
     * Removes the timer with the provided name. No effect if the location has no such timer.
     *
     * @param location
     *         location of the timer
     * @param timerName
     *         name of the timer
     */
    void removeTimer(S location, String timerName);

    /**
     * Adds a local reset at the provided input in the provided location. Throws an error if the transition does not
     * self-loop.
     *
     * @param location
     *         source location
     * @param input
     *         input of the transition that should perform a local reset
     */
    void addLocalReset(S location, I input);

    /**
     * Removes a local reset at the provided input in the provided location. No effect if the input does not trigger a
     * local reset.
     *
     * @param location
     *         source location
     * @param input
     *         input of the transition that performs a local reset.
     */
    void removeLocalReset(S location, I input);
}
