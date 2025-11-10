package net.automatalib.automaton.mmlt;

import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.common.util.Triple;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.SymbolicInput;
import net.automatalib.symbol.time.TimerTimeoutSymbol;

/**
 * Base type for a Mealy Machine with Local Timers (MMLT).
 * <p>
 * An MMLT extends Mealy machines with local timers. Each location can have multiple timers. A timer can only be active
 * in its assigned location. All timers of a location reset when this location is entered from a different location or,
 * in case of the initial location, if the location is entered for the first time. There are periodic and one-shot
 * timers. Periodic timers reset themselves on timeout. They cannot cause a location change. One-shot timers can cause a
 * location change. They reset all timers of the target location at timeout. A location can have arbitrarily many
 * periodic timers and up to one one-shot timers. Timers are always reset to their initial value. The initial values
 * must be chosen so that a periodic timer never times out at the same time as a one-shot timer (to preserve
 * determinism). Multiple periodic timers may time out simultaneously. In this case, their outputs are combined using an
 * AbstractSymbolCombiner.
 * <p>
 * The timeout of a timer is modeled with a transition that has a {@link TimerTimeoutSymbol} as input. Other inputs are
 * {@link InputSymbol non-delaying}. A non-delaying input that causes a self-loop can cause a local reset. Then, all
 * timers of that location reset.
 * <p>
 * <b>Implementation note:</b> this class resembles a "structural" view on the MMLT. For a semantic view with
 * time-sensitive transductions, see the {@link #getSemantics()} method.
 *
 * @param <S>
 *         Location type
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output symbol type
 */
public interface MMLT<S, I, T, O> extends UniversalDeterministicAutomaton<S, I, T, Void, O>,
                                          InputAlphabetHolder<I>,
                                          GraphViewable {

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return Silent output symbol
     */
    O getSilentOutput();

    /**
     * Multiple periodic timers may out simultaneously. Then, their outputs are combined using an SymbolCombiner. This
     * method returns the combiner used for this model.
     *
     * @return symbol combiner used for this model.
     */
    SymbolCombiner<O> getOutputCombiner();

    /**
     * Returns the input alphabet of this MMLT, consisting of non-delaying inputs and timeout-symbols for its timers.
     *
     * @return Input alphabet
     */
    Alphabet<I> getInputAlphabet();

    /**
     * Retrieves the non-delaying inputs for this automaton. Excludes timer timeout symbols. May be empty.
     *
     * @return Untimed alphabet.
     */
    Alphabet<I> getUntimedAlphabet();

    /**
     * Indicates if the provided input performs a local reset in the given location.
     *
     * @param location
     *         Location
     * @param input
     *         Non-delaying input
     *
     * @return True if performing a local reset
     */
    boolean isLocalReset(S location, I input);

    /**
     * Returns the timers of the specified location sorted ascendingly by their initial time.
     *
     * @param location
     *         Location
     *
     * @return Sorted list of local timers. Empty if location has no timers.
     */
    List<MealyTimerInfo<S, O>> getSortedTimers(S location);

    /**
     * Returns the semantics automaton that describes the behavior of this MMLT.
     *
     * @return Semantics automaton
     */
    MMLTSemantics<S, I, ?, O> getSemantics();

    @Override
    default Graph<S, Triple<SymbolicInput<I>, O, S>> graphView() {
        return new MMLTGraphView<>(this);
    }
}
