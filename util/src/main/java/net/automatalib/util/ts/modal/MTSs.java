/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.fixpoint.Worksets;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.ts.modal.impl.CompactMTS;

/**
 * Operations on {@link ModalTransitionSystem}s.
 */
public final class MTSs {

    private MTSs() {
        // prevent instantiation
    }

    public static <S0, S1, I> CompactMTS<I> conjunction(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                        ModalTransitionSystem<S1, I, ?, ?> mts1) {
        return conjunction(mts0, mts1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, ?>, S, S0, S1, I, T> A conjunction(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return conjunctionWithMapping(mts0, mts1, creator).getSecond();
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, ?>, S, S0, S1, I, T> Pair<Map<Pair<S0, S1>, S>, A> conjunctionWithMapping(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalConjunction<>(mts0, mts1, creator));
    }

    public static <S0, S1, I> CompactMTS<I> compose(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                    ModalTransitionSystem<S1, I, ?, ?> mts1) {
        return compose(mts0, mts1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, ?, ?>, S, S0, S1, I> A compose(ModalTransitionSystem<S0, I, ?, ?> mts0,
                                                                                               ModalTransitionSystem<S1, I, ?, ?> mts1,
                                                                                               AutomatonCreator<A, I> creator) {
        return composeWithMapping(mts0, mts1, creator).getSecond();
    }

    public static <A extends MutableModalTransitionSystem<S, I, ?, ?>, S, S0, S1, I> Pair<Map<Pair<S0, S1>, S>, A> composeWithMapping(
            ModalTransitionSystem<S0, I, ?, ?> mts0,
            ModalTransitionSystem<S1, I, ?, ?> mts1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalParallelComposition<>(mts0, mts1, creator));
    }

    public static <AS, BS, I> boolean isRefinementOf(ModalTransitionSystem<AS, I, ?, ?> a,
                                                     ModalTransitionSystem<BS, I, ?, ?> b,
                                                     Collection<I> input) {

        final Set<Pair<AS, BS>> refinement = ModalRefinement.refinementRelation(a, b, input);

        final Set<AS> statesA = new HashSet<>(a.getInitialStates());
        final Set<BS> statesB = new HashSet<>(b.getInitialStates());

        for (Pair<AS, BS> p : refinement) {
            statesA.remove(p.getFirst());
            statesB.remove(p.getSecond());
        }

        return statesA.isEmpty() && statesB.isEmpty();
    }

}
