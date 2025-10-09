package net.automatalib.automaton.time.mmlt.semantics;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the semantics of an MMLT.
 * <p>
 * The semantics of an MMLT are defined with an associated Mealy machine. The states of this machine are
 * LocalTimerMealyConfiguration objects. These represent tuples of an active location and the current timer values
 * of this location. The inputs of the machine are non-delaying inputs, discrete time steps, and the
 * symbolic input timeout, which causes a delay until the next timeout.
 * <p>
 * The outputs of this machine are the outputs of the MMLT, extended with a delay. This delay is zero for all
 * transitions, except for those with the input timeout.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output type of the MMLT
 */
public interface LocalTimerMealySemantics<S, I, O> {
    /**
     * Returns the input alphabet of the semantics automaton. This consists of all non-delaying inputs
     * of the associated MMLT, as well as the time step symbol and the symbolic timeout symbol.
     *
     * @return Input alphabet
     */
    Alphabet<LocalTimerMealySemanticInputSymbol<I>> getInputAlphabet();

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return Silent output symbol
     */
    LocalTimerMealyOutputSymbol<O> getSilentOutput();

    /**
     * Returns the initial configuration of this MMLT. This is a tuple of the
     * initial location and its initial timer values.
     *
     * @return Initial configuration
     */
    LocalTimerMealyConfiguration<S, I, O> getInitialConfiguration();

    /**
     * Enters the suffix into the provided configuration and returns corresponding outputs.
     * <p>
     * Cannot provide TimeSequences with more than 1 symbol for the suffix, as this might trigger multiple timeouts
     * and thus lead to output sequences that are longer than the suffix.
     *
     * @param configuration Configuration
     * @param suffix        Suffix inputs
     * @return Outputs for the suffix
     */
    Word<LocalTimerMealyOutputSymbol<O>> computeSuffixOutput(LocalTimerMealyConfiguration<S, I, O> configuration, Word<LocalTimerMealySemanticInputSymbol<I>> suffix);

    /**
     * Enters the prefix and suffix sequences into the automaton and returns the outputs that occur for the suffixes.
     *
     * @param prefix Configuration prefix
     * @param suffix Suffix inputs
     * @return Outputs for the suffix
     */
    Word<LocalTimerMealyOutputSymbol<O>> computeSuffixOutput(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, Word<LocalTimerMealySemanticInputSymbol<I>> suffix);

    /**
     * Traces the provided prefix and returns the reached configuration.
     *
     * @param prefix Configuration prefix
     * @return Reached configuration
     */
    LocalTimerMealyConfiguration<S, I, O> traceInputs(Word<LocalTimerMealySemanticInputSymbol<I>> prefix);

    @NonNull
    LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source, LocalTimerMealySemanticInputSymbol<I> input);

    /**
     * Retrieves the transition in the semantics automaton that has the provided input and source configuration.
     * <p>
     * If the input is a sequence of time steps, the target of the transition is the configuration reached after
     * executing all time steps. If the sequence counts more than one step, the sequence might trigger multiple
     * timeouts. To avoid ambiguity, the transition output is set to null in this case.
     * If the sequence comprises a single time step only, the output is either that of a timeout or silence.
     *
     * @param source         Source configuration
     * @param input          Input symbol
     * @param maxWaitingTime Maximum time steps to wait for a timeout
     * @return Transition in semantics automaton
     */
    @NonNull
    LocalTimerMealySemanticTransition<S, I, O> getTransition(LocalTimerMealyConfiguration<S, I, O> source,
                                                             LocalTimerMealySemanticInputSymbol<I> input,
                                                             long maxWaitingTime);
}
