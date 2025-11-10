package net.automatalib.automaton.mmlt;

import java.util.List;

/**
 * Stores information about timers that expire after a given time from now.
 *
 * @param delay
 *         Offset to the next timeout
 * @param timers
 *         Timers expiring simultaneously at next timeout
 * @param <O>
 *         Output suffix type
 */
public record TimeoutPair<O>(long delay, List<MealyTimerInfo<O>> timers) {

    public boolean allPeriodic() {
        if (timers.size() == 1) {
            return timers.get(0).periodic();
        }
        return timers.stream().allMatch(MealyTimerInfo::periodic);
    }
}
