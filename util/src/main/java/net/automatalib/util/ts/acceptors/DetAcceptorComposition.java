/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.util.ts.acceptors;

import net.automatalib.commons.util.Pair;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.ts.comp.DTSComposition;

public class DetAcceptorComposition<S1, S2, I, A1 extends DeterministicAcceptorTS<S1, I>, A2 extends DeterministicAcceptorTS<S2, I>>
        extends DTSComposition<S1, S2, I, S1, S2, A1, A2> implements DeterministicAcceptorTS<Pair<S1, S2>, I> {

    private final AcceptanceCombiner combiner;

    public DetAcceptorComposition(A1 ts1, A2 ts2, AcceptanceCombiner combiner) {
        super(ts1, ts2, true);
        this.combiner = combiner;
    }

    @Override
    public boolean isAccepting(Pair<S1, S2> state) {
        S1 s1 = state.getFirst();
        S2 s2 = state.getSecond();
        boolean acc1 = (s1 != null) && ts1.isAccepting(s1);
        boolean acc2 = (s2 != null) && ts2.isAccepting(s2);
        return combiner.combine(acc1, acc2);
    }
}
