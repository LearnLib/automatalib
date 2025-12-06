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
package net.automatalib.automaton.transducer;

import net.automatalib.automaton.concept.DeterministicSuffixOutputAutomaton;
import net.automatalib.ts.output.DeterministicStateOutputTS;
import net.automatalib.word.Word;

/**
 * A state output automaton is a {@link DeterministicSuffixOutputAutomaton deterministic suffix output automaton} that
 * produces outputs based on its {@link DeterministicStateOutputTS state outputs}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <O>
 *         output symbol type
 */
public interface StateOutputAutomaton<S, I, T, O>
        extends DeterministicSuffixOutputAutomaton<S, I, T, Word<O>>, DeterministicStateOutputTS<S, I, T, O> {}
