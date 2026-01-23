/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;

/**
 * Provides a reduced version of the semantics automaton of an MMLT. This reduced version retains all configurations
 * that can be reached by timeouts and non-delaying inputs. It omits configurations that can only be reached by at least
 * two subsequent time steps.
 * <p>
 * The resulting automaton suffices to check the equivalence of two MMLTs. However, as the timeStep-transition is
 * undefined in some configurations, the automaton cannot execute any sequence of inputs that can be executed on an
 * MMLT.
 *
 * @param <S>
 *         location type of the original MMLT
 * @param <I>
 *         input symbol of the original MMLT
 * @param <O>
 *         output symbol type of the original MMLT
 */
public final class ReducedMMLTSemantics<S, I, O> extends CompactMealy<TimedInput<I>, TimedOutput<O>> {

    private final Map<State<S, O>, Integer> stateMap;

    private ReducedMMLTSemantics(Alphabet<TimedInput<I>> alphabet) {
        super(alphabet);
        this.stateMap = new HashMap<>();
    }

    /**
     * Constructs a reduced semantics view for the given MMLT.
     *
     * @param mmlt
     *         the MMLT
     * @param <S>
     *         location type of the original MMLT
     * @param <I>
     *         input symbol of the original MMLT
     * @param <O>
     *         output symbol type of the original MMLT
     *
     * @return the reduced semantics view
     */
    public static <S, I, O> ReducedMMLTSemantics<S, I, O> forMMLT(MMLT<S, I, ?, O> mmlt) {
        return forMMLT(mmlt, mmlt.getSemantics());
    }

    private static <S, I, T, O> ReducedMMLTSemantics<S, I, O> forMMLT(MMLT<S, I, ?, O> automaton,
                                                                      MMLTSemantics<S, I, T, O> semantics) {
        // Create alphabet for expanded form:
        Alphabet<TimedInput<I>> alphabet = semantics.getInputAlphabet();

        ReducedMMLTSemantics<S, I, O> mealy = new ReducedMMLTSemantics<>(alphabet);

        // 1a: Add all configurations that can be reached via timeouts/non-delaying inputs, or are at least one time
        // step away from these configurations:
        for (S loc : automaton) {
            getRelevantConfigurations(loc, automaton, semantics).forEach(c -> mealy.stateMap.put(c, mealy.addState()));
        }

        // 1b: Mark initial state:
        State<S, O> initialConfig = semantics.getInitialState();
        mealy.setInitialState(mealy.stateMap.get(initialConfig));

        // 2. Add transitions:
        for (State<S, O> config : mealy.stateMap.keySet()) {
            Integer sourceState = mealy.stateMap.get(config);

            for (TimedInput<I> sym : alphabet) {
                T trans = semantics.getTransition(config, sym);
                if (trans != null) {
                    TimedOutput<O> output = semantics.getTransitionOutput(trans);

                    // Try to find matching state. If not found, leave undefined:
                    int targetId = mealy.stateMap.getOrDefault(semantics.getSuccessor(trans), -1);
                    if (targetId != -1) {
                        mealy.addTransition(sourceState, sym, targetId, output);
                    }
                }
            }
        }

        return mealy;
    }

    /**
     * Retrieves a list of configurations of the provided location that can be reached via timeouts and those that are
     * at most one time step away from these.
     *
     * @param <S>
     *         location type of the original MMLT
     * @param <I>
     *         input symbol of the original MMLT
     * @param <T>
     *         transition type of the MMLTSemantics
     * @param <O>
     *         output symbol type of the original MMLT
     * @param location
     *         the considered location
     * @param mmlt
     *         the MMLT
     * @param semantics
     *         the semantics automaton of {@code mmlt}
     *
     * @return the list of the relevant configurations of the location
     */
    private static <S, I, T, O> List<State<S, O>> getRelevantConfigurations(S location,
                                                                            MMLT<S, I, ?, O> mmlt,
                                                                            MMLTSemantics<S, I, T, O> semantics) {

        List<State<S, O>> configurations = new ArrayList<>();

        State<S, O> currentConfiguration = new State<>(location, mmlt.getSortedTimers(location));
        configurations.add(currentConfiguration);

        // Enumerate all timeouts, until we change to a different location or re-enter the entry configuration
        // of this location:
        while (true) {
            // Wait for next timeout:
            T trans = semantics.getTransition(currentConfiguration, new TimeoutSymbol<>());
            if (trans != null) {
                TimedOutput<O> output = semantics.getTransitionOutput(trans);
                State<S, O> target = semantics.getSuccessor(trans);
                if (output.equals(semantics.getSilentOutput())) {
                    break; // no timeout
                }

                if (output.delay() > 1) {
                    // More than one time unit away -> add 1-step successor config.
                    // If one time unit away, the successor is already in our list.
                    State<S, O> newGapConfig = currentConfiguration.decrement(1);
                    configurations.add(newGapConfig);
                }

                if (target.isEntryConfig()) {
                    break; // location change OR repeating behavior
                }
                configurations.add(target);
                currentConfiguration = target;
            } else {
                return configurations;
            }
        }
        return configurations;
    }

    /**
     * Returns the state that represents the provided configuration. If the configuration is not included and
     * allowApproximate is set, the closest configuration of the same location (with a smaller entry distance) will be
     * returned. If {@code allowApproximate} is not set, an error is thrown.
     *
     * @param configuration
     *         the provided configuration
     * @param allowApproximate
     *         a flag to indicate whether the closest matching state is returned if the configuration is not part of the
     *         reduced automaton
     *
     * @return the corresponding state in the reduced automaton
     */
    public Integer getStateForConfiguration(State<S, O> configuration, boolean allowApproximate) {

        Entry<State<S, O>, Integer> closestMatch = null;

        for (Entry<State<S, O>, Integer> e : stateMap.entrySet()) {
            State<S, O> cfg = e.getKey();
            if (!Objects.equals(cfg.getLocation(), configuration.getLocation()) ||
                cfg.getEntryDistance() > configuration.getEntryDistance()) {
                continue;
            }

            if (cfg.getEntryDistance() == configuration.getEntryDistance()) {
                // Perfect match:
                return e.getValue();
            }

            if (closestMatch == null || cfg.getEntryDistance() > closestMatch.getKey().getEntryDistance()) {
                // Closer than previous candidate:
                closestMatch = e;
            }
        }

        if (closestMatch == null || !allowApproximate) {
            throw new IllegalStateException("Could not find corresponding configuration in expanded form");
        }

        return closestMatch.getValue();
    }

    /**
     * Returns the configuration that represents the provided state. Throws an error if the state is not part of the
     * reduced automaton.
     *
     * @param state
     *         considered state
     *
     * @return corresponding configuration
     */
    public State<S, O> getConfigurationForState(int state) {
        for (Entry<State<S, O>, Integer> e : this.stateMap.entrySet()) {
            if (e.getValue() == state) {
                return e.getKey();
            }
        }
        throw new IllegalStateException("Could not find corresponding configuration in expanded form");
    }
}
