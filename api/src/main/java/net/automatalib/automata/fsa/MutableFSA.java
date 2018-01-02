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
package net.automatalib.automata.fsa;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.MutableAutomaton;

/**
 * @author fh
 */
@ParametersAreNonnullByDefault
public interface MutableFSA<S, I> extends FiniteStateAcceptor<S, I>, MutableAutomaton<S, I, S, Boolean, Void> {

    default void flipAcceptance() {
        for (S state : this) {
            setAccepting(state, !isAccepting(state));
        }
    }

    void setAccepting(S state, boolean accepting);

    @Nonnull
    S addState(boolean accepting);

    @Override
    default S addState() {
        return addState(false);
    }

    @Override
    default S addState(Boolean property) {
        boolean acc = (property != null) && property;
        return addState(acc);
    }

    @Override
    default S addInitialState() {
        return addInitialState(false);
    }

    default S addInitialState(boolean accepting) {
        S init = addState(accepting);
        setInitial(init, true);
        return init;
    }

    @Override
    default S addInitialState(Boolean property) {
        boolean acc = (property != null) && property;
        return addInitialState(acc);
    }

    @Override
    default void setStateProperty(S state, Boolean property) {
        boolean acc = (property != null) && property;
        setAccepting(state, acc);
    }

    @Override
    default void setTransitionProperty(S transition, Void property) {
    }

    @Override
    default S createTransition(S successor, Void properties) {
        return successor;
    }

    @Override
    default S copyTransition(S trans, S succ) {
        return succ;
    }
}
