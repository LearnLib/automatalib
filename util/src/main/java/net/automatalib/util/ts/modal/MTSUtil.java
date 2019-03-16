/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.ts.modal;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalEdgeProperty;
import net.automatalib.ts.modal.MutableModalTransitionSystem;

/**
 * @author msc
 * @author frohme
 */
public final class MTSUtil {

    private MTSUtil() {
        // prevent instantiation
    }

    public static <S0, S1, I, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> CompactMTS<I> conjunction(

            ModalTransitionSystem<S0, I, T0, TP0> mc0, ModalTransitionSystem<S1, I, T1, TP1> mc1) {
        return conjunction(mc0, mc1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> A conjunction(
            ModalTransitionSystem<S0, I, T0, TP0> mc0,
            ModalTransitionSystem<S1, I, T1, TP1> mc1,
            AutomatonCreator<A, I> creator) {
        return Workset.map(new ModalConjunction<>(mc0, mc1, creator)).getSecond();
    }

    public static <S0, S1, I, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> CompactMTS<I> compose(

            ModalTransitionSystem<S0, I, T0, TP0> mc0, ModalTransitionSystem<S1, I, T1, TP1> mc1) {
        return compose(mc0, mc1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> A compose(
            ModalTransitionSystem<S0, I, T0, TP0> mc0,
            ModalTransitionSystem<S1, I, T1, TP1> mc1,
            AutomatonCreator<A, I> creator) {
        return Workset.map(new ModalParallelComposition<>(mc0, mc1, creator)).getSecond();
    }

}
