package net.automatalib.automaton.time.impl.mmlt;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.ILocalTimerMealyInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimerTimeoutSymbol;
import net.automatalib.automaton.time.mmlt.AbstractSymbolCombiner;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.automaton.time.mmlt.MutableLocalTimerMealy;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealySemantics;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * Implements a LocalTimerMealy that is mutable.
 * The structure automaton is backed by a CompactMealy automaton.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class CompactLocalTimerMealy<I, O> implements LocalTimerMealy<Integer, I, O>, MutableLocalTimerMealy<Integer, I, O> {
    private final CompactMealy<ILocalTimerMealyInputSymbol<I>, O> automaton;
    private final Map<Integer, List<MealyTimerInfo<O>>> sortedTimers; // location -> (sorted timers)
    private final Map<Integer, Set<NonDelayingInput<I>>> resets; // location -> inputs (that reset all timers)

    private final GrowingAlphabet<NonDelayingInput<I>> untimedAlphabet;

    private final O silentOutput;
    private final AbstractSymbolCombiner<O> outputCombiner;

    /**
     * Initializes a new CompactLocalTimerMealy.
     *
     * @param nonDelayingInputs Non-delaying inputs used by this MMLT.
     * @param silentOutput      The silent output used by this MMLT.
     * @param outputCombiner    The combiner function for simultaneous timeouts of periodic timers.
     */
    public CompactLocalTimerMealy(Collection<NonDelayingInput<I>> nonDelayingInputs,
                                  O silentOutput,
                                  AbstractSymbolCombiner<O> outputCombiner
    ) {
        this.untimedAlphabet = new GrowingMapAlphabet<>();
        this.untimedAlphabet.addAll(nonDelayingInputs);

        this.sortedTimers = new HashMap<>();
        this.resets = new HashMap<>();

        this.silentOutput = silentOutput;
        this.outputCombiner = outputCombiner;

        // Prepare compact Mealy:
        GrowingMapAlphabet<ILocalTimerMealyInputSymbol<I>> inputAlphabet = new GrowingMapAlphabet<>();
        inputAlphabet.addAll(nonDelayingInputs);
        this.automaton = new CompactMealy<>(inputAlphabet);
    }


    @Override
    public O getSilentOutput() {
        return this.silentOutput;
    }

    @Override
    public AbstractSymbolCombiner<O> getOutputCombiner() {
        return this.outputCombiner;
    }

    @Override
    public Alphabet<ILocalTimerMealyInputSymbol<I>> getInputAlphabet() {
        return this.automaton.getInputAlphabet();
    }

    @Override
    public Alphabet<NonDelayingInput<I>> getUntimedAlphabet() {
        return this.untimedAlphabet;
    }

    @Override
    public boolean isLocalReset(Integer location, NonDelayingInput<I> input) {
        return this.resets.getOrDefault(location, Collections.emptySet()).contains(input);
    }

    @Override
    public List<MealyTimerInfo<O>> getSortedTimers(Integer location) {
        return Collections.unmodifiableList(this.sortedTimers.getOrDefault(location, Collections.emptyList()));
    }

    @Override
    public LocalTimerMealySemantics<Integer, I, O> getSemantics() {
        return new net.automatalib.automaton.time.impl.mmlt.LocalTimerMealySemantics<>(this);
    }

    @Override
    public Collection<Integer> getStates() {
        return automaton.getStates();
    }

    @Override
    public @Nullable LocalTimerMealyTransition<Integer, O> getTransition(Integer location, ILocalTimerMealyInputSymbol<I> input) {
        var trans = this.automaton.getTransition(location, input);
        if (trans == null) {
            return null;
        }
        return new LocalTimerMealyTransition<>(trans.getSuccId(), trans.getProperty());
    }

    @Override
    public @Nullable Integer getInitialState() {
        return automaton.getInitialState();
    }

    @Override
    public Integer addState() {
        return automaton.addState();
    }

    @Override
    public void setInitialState(Integer location) {
        automaton.setInitialState(location);
    }

    @Override
    public void addTransition(Integer source, NonDelayingInput<I> input, O output, Integer target) {
        automaton.addAlphabetSymbol(input);
        this.untimedAlphabet.addSymbol(input);

        automaton.removeAllTransitions(source, input); // remove transition if already defined
        automaton.addTransition(source, input, target, output);
    }

    @Override
    public void removeTransition(Integer source, NonDelayingInput<I> input) {
        automaton.removeAllTransitions(source, input);
    }

    private void ensureThatCanAddTimer(List<MealyTimerInfo<O>> timers, String name, long initial, O output, boolean periodic) {
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
                throw new IllegalArgumentException(String.format("The initial value %d of '%s' exceeds that of a one-shot timer; will never time out.", initial, name));
            }
            if (periodic && (oldOneShot.get().initial() % initial == 0)) {
                // Our new periodic timer will time out at the same time as the existing one-shot timer.
                // This makes the model non-deterministic and is not allowed:
                throw new IllegalArgumentException(String.format("The timer '%s' times out at the same time as a one-shot timer (%d).", name, initial));
            }
        }
        if (!periodic) {
            // Our new one-shot timer is the one-shot timer with the highest initial value (or the only one).
            // Check that no timer with a lower initial value will time out at the same time:
            for (var timer : timers) {
                if (timer.initial() <= initial && initial % timer.initial() == 0) {
                    throw new IllegalArgumentException(String.format("The existing timer '%s' times out at the same time as the new one-shot timer (%d).", timer.name(), initial));
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
    public void addLocalReset(Integer location, NonDelayingInput<I> input) {
        // Ensure that input causes self-loop:
        var target = this.getSuccessor(location, input);
        if (target == null || !target.equals(location)) {
            throw new IllegalArgumentException("Provided input is not defined or does not trigger a self-loop.");
        }

        resets.putIfAbsent(location, new HashSet<>());
        resets.get(location).add(input);
    }

    @Override
    public void removeLocalReset(Integer location, NonDelayingInput<I> input) {
        var localResets = resets.get(location);
        if (localResets == null) {
            return;
        }

        localResets.remove(input);
    }
}
