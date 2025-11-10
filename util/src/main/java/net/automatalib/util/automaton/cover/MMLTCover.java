package net.automatalib.util.automaton.cover;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.word.Word;

import java.util.*;

public class MMLTCover {

    public static <S, I, T, O> Map<S, Word<TimedInput<I>>> getLocalTimerMealyLocationCover(MMLT<S, I, T, O> automaton, Collection<TimedInput<I>> inputs) {
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
    public static <S, I, T, O> Map<S, Word<TimedInput<I>>> getLocalTimerMealyLocationCover(MMLT<S, I, T, O> automaton, Collection<TimedInput<I>> inputs, boolean ignoreTimeStep, boolean allowIncomplete) {
        return getLocalTimerMealyLocationCover(automaton, automaton.getSemantics(), inputs, ignoreTimeStep, allowIncomplete);
    }

    public static <S, I, T1, T2, O> Map<S, Word<TimedInput<I>>> getLocalTimerMealyLocationCover(MMLT<S, I, T1, O> automaton, MMLTSemantics<S, I, T2, O> semantics, Collection<TimedInput<I>> inputs, boolean ignoreTimeStep, boolean allowIncomplete) {
        Map<S, Word<TimedInput<I>>> locPrefixes = new HashMap<>();
        Map<State<S, O>, Word<TimedInput<I>>> cfgPrefixes = new HashMap<>();
        List<State<S, O>> queue = new ArrayList<>();

        queue.add(semantics.getInitialState());
        cfgPrefixes.put(semantics.getInitialState(), Word.epsilon());
        locPrefixes.put(automaton.getInitialState(), Word.epsilon());

        while (!queue.isEmpty()) {
            var current = queue.remove(0);
            for (var symbol : inputs) {
                if (symbol instanceof TimeStepSequence<I>) {
                    continue;
                }

                var trans = semantics.getTransition(current, symbol);
                var succ = semantics.getSuccessor(trans);
                if (succ.equals(current)) {
                    continue; // self-loop
                }
                if (!cfgPrefixes.containsKey(succ)) {
                    var newPrefix = cfgPrefixes.get(current).append(symbol);
                    cfgPrefixes.put(succ, newPrefix);
                    queue.add(succ);

                    if (succ.isEntryConfig()) {
                        locPrefixes.put(succ.getLocation(), newPrefix);
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
