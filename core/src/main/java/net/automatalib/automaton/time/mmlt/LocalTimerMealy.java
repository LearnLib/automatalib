package net.automatalib.automaton.time.mmlt;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.time.mmlt.ILocalTimerMealyInputSymbol;
import net.automatalib.alphabet.impl.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.graph.UniversalAutomatonGraphView;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealySemantics;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.visualization.VisualizationHelper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Base type for a Mealy Machine with Local Timers (MMLT).
 * <p>
 * An MMLT extends Mealy machines with local timers.
 * Each location can have multiple timers. A timer can only be active in its assigned location.
 * All timers of a location reset when this location is entered from a different location or, in case of the initial
 * location, if the location is entered for the first time.
 * There are periodic and one-shot timers. Periodic timers reset themselves on timeout. They cannot cause a location
 * change. One-shot timers can cause a location change. They reset all timers of the target location at timeout.
 * A location can have arbitrarily many periodic timers and up to one one-shot timers.
 * Timers are always reset to their initial value. The initial values must be chosen so that a periodic timer never
 * times out at the same time as a one-shot timer (to preserve determinism). Multiple periodic timers may time out
 * simultaneously. In this case, their outputs are combined using an AbstractSymbolCombiner.
 * <p>
 * The timeout of a timer is modeled with a transition that has a TimerTimeoutSymbol as input. Other inputs are called
 * non-delaying. A non-delaying input that causes a self-loop can cause a local reset. Then, all timers of that
 * location reset.
 * <p>
 * To be able to subclass existing automata, some methods refer to locations as "state".
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public interface LocalTimerMealy<S, I, O> extends UniversalDeterministicAutomaton<S, ILocalTimerMealyInputSymbol<I>, LocalTimerMealy.LocalTimerMealyTransition<S, O>, Void, O> {

    record LocalTimerMealyTransition<S, O>(@NonNull S successor, @NonNull O output) {

    }

    /**
     * Returns the symbol used for silent outputs.
     *
     * @return Silent output symbol
     */
    O getSilentOutput();

    /**
     * Multiple periodic timers may out simultaneously. Then, their outputs are combined
     * using an AbstractSymbolCombiner. This method returns the combiner used for
     * this model.
     *
     * @return Symbol combiner used for this model.
     */
    AbstractSymbolCombiner<O> getOutputCombiner();

    /**
     * Returns the input alphabet of this MMLT, consisting of non-delaying inputs
     * and timeout-symbols for its timers.
     *
     * @return Input alphabet
     */
    Alphabet<ILocalTimerMealyInputSymbol<I>> getInputAlphabet();

    /**
     * Retrieves the non-delaying inputs for this automaton.
     * Excludes timer timeout symbols. May be empty.
     *
     * @return Untimed alphabet.
     */
    Alphabet<NonDelayingInput<I>> getUntimedAlphabet();

    @Override
    default O getTransitionProperty(LocalTimerMealyTransition<S, O> transition) {
        return transition.output();
    }

    @Override
    default S getSuccessor(LocalTimerMealyTransition<S, O> transition) {
        return transition.successor();
    }

    /**
     * Indicates if the provided input performs a local reset in the given location.
     *
     * @param location Location
     * @param input    Non-delaying input
     * @return True if performing a local reset
     */
    boolean isLocalReset(S location, NonDelayingInput<I> input);

    /**
     * Returns the timers of the specified location sorted ascendingly by their initial time.
     *
     * @param location Location
     * @return Sorted list of local timers. Empty if location has no timers.
     */
    List<MealyTimerInfo<O>> getSortedTimers(S location);

    /**
     * Returns the semantics automaton that describes the behavior of this MMLT.
     *
     * @return Semantics automaton
     */
    default LocalTimerMealySemantics<S, I, O> getSemantics() {
        return new LocalTimerMealySemantics<>(this);
    }

    // =======================================

    @Override
    default UniversalGraph<S, TransitionEdge<ILocalTimerMealyInputSymbol<I>, LocalTimerMealyTransition<S, O>>, Void, TransitionEdge.Property<ILocalTimerMealyInputSymbol<I>, O>> transitionGraphView(Collection<? extends ILocalTimerMealyInputSymbol<I>> inputs) {
        return new LocalTimerMealyGraphView<>(this, inputs, false, false);
    }

    default UniversalGraph<S, TransitionEdge<ILocalTimerMealyInputSymbol<I>, LocalTimerMealyTransition<S, O>>, Void, TransitionEdge.Property<ILocalTimerMealyInputSymbol<I>, O>> transitionGraphView() {
        return this.transitionGraphView(getInputAlphabet());
    }

    default UniversalGraph<S, TransitionEdge<ILocalTimerMealyInputSymbol<I>, LocalTimerMealyTransition<S, O>>, Void, TransitionEdge.Property<ILocalTimerMealyInputSymbol<I>, O>> transitionGraphView(boolean colorEdges, boolean includeResets) {
        return new LocalTimerMealyGraphView<>(this, getInputAlphabet(), colorEdges, includeResets);
    }

    // =======================================

    @Override
    default Void getStateProperty(S state) {
        return null;
    }

    // We do not want to provide tracing abilities for inputs of the structure automaton:
    @Override
    default Set<S> getStates(Iterable<? extends ILocalTimerMealyInputSymbol<I>> input) {
        throw new IllegalStateException("Not supported. Use the semantics automaton to trace inputs.");
    }

    @Override
    @Nullable
    default S getSuccessor(S state, Iterable<? extends ILocalTimerMealyInputSymbol<I>> input) {
        throw new IllegalStateException("Not supported. Use the semantics automaton to trace inputs.");
    }

    @Override
    @Nullable
    default S getState(Iterable<? extends ILocalTimerMealyInputSymbol<I>> input) {
        throw new IllegalStateException("Not supported. Use the semantics automaton to trace inputs.");
    }

    // =======================================

    class LocalTimerMealyGraphView<SX, IX, OX> extends
            UniversalAutomatonGraphView<SX, ILocalTimerMealyInputSymbol<IX>, LocalTimerMealy.LocalTimerMealyTransition<SX, OX>, Void, OX, LocalTimerMealy<SX, IX, OX>> {

        private final boolean colorEdges;
        private final boolean includeResets;

        public LocalTimerMealyGraphView(LocalTimerMealy<SX, IX, OX> automaton,
                                        Collection<? extends ILocalTimerMealyInputSymbol<IX>> inputs,
                                        boolean colorEdges, boolean includeResets) {
            super(automaton, inputs);
            this.colorEdges = colorEdges;
            this.includeResets = includeResets;
        }

        @Override
        public VisualizationHelper<SX, TransitionEdge<ILocalTimerMealyInputSymbol<IX>, LocalTimerMealyTransition<SX, OX>>> getVisualizationHelper() {
            return new LocalTimerMealyVisualizationHelper<>(automaton, colorEdges, includeResets);
        }
    }

}
