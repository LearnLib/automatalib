/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.automaton.vpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.vpa.SEVPAGraphView.SevpaViewEdge;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.semantic.DeterministicSemantics;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Interface for k-SEVPAs (k-module single entry visibly push-down automata), a visibly push-down automaton of specific
 * structure and semantics. Additionally -- unless specified other by an implementation -- this interface only accepts
 * well-matched words.
 * <p>
 * For more information on the semantics of VPAs see e.g. <a href="https://doi.org/10.1007/11523468_89">Congruences for
 * Visibly Pushdown Languages</a> by Alur, Kumar, Madhusudan and Viswanathan.
 *
 * @param <S>
 *         location type
 * @param <I>
 *         input alphabet type
 */
public interface SEVPA<S, I> extends UniversalAutomaton<S, I, S, Boolean, Void>,
                                     DeterministicSemantics,
                                     GraphViewable,
                                     InputAlphabetHolder<I> {

    @Override
    VPAlphabet<I> getInputAlphabet();

    S getModuleEntry(I callSym);

    int getNumStackSymbols();

    int encodeStackSym(S srcLoc, I callSym);

    @Nullable
    S getInternalSuccessor(S loc, I intSym);

    @Nullable
    S getReturnSuccessor(S loc, I retSym, int stackSym);

    @Nullable
    S getInitialState();

    @Override
    default Set<S> getInitialStates() {
        final S init = getInitialState();
        return init == null ? Collections.emptySet() : Collections.singleton(init);
    }

    @Override
    default Void getTransitionProperty(S transition) {
        return null;
    }

    @Override
    default Collection<S> getTransitions(S state, I input) {
        final VPAlphabet<I> alphabet = getInputAlphabet();
        return switch (alphabet.getSymbolType(input)) {
            case CALL:
                yield Collections.singleton(getModuleEntry(input));
            case INTERNAL:
                final S iSucc = getInternalSuccessor(state, input);
                yield iSucc == null ? Collections.emptyList() : Collections.singleton(iSucc);
            case RETURN:
                final int symbols = getNumStackSymbols();
                final List<S> result = new ArrayList<>(symbols);
                for (int i = 0; i < symbols; i++) {
                    final S rSucc = getReturnSuccessor(state, input, i);
                    if (rSucc != null) {
                        result.add(rSucc);
                    }
                }
                yield result;
        };
    }

    @Override
    default S getSuccessor(S transition) {
        return transition;
    }

    @Override
    default DeterministicAcceptorTS<State<S>, I> getSemantics() {
        return new SEVPASemantics<>(this);
    }

    @Override
    default Graph<S, SevpaViewEdge<S, I>> graphView() {
        return new SEVPAGraphView<>(this);
    }
}
