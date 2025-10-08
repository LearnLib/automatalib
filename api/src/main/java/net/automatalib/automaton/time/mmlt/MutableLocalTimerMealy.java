package net.automatalib.automaton.time.mmlt;

import net.automatalib.alphabet.time.mmlt.NonDelayingInput;

public interface MutableLocalTimerMealy<S, I, O> {

    /**
     * Adds a new location to the MMLT.
     *
     * @return The newly-added location.
     */
    S addState();

    /**
     * Sets the initial location to the provided location.
     *
     * @param location New initial location
     */
    void setInitialState(S location);

    /**
     * Adds a transition for a non-delaying input, adding the input
     * to the automaton's alphabet if necessary.
     *
     * @param source Source location
     * @param input  Non-delaying input symbol
     * @param output Output
     * @param target Target location
     */
    void addTransition(S source, NonDelayingInput<I> input, O output, S target);

    /**
     * Removes the transition at the provided non-delaying input.
     * No effect if the location has no such transition.
     *
     * @param source Transition source location
     * @param input  Transition input
     */
    void removeTransition(S source, NonDelayingInput<I> input);

    /**
     * Adds a new periodic timer to the provided location.
     * <p>
     * Throws an error if a) the output is silent b) the initial value is less zero or less
     * c) the initial value exceeds that of a one-shot timer (-> timer never expires)
     * d) the timer will time out at the same time as a one-shot timer.
     *
     * @param location Location of the timer
     * @param name     Timer name
     * @param initial  Initial value
     * @param output   Output at timeout
     */
    void addPeriodicTimer(S location, String name, long initial, O output);

    /**
     * Adds a new one-shot timer to the provided location.
     * Removes all timers of that location with higher initial value, as these can no longer time out.
     * <p>
     * Throws an error if a) the output is silent b) the initial value is less zero or less
     * c) the initial value exceeds that of a one-shot timer (-> timer never expires)
     * d) the timer will time out at the same time as a periodic timer.
     *
     * @param location Location of the timer
     * @param name     Timer name
     * @param initial  Initial value
     * @param output   Output at timeout
     */
    void addOneShotTimer(S location, String name, long initial, O output, S target);

    /**
     * Removes the timer with the provided name.
     * No effect if the location has no such timer.
     *
     * @param location  Location of the timer
     * @param timerName Name of the timer
     */
    void removeTimer(S location, String timerName);

    /**
     * Adds a local reset at the provided input in the provided location.
     * Throws an error if the transition does not self-loop.
     *
     * @param location Source location
     * @param input    Input of the transition that should perform a local reset
     */
    void addLocalReset(S location, NonDelayingInput<I> input);

    /**
     * Removes a local reset at the provided input in the provided location.
     * No effect if the input does not trigger a local reset.
     *
     * @param location Source location
     * @param input    Input of the transition that performs a local reset.
     */
    void removeLocalReset(S location, NonDelayingInput<I> input);
}
