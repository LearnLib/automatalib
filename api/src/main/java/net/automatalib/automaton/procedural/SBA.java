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
package net.automatalib.automaton.procedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;

/**
 * A system of behavioral automata. {@link SBA}s extend the idea of {@link SPA}s by supporting a direct response
 * mechanism which makes them especially suited for reactive systems with immediate feedback. Language-wise, this can be
 * thought of as a form of prefix-closure of {@link SPA} languages. However, {@link SBA}s are also able to model
 * non-terminating procedures which are not expressible via (the prefixes of) {@link SPA} words.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 */
public interface SBA<S, I>
        extends ProceduralSystem<I, DFA<?, I>>, DeterministicAcceptorTS<S, I>, SuffixOutput<I, Boolean> {

    @Override
    default Collection<I> getProceduralInputs(Collection<I> constraints) {
        final ProceduralInputAlphabet<I> alphabet = getInputAlphabet();
        final Map<I, DFA<?, I>> procedures = getProcedures();

        final List<I> result = new ArrayList<>(Math.min(alphabet.size(), constraints.size()));

        for (I i : constraints) {
            if (procedures.containsKey(i) || alphabet.isInternalSymbol(i) || alphabet.isReturnSymbol(i)) {
                result.add(i);
            }
        }

        return result;
    }

}
