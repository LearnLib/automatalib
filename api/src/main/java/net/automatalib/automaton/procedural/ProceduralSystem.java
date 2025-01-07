/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.automaton.procedural;

import java.util.Collection;
import java.util.Map;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.InputAlphabetHolder;
import net.automatalib.automaton.simple.SimpleAutomaton;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.ts.simple.SimpleTS;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * (Meta-) Interface for procedural systems, i.e., systems that consist of multiple procedural automata that can
 * mutually call each other.
 *
 * @param <I>
 *         input symbol type
 * @param <M>
 *         procedural model type
 */
interface ProceduralSystem<I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>>
        extends FiniteRepresentation, GraphViewable, InputAlphabetHolder<I> {

    /**
     * Refinement of {@link InputAlphabetHolder#getInputAlphabet()} to add the constraint that {@code this} system
     * operates on {@link ProceduralInputAlphabet}s.
     *
     * @return the input alphabet
     */
    @Override
    ProceduralInputAlphabet<I> getInputAlphabet();

    /**
     * Convenience method for {@link #getProceduralInputs(Collection)} which uses the
     * {@link #getInputAlphabet() input alphabet} of {@code this} system as {@code constraints}.
     *
     * @return a collection of defined inputs for {@code this} system's procedures.
     */
    default Collection<I> getProceduralInputs() {
        return getProceduralInputs(this.getInputAlphabet());
    }

    /**
     * Returns a collection of input symbols which the procedural automata can process. Depending on {@code this}
     * system's semantics, this can exclude certain symbols such as the
     * {@link ProceduralInputAlphabet#getReturnSymbol() return symbol} or
     * {@link ProceduralInputAlphabet#getCallAlphabet() call symbols} for which no procedure exists.
     * <p>
     * This collection can be further constrained via the {@code constraints} parameter which is used in a final
     * intersection operation with the previous collection.
     *
     * @param constraints
     *         a {@link Collection} for additionally constraining the returned procedural inputs.
     *
     * @return the (constrained) procedural inputs
     */
    Collection<I> getProceduralInputs(Collection<I> constraints);

    /**
     * Returns the initial procedure (represented via its {@link ProceduralInputAlphabet#getCallAlphabet() call symbol})
     * of this system.
     *
     * @return the initial procedure, may be {@code null} if undefined
     */
    @Nullable I getInitialProcedure();

    /**
     * Returns a {@link Map} from {@link ProceduralInputAlphabet#getCallAlphabet() call symbols} to the procedures of
     * {@code this} system. Note that a (non-minimal) {@link ProceduralSystem} may not contain a procedure for every
     * call symbol.
     *
     * @return the procedures of this system
     */
    Map<I, M> getProcedures();

    /**
     * Convenience method for {@link #getProcedures()} to quickly return the procedure of a given call symbol.
     *
     * @param callSymbol
     *         the call symbol
     *
     * @return the corresponding procedure. May be {@code null} if {@code this} system does not have a procedure for the
     * given call symbol.
     *
     * @see #getProcedures()
     */
    default @Nullable M getProcedure(I callSymbol) {
        assert getInputAlphabet().isCallSymbol(callSymbol);
        return getProcedures().get(callSymbol);
    }

    /**
     * Returns the size of {@code this} system which is given by the sum of the sizes of all
     * {@link #getProcedures() procedures}. Note that this value does not necessarily correspond to the classical notion
     * of {@link SimpleAutomaton#size()}, since semantically a {@link ProceduralSystem} may be infinite-sized
     * {@link SimpleTS}.
     *
     * @return the size of {@code this} system
     */
    @Override
    default int size() {
        int size = 0;

        for (M p : getProcedures().values()) {
            size += p.size();
        }

        return size;
    }

    @Override
    default Graph<?, ?> graphView() {
        final ProceduralInputAlphabet<I> alphabet = this.getInputAlphabet();
        return new ProceduralGraphView<>(alphabet.getInternalAlphabet(),
                                         this.getProceduralInputs(alphabet),
                                         this.getProcedures());
    }
}
