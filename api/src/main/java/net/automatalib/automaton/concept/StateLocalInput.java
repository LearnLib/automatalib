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
package net.automatalib.automaton.concept;

import java.util.Collection;

/**
 * Concept for transition systems, that can for each state return the set of input symbols for which successor states
 * are defined. Providing this information can prove useful in situations where managing a global input alphabet may not
 * be possible or (blindly) checking the existence of a transitions using a global alphabet is an expensive operation.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 */
public interface StateLocalInput<S, I> {

    /**
     * Returns the collection of input symbols for which a successor state is defined.
     *
     * @param state
     *         the state for which the defined inputs should be returned
     *
     * @return the collection of input symbols for which a successor state is defined.
     */
    Collection<I> getLocalInputs(S state);
}
