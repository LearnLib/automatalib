package net.automatalib.automaton.mmlt.impl;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a reduced version of the semantics automaton of an MMLT.
 * This reduced version retains all configurations that can be reached by timeouts and non-delaying inputs.
 * It omits configurations that can only be reached by at least two subsequent time steps.
 * <p>
 * The resulting automaton suffices to check the equivalence of two MMLTs.
 * However, as the timeStep-transition is undefined in some configurations,
 * the automaton cannot execute any sequence of inputs that can be executed on an MMLT.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class ReducedMMLTSemantics<S, I, O> extends CompactMealy<TimedInput<I>, TimedOutput<O>> {

    private final static Logger logger = LoggerFactory.getLogger(ReducedMMLTSemantics.class);

    private final Map<State<S, O>, Integer> stateMap;

    private ReducedMMLTSemantics(Alphabet<TimedInput<I>> alphabet) {
        super(alphabet);
        this.stateMap = new HashMap<>();
    }

    public static <S, I, T, O> ReducedMMLTSemantics<S, I, O> forLocalTimerMealy(MMLT<S, I, T, O> automaton) {
        return forLocalTimerMealy(automaton, automaton.getSemantics());
    }

    private static <S, I, T1, T2, O> ReducedMMLTSemantics<S, I, O> forLocalTimerMealy(MMLT<S, I, T1, O> automaton, MMLTSemantics<S, I, T2, O> semantics) {
        // Create alphabet for expanded form:
        var alphabet = semantics.getInputAlphabet();

        ReducedMMLTSemantics<S, I, O> mealy = new ReducedMMLTSemantics<>(alphabet);

        // 1a: Add all configurations that can be reached via timeouts/non-delaying inputs, or are at least one time
        // step away from these configurations:
        for (var loc : automaton.getStates()) {
            getRelevantConfigurations(loc, automaton, semantics)
                    .forEach(c -> mealy.stateMap.put(c, mealy.addState()));
        }

        // 1b: Mark initial state:
        var initialConfig = semantics.getInitialState();
        mealy.setInitialState(mealy.stateMap.get(initialConfig));

        // 2. Add transitions:
        for (var config : mealy.stateMap.keySet()) {
            var sourceState = mealy.stateMap.get(config);

            for (var sym : alphabet) {
                var trans = semantics.getTransition(config, sym);
                var output = semantics.getTransitionOutput(trans);

                // Try to find matching state. If not found, leave undefined:
                int targetId = mealy.stateMap.getOrDefault(semantics.getSuccessor(trans), -1);
                if (targetId != -1) {
                    mealy.addTransition(sourceState, sym, targetId, output);
                }
            }
        }

        logger.debug("Expanded from {} locations to {} states.",
                automaton.getStates().size(), mealy.size());

        return mealy;
    }

    /**
     * Retrieves a list of configurations of the provided location
     * that can be reached via timeouts, and those that are at most one time step away from these.
     *
     * @param <S>       Location type
     * @param location  Considered location
     * @param automaton MMLT
     * @return List of the relevant configurations of the location
     */
    private static <S, I, T1, T2, O> List<State<S, O>> getRelevantConfigurations(S location,
                                                                                 MMLT<S, I, T1, O> automaton,
                                                                                 MMLTSemantics<S, I, T2, O> semantics) {

        List<State<S, O>> configurations = new ArrayList<>();

        State<S, O> currentConfiguration = new State<>(location, automaton.getSortedTimers(location));
        configurations.add(currentConfiguration);

        // Enumerate all timeouts, until we change to a different location or re-enter the entry configuration
        // of this location:
        while (true) {
            // Wait for next timeout:
            var trans = semantics.getTransition(currentConfiguration, new TimeoutSymbol<>());
            var output = semantics.getTransitionOutput(trans);
            var target = semantics.getSuccessor(trans);
            if (output.equals(semantics.getSilentOutput())) {
                break; // no timeout
            }

            if (output.delay() > 1) {
                // More than one time unit away -> add 1-step successor config.
                // If one time unit away, the successor is already in our list.
                var newGapConfig = currentConfiguration.copy();
                newGapConfig.decrement(1);
                configurations.add(newGapConfig);
            }

            if (target.isEntryConfig()) {
                break; // location change OR repeating behavior
            }
            configurations.add(target);
            currentConfiguration = target;
        }
        return configurations;
    }


    /**
     * Returns the state that represents the provided configuration.
     * <p>
     * If the configuration is not included and allowApproximate is set,
     * the closest configuration of the same location (with a smaller entry distance)
     * will be returned. If allowApproximate is not set, an error is thrown.
     *
     * @param configuration    Provided configuration
     * @param allowApproximate If set, the closest matching state is returned if the configuration is not part of the reduced automaton
     * @return Corresponding state in the reduced automaton
     */
    public Integer getStateForConfiguration(State<S, O> configuration, boolean allowApproximate) {

        State<S, O> closestMatch = null;

        for (var cfg : stateMap.keySet()) {
            if (!cfg.getLocation().equals(configuration.getLocation()) ||
                    cfg.getEntryDistance() > configuration.getEntryDistance()) {
                continue;
            }

            if (cfg.getEntryDistance() == configuration.getEntryDistance()) {
                // Perfect match:
                return stateMap.get(cfg);
            }

            if (closestMatch == null || cfg.getEntryDistance() > closestMatch.getEntryDistance()) {
                // Closer than previous candidate:
                closestMatch = cfg;
            }
        }

        if (closestMatch == null || !allowApproximate) {
            throw new IllegalStateException("Could not find corresponding configuration in expanded form.");
        }

        return stateMap.get(closestMatch);
    }

    /**
     * Returns the configuration that represents the provided state.
     * Throws an error if the state is not part of the reduced automaton.
     *
     * @param state Considered state
     * @return Corresponding configuration
     */
    public State<S, O> getConfigurationForState(int state) {
        return this.stateMap.entrySet().stream()
                .filter(e -> e.getValue() == state)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find corresponding configuration in expanded form."));
    }
}
