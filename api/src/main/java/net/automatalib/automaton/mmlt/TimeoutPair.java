/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.automaton.mmlt;

import java.util.List;

/**
 * Stores information about timers that expire after a given time from now.
 *
 * @param delay
 *         the offset to the next timeout
 * @param timers
 *         the timers expiring simultaneously at next timeout
 * @param <S>
 *         location type
 * @param <O>
 *         output symbol type
 */
public record TimeoutPair<S, O>(long delay, List<TimerInfo<S, O>> timers) {

    public boolean allPeriodic() {
        for (TimerInfo<S, O> t : timers) {
            if (!t.periodic()) {
                return false;
            }
        }
        return true;
    }
}
