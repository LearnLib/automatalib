package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.mmlt.MMLTGraphView;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.MutableMMLT;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;

/**
 * Implements a LocalTimerMealy that is mutable. The structure automaton is backed by a CompactMealy automaton.
 *
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output symbol type
 */
public class CompactMMLT<I, O> extends CompactMealy<I, O> implements MutableMMLT<Integer, I, CompactTransition<O>, O> {

    private final Map<Integer, List<MealyTimerInfo<Integer, O>>> sortedTimers; // location -> (sorted timers)
    private final Map<Integer, Set<I>> resets; // location -> inputs (that reset all timers)

    private final O silentOutput;
    private final SymbolCombiner<O> outputCombiner;

    /**
     * Initializes a new CompactLocalTimerMealy.
     *
     * @param nonDelayingInputs
     *         Non-delaying inputs used by this MMLT.
     * @param silentOutput
     *         The silent output used by this MMLT.
     * @param outputCombiner
     *         The combiner function for simultaneous timeouts of periodic timers.
     */
    public CompactMMLT(Alphabet<I> nonDelayingInputs, O silentOutput, SymbolCombiner<O> outputCombiner) {
        this(nonDelayingInputs, DEFAULT_INIT_CAPACITY, silentOutput, outputCombiner);
    }

    public CompactMMLT(Alphabet<I> nonDelayingInputs, int sizeHint, O silentOutput, SymbolCombiner<O> outputCombiner) {
        super(nonDelayingInputs, sizeHint);

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
    public List<MealyTimerInfo<Integer, O>> getSortedTimers(Integer location) {
        return Collections.unmodifiableList(this.sortedTimers.getOrDefault(location, Collections.emptyList()));
    }

    @Override
    public MMLTSemantics<Integer, I, ?, O> getSemantics() {
        return new CompactMMLTSemantics<>(this);
    }

    private void ensureThatCanAddTimer(List<MealyTimerInfo<Integer, O>> timers,
                                       String name,
                                       long initial,
                                       O output,
                                       boolean periodic) {
        if (output.equals(this.silentOutput)) {
            throw new IllegalArgumentException(String.format("Provided silent output for timer '%s'.", name));
        }

        // Verify that the timer name is unique:
        if (timers.stream().anyMatch(t -> t.name().equals(name))) {
            throw new IllegalArgumentException(String.format("Location already has a timer of the name '%s'.", name));
        }

        // Ensure that our new timer can time out AND that its timeouts do not coincide with that of an existing one-shot timer:
        var oldOneShot = timers.stream().filter(t -> !t.periodic()).findFirst();
        if (oldOneShot.isPresent()) {
            if (initial > oldOneShot.get().initial()) {
                throw new IllegalArgumentException(String.format(
                        "The initial value %d of '%s' exceeds that of a one-shot timer; will never time out.",
                        initial,
                        name));
            }
            if (periodic && (oldOneShot.get().initial() % initial == 0)) {
                // Our new periodic timer will time out at the same time as the existing one-shot timer.
                // This makes the model non-deterministic and is not allowed:
                throw new IllegalArgumentException(String.format(
                        "The timer '%s' times out at the same time as a one-shot timer (%d).",
                        name,
                        initial));
            }
        }
        if (!periodic) {
            // Our new one-shot timer is the one-shot timer with the highest initial value (or the only one).
            // Check that no timer with a lower initial value will time out at the same time:
            for (var timer : timers) {
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
    public void addPeriodicTimer(Integer location, String name, long initial, O output) {
        this.sortedTimers.putIfAbsent(location, new ArrayList<>());
        var localTimers = this.sortedTimers.get(location);

        ensureThatCanAddTimer(localTimers, name, initial, output, true);
        localTimers.add(new MealyTimerInfo<>(name, initial, output, true, location));
        localTimers.sort(Comparator.comparingLong(MealyTimerInfo::initial));

        // Add self-looping transition:
//        TimerTimeoutSymbol<I> newTimerSymbol = new TimerTimeoutSymbol<>(name);
//        this.automaton.addAlphabetSymbol(newTimerSymbol);
//        automaton.addTransition(location, newTimerSymbol, location, output);
    }

    @Override
    public void addOneShotTimer(Integer location, String name, long initial, O output, Integer target) {
        this.sortedTimers.putIfAbsent(location, new ArrayList<>());
        var localTimers = this.sortedTimers.get(location);

        ensureThatCanAddTimer(localTimers, name, initial, output, false);
        localTimers.add(new MealyTimerInfo<>(name, initial, output, false, target));
        localTimers.sort(Comparator.comparingLong(MealyTimerInfo::initial));

        // Add transition with location change:
//        TimerTimeoutSymbol<I> newTimerSymbol = new TimerTimeoutSymbol<>(name);
//        this.automaton.addAlphabetSymbol(newTimerSymbol);
//        automaton.addTransition(location, newTimerSymbol, target, output);

        // Remove all timers with higher initial value, as these can no longer time out:
        localTimers.removeIf(t -> t.initial() > initial);
    }

    @Override
    public void removeTimer(Integer location, String timerName) {
        var localTimers = this.sortedTimers.get(location);
        if (localTimers == null) {
            return;
        }

        localTimers.removeIf(t -> t.name().equals(timerName));
//        automaton.removeAllTransitions(location, new TimerTimeoutSymbol<>(timerName));
    }

    @Override
    public void addLocalReset(Integer location, I input) {
        // Ensure that input causes self-loop:
        var target = this.getSuccessor(location, input);
        if (target == null || !target.equals(location)) {
            throw new IllegalArgumentException("Provided input is not defined or does not trigger a self-loop.");
        }

        resets.putIfAbsent(location, new HashSet<>());
        resets.get(location).add(input);
    }

    @Override
    public void removeLocalReset(Integer location, I input) {
        var localResets = resets.get(location);
        if (localResets == null) {
            return;
        }

        localResets.remove(input);
    }

    @Override
    public void clear() {
        super.clear();
        this.sortedTimers.clear();
        this.resets.clear();
    }

    @Override
    public Graph<Integer, Triple<SymbolicInput<I>, O, Integer>> graphView() {
        return new MMLTGraphView<>(this);
    }
}
