package net.automatalib.util.automaton.mmlt;

import net.automatalib.alphabet.impl.time.mmlt.ILocalTimerMealySemanticInputSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.automaton.time.mmlt.semantics.ReducedLocalTimerMealySemantics;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides various functions that are related MMLTs.
 */
public class LocalTimerMealyUtil {

    public static @Nullable <S1, S2, I, O> Word<ILocalTimerMealySemanticInputSymbol<I>> findSeparatingWord(LocalTimerMealy<S1, I, O> modelA, LocalTimerMealy<S2, I, O> modelB) {
        var expandedA = ReducedLocalTimerMealySemantics.forLocalTimerMealy(modelA);
        var expandedB = ReducedLocalTimerMealySemantics.forLocalTimerMealy(modelB);

        var separatingWord = Automata.findSeparatingWord(expandedA, expandedB, expandedA.getInputAlphabet());

        if (separatingWord != null) {
            var outputA = modelA.getSemantics().computeSuffixOutput(Word.epsilon(), separatingWord);
            var outputB = modelB.getSemantics().computeSuffixOutput(Word.epsilon(), separatingWord);
            if (outputA.equals(outputB)) {
                throw new AssertionError("Invalid separating word.");
            }
        }

        return separatingWord;
    }

    /**
     * Returns the number of configurations for the given location. This is defined as follows:
     * - When l has no timers: one
     * - When l has a one-shot timer: initial value of this timer
     * - When l only has periodic timers: least-common multiple of their initial values
     *
     * @param automaton Considered automaton
     * @param location  Considered location
     * @param <L>       Location type
     * @param <I>       Input type
     * @param <O>       Output type
     * @return Maximum configuration time. Long.MAX_VALUE, if exceeding integer maximum.
     */
    public static <L, I, O> long getConfigurationCount(LocalTimerMealy<L, I, O> automaton, L location) {
        Collection<MealyTimerInfo<O>> timers = automaton.getSortedTimers(location);
        if (timers.isEmpty()) {
            return 1;
        }

        // Single timer: take that timer's value:
        if (timers.size() == 1) {
            return timers.stream().findFirst().get().initial();
        }

        var optOneShot = timers.stream().filter(t -> !t.periodic()).findFirst();
        if (optOneShot.isPresent()) {
            return optOneShot.get().initial(); // leave location at latest when one-shot expires
        }

        // The lcm of multiple numbers is equal to the product of their multiple with their gcd.
        // Therefore: calculate gcd and multiple. Then, divide multiple by gcd.
        List<BigInteger> bigTimeouts = timers.stream().map(t -> new BigInteger(String.valueOf(t.initial()))).toList();

        // Calculate gcd of all timeouts:
        BigInteger gcd = bigTimeouts.get(0);
        BigInteger multiple = bigTimeouts.get(0);
        for (int i = 1; i < bigTimeouts.size(); i++) {
            gcd = gcd.gcd(bigTimeouts.get(i));
            multiple = multiple.multiply(bigTimeouts.get(i));
        }
        BigInteger lcm = multiple.divide(gcd);

        try {
            return lcm.longValueExact();
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }


    /**
     * Returns the maximum initial value of all timers.
     *
     * @return Maximum initial timer value. Zero, if no timers are used.
     */
    public static <S, I, O> long getMaximumInitialTimerValue(LocalTimerMealy<S, I, O> automaton) {
        long maxValue = 0;
        for (S loc : automaton.getStates()) {
            var optMaxInitial = automaton.getSortedTimers(loc).stream().mapToLong(MealyTimerInfo::initial).max();
            if (optMaxInitial.isPresent()) {
                maxValue = Math.max(maxValue, optMaxInitial.getAsLong());
            }
        }
        return maxValue;
    }

    /**
     * Retrieves the maximum delay to the next timeout in this model.
     *
     * @return Maximum timeout delay. Is at least one.
     */
    public static <S, I, O> long getMaximumTimeoutDelay(LocalTimerMealy<S, I, O> automaton) {
        long maxValue = 1;

        for (S loc : automaton.getStates()) {
            var timers = automaton.getSortedTimers(loc);
            if (timers.isEmpty()) {
                continue;
            }

            if (timers.size() == 1) {
                maxValue = Math.max(maxValue, timers.stream().findFirst().get().initial());
                continue;
            }

            // Get the least common multiple of the initial times:
            long lcm = LocalTimerMealyUtil.getConfigurationCount(automaton, loc);

            // Calculate expiration times of all timers:
            List<Long> timeouts = new ArrayList<>();
            for (var timer : automaton.getSortedTimers(loc)) {
                long currentValue = timer.initial();
                while (currentValue < lcm) {
                    timeouts.add(currentValue);
                    currentValue += timer.initial();
                }
            }

            // Find the largest distance between two successive expirations:
            timeouts.add(0L); // need to consider time to first expiration
            Collections.sort(timeouts);
            long maxDiff = 0;
            for (int i = 0; i < timeouts.size() - 1; i++) {
                long diff = timeouts.get(i + 1) - timeouts.get(i);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            maxValue = Math.max(maxValue, maxDiff);
        }

        return maxValue;
    }

}
