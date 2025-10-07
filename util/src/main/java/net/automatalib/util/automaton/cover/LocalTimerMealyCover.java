package net.automatalib.util.automaton.cover;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.impl.time.mmlt.ILocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.impl.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import net.automatalib.word.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalTimerMealyCover {

    public static <S, I, O> Map<S, Word<ILocalTimerMealySemanticInputSymbol<I>>> getLocalTimerMealyLocationCover(LocalTimerMealy<S, I, O> automaton) {
        return getLocalTimerMealyLocationCover(automaton, true);
    }

    /**
     * Calculates a location cover for an MMLT.
     * <p>
     * The cover provides one prefix for each location of the MMLT.
     * The returned prefixes use inputs for the extended semantics.
     * If some locations are isolated, they are excluded from the cover.
     *
     * @param automaton       MMLT
     * @param allowIncomplete If set, no error is thrown if some locations are unreachable.
     * @return Location cover in format location -> prefix.
     */
    public static <S, I, O> Map<S, Word<ILocalTimerMealySemanticInputSymbol<I>>> getLocalTimerMealyLocationCover(LocalTimerMealy<S, I, O> automaton, boolean allowIncomplete) {
        Map<S, Word<ILocalTimerMealySemanticInputSymbol<I>>> locPrefixes = new HashMap<>();
        Map<LocalTimerMealyConfiguration<S, I, O>, Word<ILocalTimerMealySemanticInputSymbol<I>>> cfgPrefixes = new HashMap<>();
        List<LocalTimerMealyConfiguration<S, I, O>> queue = new ArrayList<>();

        queue.add(automaton.getSemantics().getInitialConfiguration());
        cfgPrefixes.put(automaton.getSemantics().getInitialConfiguration(), Word.epsilon());
        locPrefixes.put(automaton.getInitialState(), Word.epsilon());

        GrowingMapAlphabet<ILocalTimerMealySemanticInputSymbol<I>> exploreAlphabet = new GrowingMapAlphabet<>(automaton.getUntimedAlphabet());
        exploreAlphabet.add(new TimeoutSymbol<>());

        while (!queue.isEmpty()) {
            var current = queue.remove(0);
            for (var symbol : exploreAlphabet) {
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
