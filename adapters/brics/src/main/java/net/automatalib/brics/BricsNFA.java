/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.brics;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import net.automatalib.automata.fsa.NFA;

/**
 * Adapter class for wrapping a Brics automaton as an {@link NFA}.
 * <p>
 * This adapter is backed by the Brics automaton, so changes to the {@link Automaton} are reflected.
 * <p>
 * As a DFA can be regarded as a special case of an NFA, using this class on a Brics {@link Automaton} will always work.
 * However, determining successor states for input characters might be much less efficient than when using a {@link
 * BricsDFA}.
 *
 * @author Malte Isberner
 */
public class BricsNFA extends AbstractBricsAutomaton implements NFA<State, Character> {

    /**
     * Constructor.
     *
     * @param automaton
     *         the Brics automaton object
     */
    public BricsNFA(Automaton automaton) {
        super(automaton);
    }
}
