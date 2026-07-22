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

/**
 * This package introduces the concept of <em>semantics</em> which allow one to distinguish between the structural and
 * semantic interpretations of transition systems and automata using the same type definitions.
 * <p>
 * The classes in this package act as semantic providers allowing, for example, structurally finite-state systems such
 * as {@link net.automatalib.automaton.vpa.SEVPA}s or {@link net.automatalib.automaton.mmlt.MMLT}s to provide a view on
 * their semantically infinite-state systems.
 */
package net.automatalib.semantic;
