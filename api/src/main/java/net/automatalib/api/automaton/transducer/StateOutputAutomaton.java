/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.api.automaton.transducer;

import net.automatalib.api.automaton.concept.DetSuffixOutputAutomaton;
import net.automatalib.api.automaton.concept.Output;
import net.automatalib.api.ts.output.DeterministicStateOutputTS;
import net.automatalib.api.word.Word;
import net.automatalib.api.word.WordBuilder;

public interface StateOutputAutomaton<S, I, T, O>
        extends DetSuffixOutputAutomaton<S, I, T, Word<O>>, DeterministicStateOutputTS<S, I, T, O> {

    @Override
    default Word<O> computeStateOutput(S state, Iterable<? extends I> input) {
        final WordBuilder<O> result = Output.getBuilderFor(input);

        trace(state, input, result);

        return result.toWord();
    }
}
