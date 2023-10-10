/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automaton.procedural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import net.automatalib.word.ProceduralInputAlphabet;

/**
 * A system of procedural automata. An {@link SPA} is a context-free system where each non-terminal (procedure) is
 * represented by a {@link DFA} that accepts the language of right-hand sides of its respective production rules.
 * <p>
 * For example, take the following context-free palindrome system over {@code a,b,c} using two non-terminals
 * {@code F,G}:
 * <pre>
 *     F -&gt; a | a F a | b | b F b | G | ε
 *     G -&gt; c | c G c | F
 * </pre>
 * The corresponding {@link SPA} would consist of {@link DFA procedures} (for {@code F} and {@code G}), accepting the
 * regular languages {@code {a,aFa,b,bFb,G,ε}} and {@code {c,cGc,F}} respectively.
 * <p>
 * In {@link SPA}s, calls to and returns from procedures are visible. For the above example, a possible word accepted by
 * the respective {@link SPA} (when using {@code F} as {@link #getInitialProcedure() initial procedure}) would be
 * {@code FaFGcRRaR} (where {@code R} denotes the designated
 * {@link ProceduralInputAlphabet#getReturnSymbol() return symbol}.
 * <p>
 * For further information, see "<a href="https://doi.org/10.1007/s10009-021-00634-y">Compositional learning of mutually
 * recursive procedural systems</a>".
 * <p>
 * This interface makes no assumptions about how the semantics are implemented. One may use a stack-based approach,
 * graph expansion, or else. However, {@link SPA}s should be <i>consistent</i> with their alphabet definitions, i.e. an
 * {@link SPA} should be able to {@link #accepts(Iterable) parse} words over the
 * {@link #getInputAlphabet() specified alphabet} and each {@link #getProcedures() procedure} should be able to
 * {@link DFA#accepts(Iterable) parse} words over the {@link #getProceduralInputs() procedural inputs}.
 *
 * @param <S>
 *         state type
 * @param <I>
 *         input symbol type
 */
public interface SPA<S, I>
        extends ProceduralSystem<I, DFA<?, I>>, DeterministicAcceptorTS<S, I>, SuffixOutput<I, Boolean> {

    @Override
    default Collection<I> getProceduralInputs(Collection<I> constraints) {
        final ProceduralInputAlphabet<I> alphabet = getInputAlphabet();
        final Map<I, DFA<?, I>> procedures = getProcedures();

        final List<I> result = new ArrayList<>(Math.min(alphabet.size() - 1, constraints.size()));

        for (I i : constraints) {
            if (procedures.containsKey(i) || alphabet.isInternalSymbol(i)) {
                result.add(i);
            }
        }

        return result;
    }

    @Override
    default Boolean computeOutput(Iterable<? extends I> input) {
        return this.accepts(input);
    }

}
