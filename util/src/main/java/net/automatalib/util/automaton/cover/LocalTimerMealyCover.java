package net.automatalib.util.automaton.cover;

import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import net.automatalib.alphabet.time.mmlt.TimeStepSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import net.automatalib.word.Word;

import java.util.*;

public class LocalTimerMealyCover {

    public static <S, I, O> Map<S, Word<LocalTimerMealySemanticInputSymbol<I>>> getLocalTimerMealyLocationCover(LocalTimerMealy<S, I, O> automaton, Collection<LocalTimerMealySemanticInputSymbol<I>> inputs) {
        return getLocalTimerMealyLocationCover(automaton, inputs, true, true);
    }

    /**
     * Calculates a location cover for an MMLT.
     * <p>
     * The cover provides one prefix for each location of the MMLT.
     * The returned prefixes use only the provided inputs.
     * Time steps are not needed to calculate a full cover. If set, they are ignored when calculating the cover,
     * even if they are part of the provided inputs.
     * <p>
     * If some locations are isolated, they are excluded from the cover.
     * If some locations cannot be reached with the provided inputs, they are also excluded.
     *
     * @param automaton       MMLT
     * @param inputs          The inputs to use when calculating the cover. Must be an ordered collection.
     * @param ignoreTimeStep  If this option is set, time steps are ignored when calculating the cover,
     *                        even if they are part of the provided inputs. This is a convenience option for not having to
     *                        create a separate alphabet when calculating location covers.
     * @param allowIncomplete If set, no error is thrown if some locations are unreachable.
     * @return Location cover in format location -> prefix.
     */
    public static <S, I, O> Map<S, Word<LocalTimerMealySemanticInputSymbol<I>>> getLocalTimerMealyLocationCover(LocalTimerMealy<S, I, O> automaton, Collection<LocalTimerMealySemanticInputSymbol<I>> inputs, boolean ignoreTimeStep, boolean allowIncomplete) {
        Map<S, Word<LocalTimerMealySemanticInputSymbol<I>>> locPrefixes = new HashMap<>();
        Map<LocalTimerMealyConfiguration<S, I, O>, Word<LocalTimerMealySemanticInputSymbol<I>>> cfgPrefixes = new HashMap<>();
        List<LocalTimerMealyConfiguration<S, I, O>> queue = new ArrayList<>();

        queue.add(automaton.getSemantics().getInitialConfiguration());
        cfgPrefixes.put(automaton.getSemantics().getInitialConfiguration(), Word.epsilon());
        locPrefixes.put(automaton.getInitialState(), Word.epsilon());

        while (!queue.isEmpty()) {
            var current = queue.remove(0);
            for (var symbol : inputs) {
                if (symbol instanceof TimeStepSequence<I>) {
                    continue;
                }

                var trans = automaton.getSemantics().getTransition(current, symbol);
                if (trans.target().equals(current)) {
                    continue; // self-loop
                }
                if (!cfgPrefixes.containsKey(trans.target())) {
                    var newPrefix = cfgPrefixes.get(current).append(symbol);
                    cfgPrefixes.put(trans.target(), newPrefix);
                    queue.add(trans.target());

                    if (trans.target().isEntryConfig()) {
                        locPrefixes.put(trans.target().getLocation(), newPrefix);
                    }
                }
            }
        }

        if (!allowIncomplete && locPrefixes.size() != automaton.getStates().size()) {
            throw new AssertionError("Incomplete state cover.");
        }

        return locPrefixes;
    }
}
