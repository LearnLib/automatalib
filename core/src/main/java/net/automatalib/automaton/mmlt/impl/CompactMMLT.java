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
package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.MutableMMLT;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.symbol.time.SymbolicInput;

/**
 * Implements a {@link MutableMMLT} by storing adjacency information in compact arrays.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class CompactMMLT<I, O> extends CompactMealy<I, O> implements MutableMMLT<Integer, I, CompactTransition<O>, O> {

    private final Map<Integer, List<TimerInfo<Integer, O>>> sortedTimers; // location -> (sorted timers)
    private final Map<Integer, Set<I>> resets; // location -> inputs (that reset all timers)

    private final O silentOutput;
    private final SymbolCombiner<O> outputCombiner;

    /**
     * Initializes a new CompactMMLT.
     *
     * @param alphabet
     *         alphabet of non-delaying inputs
     * @param silentOutput
     *         the silent output used by this MMLT
     * @param outputCombiner
     *         the combiner function for simultaneous timeouts of periodic timers
     */
    public CompactMMLT(Alphabet<I> alphabet, O silentOutput, SymbolCombiner<O> outputCombiner) {
        this(alphabet, DEFAULT_INIT_CAPACITY, silentOutput, outputCombiner);
    }

    /**
     * Initializes a new CompactMMLT.
     *
     * @param alphabet
     *         alphabet of non-delaying inputs
     * @param sizeHint
     *         size hint to better allocate internal memory
     * @param silentOutput
     *         the silent output used by this MMLT
     * @param outputCombiner
     *         the combiner function for simultaneous timeouts of periodic timers
     */
    public CompactMMLT(Alphabet<I> alphabet, int sizeHint, O silentOutput, SymbolCombiner<O> outputCombiner) {
        super(alphabet, sizeHint);

        this.sortedTimers = new HashMap<>();
        this.resets = new HashMap<>();

        this.silentOutput = silentOutput;
        this.outputCombiner = outputCombiner;
    }

    @Override
    public O getSilentOutput() {
        return this.silentOutput;
    }

    @Override
    public SymbolCombiner<O> getOutputCombiner() {
        return this.outputCombiner;
    }

    @Override
    public boolean isLocalReset(Integer location, I input) {
        return this.resets.getOrDefault(location, Collections.emptySet()).contains(input);
    }

    @Override
    public List<TimerInfo<Integer, O>> getSortedTimers(Integer location) {
        return Collections.unmodifiableList(this.sortedTimers.getOrDefault(location, Collections.emptyList()));
    }

    @Override
    public MMLTSemantics<Integer, I, ?, O> getSemantics() {
        return new DefaultMMLTSemantics<>(this);
    }

    private void ensureThatCanAddTimer(List<TimerInfo<Integer, O>> timers,
                                       String name,
                                       long initial,
                                       List<O> outputs,
                                       boolean periodic) {
        if (outputs.isEmpty() || outputs.contains(silentOutput)) {
            throw new IllegalArgumentException(String.format("Timer '%s': outputs are empty or contain silent output.",
                                                             name));
        }

        for (O output : outputs) {
            if (getOutputCombiner().isCombinedSymbol(output)) {
                throw new IllegalArgumentException(String.format(
                        "Timer '%s': output '%s' is a combined symbol. You must only provide atomic outputs.",
                        name,
                        output));
            }
        }

        // Verify that the timer name is unique:
        for (TimerInfo<Integer, O> integerOTimerInfo : timers) {
            if (integerOTimerInfo.name().equals(name)) {
                throw new IllegalArgumentException(String.format("Location already has a timer of the name '%s'.",
                                                                 name));
            }
        }

        // Ensure that our new timer can time out AND that its timeouts do not coincide with that of an existing one-shot timer:
        for (TimerInfo<Integer, O> t : timers) {
            if (!t.periodic()) {
                if (initial > t.initial()) {
                    throw new IllegalArgumentException(String.format(
                            "The initial value %d of '%s' exceeds that of a one-shot timer; will never time out.",
                            initial,
                            name));
                }
                if (periodic && t.initial() % initial == 0) {
                    // Our new periodic timer will time out at the same time as the existing one-shot timer.
                    // This makes the model non-deterministic and is not allowed:
                    throw new IllegalArgumentException(String.format(
                            "The timer '%s' times out at the same time as a one-shot timer (%d).",
                            name,
                            initial));
                }
            }
        }
        if (!periodic) {
            // Our new one-shot timer is the one-shot timer with the highest initial value (or the only one).
            // Check that no timer with a lower initial value will time out at the same time:
            for (TimerInfo<Integer, O> timer : timers) {
                if (timer.initial() <= initial && initial % timer.initial() == 0) {
                    throw new IllegalArgumentException(String.format(
                            "The existing timer '%s' times out at the same time as the new one-shot timer (%d).",
                            timer.name(),
                            initial));
                }
            }
        }
    }

    @Override
    public void addPeriodicTimer(Integer location, String name, long initial, List<O> outputs) {
        final List<TimerInfo<Integer, O>> localTimers =
                this.sortedTimers.computeIfAbsent(location, k -> new ArrayList<>());

        ensureThatCanAddTimer(localTimers, name, initial, outputs, true);
        localTimers.add(new TimerInfo<>(name, initial, outputs, location, true));
        localTimers.sort(Comparator.comparingLong(TimerInfo::initial));
    }

    @Override
    public void addOneShotTimer(Integer location, String name, long initial, List<O> outputs, Integer target) {
        final List<TimerInfo<Integer, O>> localTimers =
                this.sortedTimers.computeIfAbsent(location, k -> new ArrayList<>());

        ensureThatCanAddTimer(localTimers, name, initial, outputs, false);
        localTimers.add(new TimerInfo<>(name, initial, outputs, target, false));
        localTimers.sort(Comparator.comparingLong(TimerInfo::initial));

        // Remove all timers with higher initial value, as these can no longer time out:
        localTimers.removeIf(t -> t.initial() > initial);
    }

    @Override
    public void removeTimer(Integer location, String timerName) {
        final List<TimerInfo<Integer, O>> localTimers = this.sortedTimers.get(location);
        if (localTimers != null) {
            localTimers.removeIf(t -> t.name().equals(timerName));
        }
    }

    @Override
    public void addLocalReset(Integer location, I input) {
        // Ensure that input causes self-loop:
        final Integer target = this.getSuccessor(location, input);
        if (target == null || !target.equals(location)) {
            throw new IllegalArgumentException("Provided input is not defined or does not trigger a self-loop.");
        }

        resets.computeIfAbsent(location, k -> new HashSet<>()).add(input);
    }

    @Override
    public void removeLocalReset(Integer location, I input) {
        final Set<I> localResets = resets.get(location);
        if (localResets != null) {
            localResets.remove(input);
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.sortedTimers.clear();
        this.resets.clear();
    }

    @Override
    public Graph<Integer, Triple<SymbolicInput<I>, O, Integer>> graphView() {
        return MutableMMLT.super.graphView();
    }

    public static class Creator<I, O> implements AutomatonCreator<CompactMMLT<I, O>, I> {

        private final O silentOutput;
        private final SymbolCombiner<O> outputCombiner;

        public Creator(O silentOutput, SymbolCombiner<O> outputCombiner) {
            this.silentOutput = silentOutput;
            this.outputCombiner = outputCombiner;
        }

        @Override
        public CompactMMLT<I, O> createAutomaton(Alphabet<I> alphabet, int numStatesHint) {
            return new CompactMMLT<>(alphabet, numStatesHint, silentOutput, outputCombiner);
        }

        @Override
        public CompactMMLT<I, O> createAutomaton(Alphabet<I> alphabet) {
            return new CompactMMLT<>(alphabet, silentOutput, outputCombiner);
        }
    }
}
