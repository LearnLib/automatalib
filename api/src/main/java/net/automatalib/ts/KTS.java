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
package net.automatalib.ts;

import net.automatalib.graph.concept.KripkeInterpretation;

/**
 * A Kripke Transition System combines the properties of a {@link TransitionSystem} and a {@link KripkeInterpretation}.
 *
 * @param <S>
 *         state class
 * @param <I>
 *         input symbol class
 * @param <T>
 *         transition class
 * @param <AP>
 *         atomic proposition class
 */
public interface KTS<S, I, T, AP> extends TransitionSystem<S, I, T>, KripkeInterpretation<S, AP> {}
