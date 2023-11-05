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
package net.automatalib.automaton;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.word.Word;
import org.testng.Assert;

final class SharedTestUtils {

    private SharedTestUtils() {}

    static <S, I> void checkOutput(DFA<S, I> dfa, Word<I> word, Boolean expected) {
        Assert.assertEquals(dfa.computeOutput(word), expected);
        Assert.assertEquals(dfa.computeStateOutput(dfa.getInitialState(), word), expected);
        Assert.assertEquals(dfa.computeSuffixOutput(word.prefix(word.length()), word.suffix(word.length())), expected);
    }

    static <S, I> void checkOutput(NFA<S, I> dfa, Word<I> word, Boolean expected) {
        Assert.assertEquals(dfa.computeOutput(word), expected);
        Assert.assertEquals(dfa.computeSuffixOutput(word.prefix(word.length()), word.suffix(word.length())), expected);
    }
}