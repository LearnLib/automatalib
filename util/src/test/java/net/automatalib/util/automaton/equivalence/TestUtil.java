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
package net.automatalib.util.automaton.equivalence;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;

final class TestUtil {

    static final Alphabet<Integer> ALPHABET;
    static final CompactDFA<Integer> LARGE_AUTOMATON_A;
    static final CompactDFA<Integer> LARGE_AUTOMATON_B;

    static {
        final int size = 1<<16 + 1; // (2^16 + 1)^2 > 2^31 - 1
        final int letter = 0;

        ALPHABET = Alphabets.singleton(letter);
        LARGE_AUTOMATON_A = new CompactDFA<>(ALPHABET, size);
        LARGE_AUTOMATON_B = new CompactDFA<>(ALPHABET, size);

        LARGE_AUTOMATON_A.addIntInitialState(true);
        LARGE_AUTOMATON_B.addIntInitialState(true);

        for (int i = 1; i < size - 1; i++) {
            LARGE_AUTOMATON_A.addIntState(true);
            LARGE_AUTOMATON_B.addIntState(true);
        }

        LARGE_AUTOMATON_A.addState(true);
        LARGE_AUTOMATON_B.addState(false);

        for (int i = 0; i < size - 1; i++) {
            LARGE_AUTOMATON_A.setTransition(i, letter, i + 1);
            LARGE_AUTOMATON_B.setTransition(i, letter, i + 1);
        }

    }

    private TestUtil() {
        // prevent instantiation
    }

}
