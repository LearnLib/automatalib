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
package net.automatalib.automata.procedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;
import net.automatalib.words.ProceduralInputAlphabet;

/**
 * @author frohme
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

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return this.accepts(input);
    }

}
