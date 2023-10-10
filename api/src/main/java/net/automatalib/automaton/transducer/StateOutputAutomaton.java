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
package net.automatalib.automaton.transducer;

import net.automatalib.automaton.concept.DetSuffixOutputAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.ts.output.DeterministicStateOutputTS;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public interface StateOutputAutomaton<S, I, T, O>
        extends DetSuffixOutputAutomaton<S, I, T, Word<O>>, DeterministicStateOutputTS<S, I, T, O> {

    @Override
    default Word<O> computeStateOutput(S state, Iterable<? extends I> input) {
        final WordBuilder<O> result = Output.getBuilderFor(input);

        trace(state, input, result);

        return result.toWord();
    }
}
