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

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.acceptors.AcceptorTS;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;

public final class Acceptors {

    private Acceptors() {
    }

    public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> and(
            TS1 ts1,
            TS2 ts2) {
        return combine(ts1, ts2, AcceptanceCombiner.AND);
    }

    public static <S1, S2, I, T1, T2, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> combine(
            TS1 ts1,
            TS2 ts2,
            AcceptanceCombiner combiner) {
        return new DetAcceptorComposition<>(ts1, ts2, combiner);
    }

    public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> or(
            TS1 ts1,
            TS2 ts2) {
        return combine(ts1, ts2, AcceptanceCombiner.OR);
    }

    public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> xor(
            TS1 ts1,
            TS2 ts2) {
        return combine(ts1, ts2, AcceptanceCombiner.XOR);
    }

    public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> equiv(
            TS1 ts1,
            TS2 ts2) {
        return combine(ts1, ts2, AcceptanceCombiner.EQUIV);
    }

    public static <S1, S2, I, TS1 extends DeterministicAcceptorTS<S1, I>, TS2 extends DeterministicAcceptorTS<S2, I>> DetAcceptorComposition<S1, S2, I, TS1, TS2> impl(
            TS1 ts1,
            TS2 ts2) {
        return combine(ts1, ts2, AcceptanceCombiner.IMPL);
    }

    public static <S> Mapping<S, Boolean> acceptance(final AcceptorTS<S, ?> acceptor) {
        return acceptor::isAccepting;
    }

}
