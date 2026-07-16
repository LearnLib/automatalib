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

import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.concept.InitialState;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.concept.SuffixOutput;
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
 * <p>
 * Note that this formalism integrates into the hierarchy of a non-deterministic {@link UniversalAutomaton} because a
 * single return symbol may identify multiple transitions depending on the current top-of-stack symbol. Via
 * {@link #getInternalSuccessor(Object, Object)} and {@link #getReturnSuccessor(Object, Object, int)}, these information
 * can be accessed deterministically. As a result, its <em>states</em> act more like locations than actual states. A
 * (deterministic, infinite-state) semantics view can be obtained via the {@link #getSemantics()} method.
 * <p>
 * For convenience, this type also implements {@link SuffixOutput} which delegates computation directly to its
 * {@link DeterministicSemantics semantics}.
 *
 * @param <L>
 *         location type
 * @param <I>
 *         input symbol type
 */
public interface SEVPA<L, I> extends UniversalAutomaton<L, I, L, Boolean, Void>,
                                     InitialState<L>,
                                     DeterministicSemantics,
                                     SuffixOutput<I, Boolean>,
                                     GraphViewable,
                                     InputAlphabetHolder<I> {

    @Override
    VPAlphabet<I> getInputAlphabet();

    L getModuleEntry(I callSym);

    int getNumStackSymbols();

    int encodeStackSym(L srcLoc, I callSym);

    @Nullable L getInternalSuccessor(L loc, I intSym);

    @Nullable L getReturnSuccessor(L loc, I retSym, int stackSym);

    @Override // do not allow a nullable initial state
    L getInitialState();

    @Override
    default Void getTransitionProperty(L transition) {
        return null;
    }

    @Override
    default Collection<L> getTransitions(L state, I input) {
        final VPAlphabet<I> alphabet = getInputAlphabet();
        return switch (alphabet.getSymbolType(input)) {
            case CALL:
                yield Collections.singleton(getModuleEntry(input));
            case INTERNAL:
                final L iSucc = getInternalSuccessor(state, input);
                yield iSucc == null ? Collections.emptyList() : Collections.singleton(iSucc);
            case RETURN:
                final int symbols = getNumStackSymbols();
                final List<L> result = new ArrayList<>(symbols);
                for (int i = 0; i < symbols; i++) {
                    final L rSucc = getReturnSuccessor(state, input, i);
                    if (rSucc != null) {
                        result.add(rSucc);
                    }
                }
                yield result;
        };
    }

    @Override
    default L getSuccessor(L transition) {
        return transition;
    }

    @Override
    default DeterministicAcceptorTS<State<L>, I> getSemantics() {
        return new SEVPASemantics<>(this);
    }

    @Override
    default Boolean computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        return getSemantics().computeSuffixOutput(prefix, suffix);
    }

    @Override
    default Graph<L, SevpaViewEdge<L, I>> graphView() {
        return new SEVPAGraphView<>(this);
    }
}
