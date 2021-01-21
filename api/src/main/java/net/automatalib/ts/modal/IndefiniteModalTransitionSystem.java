/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.ts.modal;

import net.automatalib.graphs.IndefiniteLTS;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

/**
 * An (indefinite) modal transition system is a {@link UniversalTransitionSystem} in which transitions can be marked as
 * {@link net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType#MAY may} or {@link
 * net.automatalib.ts.modal.transition.ModalEdgeProperty.ModalType#MUST must} edges.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <TP>
 *         (specific) transition property type
 */
public interface IndefiniteModalTransitionSystem<S, I, T, TP extends ModalEdgeProperty>
        extends UniversalTransitionSystem<S, I, T, Void, TP>, IndefiniteLTS<S, T, I> {}
