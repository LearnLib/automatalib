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
package net.automatalib.automata;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for declaring, that an automaton supports adding new alphabet symbols after its instantiation.
 *
 * @param <I>
 *         input alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public interface GrowableAlphabetAutomaton<I> {

    /**
     * Adds a new symbol to the alphabet of the automaton. Behavior depends on the implementation:
     * <p>
     * <ul> <li>Duplicate symbols may: be handled accordingly, be ignored or result in an error.</li> <li>After a new
     * symbol has been added the new transitions do not have to be defined.</li> </ul>
     *
     * @param symbol
     *         The symbol to add to the alphabet.
     */
    void addAlphabetSymbol(I symbol);

}
