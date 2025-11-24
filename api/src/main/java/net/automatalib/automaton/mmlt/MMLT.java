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

import java.util.List;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.symbol.time.SymbolicInput;

/**
 * Base type for a Mealy Machine with Local Timers (MMLT).
 * <p>
 * An MMLT extends Mealy machines with local timers. Each location can have multiple timers. A timer can only be active
 * in its assigned location. All timers of a location reset when this location is entered from a different location or,
 * in case of the initial location, if the location is entered for the first time. There are periodic and one-shot
 * timers. Periodic timers reset themselves on timeout. They cannot cause a location change. One-shot timers can cause a
 * location change. They reset all timers of the target location at timeout. A location can have arbitrarily many
 * periodic timers and up to one one-shot timer. Timers are always reset to their initial value. The initial values must
 * be chosen so that a periodic timer never times out at the same time as a one-shot timer (to preserve determinism).
 * Multiple periodic timers may time out simultaneously. In this case, their outputs are combined using an
 * {@link SymbolCombiner}.
 * <p>
 * <b>Implementation note:</b> This class resembles a "structural" view on the MMLT. Timeouts can also be interpreted
 * as explicit transitions between locations. For this representation, use the {@link #graphView()} method. For a
 * semantic view that supports time-sensitive transductions, see the {@link #getSemantics()} method.
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
public interface MMLT<S, I, T, O>
        extends UniversalDeterministicAutomaton<S, I, T, Void, O>, InputAlphabetHolder<I>, GraphViewable {

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return the silent output symbol
     */
    O getSilentOutput();

    /**
     * Returns the output combiner used when multiple periodic timers time out simultaneously.
     *
     * @return the output combiner
     */
    SymbolCombiner<O> getOutputCombiner();

    /**
     * Indicates if the provided input performs a local reset in the given location.
     *
     * @param location
     *         the location
     * @param input
     *         the input
     *
     * @return {@code true} if performing a local reset, {@code false} otherwise
     */
    boolean isLocalReset(S location, I input);

    /**
     * Returns the timers of the specified location sorted ascendingly by their initial time.
     *
     * @param location
     *         location
     *
     * @return sorted list of local timers (may be empty if the location has no timers)
     */
    List<TimerInfo<S, O>> getSortedTimers(S location);

    /**
     * Returns the semantics automaton that describes the behavior of this MMLT.
     *
     * @return a semantic view of this MMLT
     */
    MMLTSemantics<S, I, ?, O> getSemantics();

    @Override
    default Graph<S, Triple<SymbolicInput<I>, O, S>> graphView() {
        return new MMLTGraphView<>(this, getOutputCombiner());
    }
}
