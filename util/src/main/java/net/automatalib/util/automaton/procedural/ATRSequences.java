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
package net.automatalib.util.automaton.procedural;

import java.util.Collections;
import java.util.Map;

import net.automatalib.word.Word;

/**
 * A data class for aggregating access sequences, terminating sequences, and return sequences.
 * <p>
 * An access sequence of a procedure is an input word that guarantees to transition a procedural system from its initial
 * state to the initial state of the respective procedure.
 * <p>
 * A terminating sequence of a procedure is an input word that guarantees to transition the respective procedure from
 * its initial state to an accepting state. The terminating sequence may contain nested invocations to other
 * procedures.
 * <p>
 * A terminating sequence of a procedure is an input word that guarantees to transition the procedural system from an
 * accepting state of the respective procedure to an accepting state of the procedural system, given that the procedure
 * has been reached by the corresponding access sequence.
 *
 * @param <I>
 *         input symbol type
 */
public final class ATRSequences<I> {

    public final Map<I, Word<I>> accessSequences;
    public final Map<I, Word<I>> terminatingSequences;
    public final Map<I, Word<I>> returnSequences;

    public ATRSequences(Map<I, Word<I>> accessSequences,
                        Map<I, Word<I>> terminatingSequences,
                        Map<I, Word<I>> returnSequences) {
        this.accessSequences = Collections.unmodifiableMap(accessSequences);
        this.terminatingSequences = Collections.unmodifiableMap(terminatingSequences);
        this.returnSequences = Collections.unmodifiableMap(returnSequences);
    }
}
