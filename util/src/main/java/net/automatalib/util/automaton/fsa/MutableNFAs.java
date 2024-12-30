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
package net.automatalib.util.automaton.fsa;

import java.util.Collection;

import net.automatalib.automaton.fsa.MutableFSA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;

public final class MutableNFAs {

    private MutableNFAs() {
        // prevent instantiation
    }

    /**
     * Calculates the disjunction ("or") of two NFAs by merging their states and transitions. The structure of
     * {@code in} is copied into {@code out}.
     *
     * @param out
     *         the first NFA
     * @param in
     *         the second NFA
     * @param inputs
     *         the input symbols to consider for copying
     * @param <S1>
     *         output state type
     * @param <S2>
     *         input state type
     * @param <I>
     *         input symbol type
     */
    public static <S1, S2, I> void or(MutableFSA<S1, I> out, NFA<S2, I> in, Collection<? extends I> inputs) {

        final Mapping<S2, S1> mapping = AutomatonLowLevelCopy.copy(AutomatonCopyMethod.BFS, in, inputs, out);

        for (S2 s : in.getInitialStates()) {
            out.setInitial(mapping.get(s), true);
        }
    }
}
