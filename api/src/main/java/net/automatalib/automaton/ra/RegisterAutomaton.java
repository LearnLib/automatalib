/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.automaton.ra;

import java.util.Collection;
import java.util.List;

import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.data.SymbolicDataValue.Register;
import net.automatalib.data.VarValuation;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.symbol.ParameterizedSymbol;
import net.automatalib.word.Word;

/**
 *
 * @author falk
 */
public interface RegisterAutomaton extends DeterministicAutomaton<RALocation, ParameterizedSymbol, Transition> {

    boolean accepts(Word<PSymbolInstance> dw);

    RALocation getLocation(Word<PSymbolInstance> dw);

    VarValuation getInitialRegisters();

    List<Transition> getTransitions();

    List<Transition> getInputTransitions();

    Collection<RALocation> getInputStates();

    Collection<Register> getRegisters();
}
