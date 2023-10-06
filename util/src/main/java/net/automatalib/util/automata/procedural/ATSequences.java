/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.util.automata.procedural;

import java.util.Collections;
import java.util.Map;

import net.automatalib.words.Word;

/**
 * A data class for aggregating access sequences and terminating sequences.
 * <p>
 * An access sequence of a procedure is an input word that guarantees to transition the procedural system from its
 * initial state to the initial state of the respective procedure.
 * <p>
 * A terminating sequence of a procedure is an input word that guarantees to transition the respective procedure from
 * its initial state to a returnable state. The terminating sequence may contain nested invocations to other
 * procedures.
 *
 * @param <I>
 *         input symbol type
 */
public final class ATSequences<I> {

    public final Map<I, Word<I>> accessSequences;
    public final Map<I, Word<I>> terminatingSequences;

    public ATSequences(Map<I, Word<I>> accessSequences, Map<I, Word<I>> terminatingSequences) {
        this.accessSequences = Collections.unmodifiableMap(accessSequences);
        this.terminatingSequences = Collections.unmodifiableMap(terminatingSequences);
    }
}
