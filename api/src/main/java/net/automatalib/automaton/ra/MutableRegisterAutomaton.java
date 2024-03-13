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

import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.symbol.ParameterizedSymbol;

/**
 * Mutable Register Automaton.
 *
 * @author falk
 */
public interface MutableRegisterAutomaton<L, T extends GuardedTransition>
        extends RegisterAutomaton<L, T>, MutableAutomaton<L, ParameterizedSymbol, T, Boolean, Void> {}
