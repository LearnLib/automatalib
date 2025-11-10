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
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.MutableMMLT;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.TimerTimeoutSymbol;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implements a LocalTimerMealy that is mutable. The structure automaton is backed by a CompactMealy automaton.
 *
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output symbol type
 */
public class CompactMMLT<I, O> implements MutableMMLT<Integer, I, CompactTransition<O>, O> {

    private final CompactMealy<SymbolicInput<I>, O> automaton;
    private final Map<Integer, List<MealyTimerInfo<O>>> sortedTimers; // location -> (sorted timers)
    private final Map<Integer, Set<SymbolicInput<I>>> resets; // location -> inputs (that reset all timers)

    private final Alphabet<InputSymbol<I>> untimedAlphabet;

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
    public CompactMMLT(Collection<InputSymbol<I>> nonDelayingInputs, O silentOutput, SymbolCombiner<O> outputCombiner) {
        this.untimedAlphabet = Alphabets.fromCollection(nonDelayingInputs);

        this.sortedTimers = new HashMap<>();
        this.resets = new HashMap<>();

        this.silentOutput = silentOutput;
        this.outputCombiner = outputCombiner;

        // Prepare compact Mealy:
        this.automaton = new CompactMealy<>(new GrowingMapAlphabet<>(nonDelayingInputs));
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
    public Alphabet<SymbolicInput<I>> getInputAlphabet() {
        return this.automaton.getInputAlphabet();
    }

    @Override
    public Alphabet<InputSymbol<I>> getUntimedAlphabet() {
        return this.untimedAlphabet;
    }

    @Override
    public boolean isLocalReset(Integer location, InputSymbol<I> input) {
        return this.resets.getOrDefault(location, Collections.emptySet()).contains(input);
    }

    @Override
    public List<MealyTimerInfo<O>> getSortedTimers(Integer location) {
        return Collections.unmodifiableList(this.sortedTimers.getOrDefault(location, Collections.emptyList()));
    }

    @Override
    public MMLTSemantics<Integer, I, ?, O> getSemantics() {
        return new CompactMMLTSemantics<>(this);
    }

    @Override
    public Collection<Integer> getStates() {
        return automaton.getStates();
    }

    @Override
    public @Nullable CompactTransition<O> getTransition(Integer location, SymbolicInput<I> input) {
        return automaton.getTransition(location, input);
    }

    @Override
    public @Nullable Integer getInitialState() {
        return automaton.getInitialState();
    }

    @Override
    public void clear() {
        this.automaton.clear();
    }

    @Override
    public Integer addState(@Nullable Void property) {
        return this.automaton.addState(property);
    }

    @Override
    public void setStateProperty(Integer state, Void property) {}

    @Override
    public void setTransitionProperty(CompactTransition<O> transition, O property) {
        this.automaton.setTransitionProperty(transition, property);
    }

    @Override
    public void removeAllTransitions(Integer state) {
        this.automaton.removeAllTransitions(state);
    }

    @Override
    public CompactTransition<O> createTransition(Integer successor, O properties) {
        return this.automaton.createTransition(successor, properties);
    }

    @Override
    public void setInitialState(Integer location) {
        automaton.setInitialState(location);
    }

    @Override
    public void setTransition(Integer state, SymbolicInput<I> input, @Nullable CompactTransition<O> transition) {
        this.automaton.setTransition(state, input, transition);
    }

    @Override
    public Void getStateProperty(Integer state) {
        return null;
    }

    @Override
    public O getTransitionProperty(CompactTransition<O> transition) {
        return transition.getProperty();
    }

    @Override
    public Integer getSuccessor(CompactTransition<O> transition) {
        return transition.getSuccId();
    }

    @Override
    public <V> MutableMapping<Integer, V> createStaticStateMapping() {
        return automaton.createStaticStateMapping();
    }

    @Override
    public <V> MutableMapping<Integer, V> createDynamicStateMapping() {
        return automaton.createDynamicStateMapping();
    }

    private void ensureThatCanAddTimer(List<MealyTimerInfo<O>> timers,
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
        localTimers.add(new MealyTimerInfo<>(name, initial, output, true));
        localTimers.sort(Comparator.comparingLong(MealyTimerInfo::initial));

        // Add self-looping transition:
        TimerTimeoutSymbol<I> newTimerSymbol = new TimerTimeoutSymbol<>(name);
        this.automaton.addAlphabetSymbol(newTimerSymbol);
        automaton.addTransition(location, newTimerSymbol, location, output);
    }

    @Override
    public void addOneShotTimer(Integer location, String name, long initial, O output, Integer target) {
        this.sortedTimers.putIfAbsent(location, new ArrayList<>());
        var localTimers = this.sortedTimers.get(location);

        ensureThatCanAddTimer(localTimers, name, initial, output, false);
        localTimers.add(new MealyTimerInfo<>(name, initial, output, false));
        localTimers.sort(Comparator.comparingLong(MealyTimerInfo::initial));

        // Add transition with location change:
        TimerTimeoutSymbol<I> newTimerSymbol = new TimerTimeoutSymbol<>(name);
        this.automaton.addAlphabetSymbol(newTimerSymbol);
        automaton.addTransition(location, newTimerSymbol, target, output);

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
        automaton.removeAllTransitions(location, new TimerTimeoutSymbol<>(timerName));
    }

    @Override
    public void addLocalReset(Integer location, InputSymbol<I> input) {
        // Ensure that input causes self-loop:
        var target = this.getSuccessor(location, input);
        if (target == null || !target.equals(location)) {
            throw new IllegalArgumentException("Provided input is not defined or does not trigger a self-loop.");
        }

        resets.putIfAbsent(location, new HashSet<>());
        resets.get(location).add(input);
    }

    @Override
    public void removeLocalReset(Integer location, InputSymbol<I> input) {
        var localResets = resets.get(location);
        if (localResets == null) {
            return;
        }

        localResets.remove(input);
    }
}
