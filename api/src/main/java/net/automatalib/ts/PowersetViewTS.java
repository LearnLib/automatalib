/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.ts;

import java.util.Collection;

/**
 * A powerset view is a deterministic view on a (potentially) non-deterministic transition system. Conceptually, this
 * view traverses sets of states of the original transition system.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <OS>
 *         original state type
 * @param <OT>
 *         original transition type
 */
public interface PowersetViewTS<S, I, T, OS, OT> extends DeterministicTransitionSystem<S, I, T> {

    /**
     * Returns the original states that the given view state represents.
     *
     * @param state
     *         the view state
     *
     * @return the original states that the given view state represents
     */
    Collection<OS> getOriginalStates(S state);

    /**
     * Returns the original transitions that the given view transition represents.
     *
     * @param transition
     *         the view transition
     *
     * @return the original transitions that the given view transition represents
     */
    Collection<OT> getOriginalTransitions(T transition);

}
