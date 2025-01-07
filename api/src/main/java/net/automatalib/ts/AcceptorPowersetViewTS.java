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

import net.automatalib.ts.acceptor.AcceptorTS;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;

/**
 * A {@link PowersetViewTS} refinement that additionally captures the semantics of {@link AcceptorTS}s.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 * @param <OS>
 *         (original) state type
 */
public interface AcceptorPowersetViewTS<S, I, OS>
        extends PowersetViewTS<S, I, S, OS, OS>, DeterministicAcceptorTS<S, I> {}
