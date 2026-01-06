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
package net.automatalib.automaton.concept;

import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.ts.concept.DeterministicOutputTS;

/**
 * A deterministic output automaton is a {@link DeterministicAutomaton deterministic automaton} that can produce
 * {@link OutputAutomaton outputs}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <D>
 *         output domain type
 */
public interface DeterministicOutputAutomaton<S, I, T, D>
        extends OutputAutomaton<S, I, T, D>, DeterministicAutomaton<S, I, T>, DeterministicOutputTS<S, I, T, D> {}
