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
import java.util.Set;

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.data.Constants;
import net.automatalib.data.SymbolicDataValue.Register;
import net.automatalib.data.VarValuation;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.symbol.ParameterizedSymbol;
import net.automatalib.ts.acceptor.AcceptorTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author falk
 */
public interface RegisterAutomaton<L, T extends GuardedTransition> extends UniversalAutomaton<L, ParameterizedSymbol, T, Boolean, Void> {

    Constants getConstants();

    Collection<Register<?>> getRegisters();

    VarValuation getInitialRegisters();

    default @Nullable L getInitialState() {
        final Set<L> initialStates = getInitialStates();
        if (initialStates.isEmpty()) {
            return null;
        } else if (initialStates.size() == 1) {
            return initialStates.iterator().next();
        } else {
            throw new IllegalStateException("Register automata must not have multiple initial states");
        }
    }

    default AcceptorTS<State<L>, PSymbolInstance> asAcceptor() {
        return new AcceptorView<>(this);
    }
}
