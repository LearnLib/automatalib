package net.automatalib.automaton.mmlt;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.word.Word;

/**
 * Defines the semantics of an MMLT.
 * <p>
 * The semantics of an MMLT are defined with an associated Mealy machine. The states of this machine are
 * LocalTimerMealyConfiguration objects. These represent tuples of an active location and the current timer values of
 * this location. The inputs of the machine are non-delaying inputs, discrete time steps, and the symbolic input
 * timeout, which causes a delay until the next timeout.
 * <p>
 * The outputs of this machine are the outputs of the MMLT, extended with a delay. This delay is zero for all
 * transitions, except for those with the input {@link TimeoutSymbol}.
 *
 * @param <S>
 *         Location type
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output type of the MMLT
 */
public interface MMLTSemantics<S, I, T, O>
        extends MealyTransitionSystem<State<S, O>, TimedInput<I>, T, TimedOutput<O>>,
                SuffixOutput<TimedInput<I>, Word<TimedOutput<O>>>,
                InputAlphabetHolder<TimedInput<I>> {

    /**
     * Returns the input alphabet of the semantics automaton. This consists of all non-delaying inputs of the associated
     * MMLT, as well as the time step symbol and the symbolic timeout symbol.
     *
     * @return Input alphabet
     */
    Alphabet<TimedInput<I>> getInputAlphabet();

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return Silent output symbol
     */
    TimedOutput<O> getSilentOutput();

    /**
     * Retrieves the transition in the semantics automaton that has the provided input and source configuration.
     * <p>
     * If the input is a sequence of time steps, the target of the transition is the configuration reached after
     * executing all time steps. If the sequence counts more than one step, the sequence might trigger multiple
     * timeouts. To avoid ambiguity, the transition output is set to null in this case. If the sequence comprises a
     * single time step only, the output is either that of a timeout or silence.
     *
     * @param source
     *         Source configuration
     * @param input
     *         Input symbol
     * @param maxWaitingTime
     *         Maximum time steps to wait for a timeout
     *
     * @return Transition in semantics automaton
     */
    T getTransition(State<S, O> source, TimedInput<I> input, long maxWaitingTime);
}
