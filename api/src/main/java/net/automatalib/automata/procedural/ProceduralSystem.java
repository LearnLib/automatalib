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
package net.automatalib.automata.procedural;

import java.util.Collection;
import java.util.Map;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.simple.SimpleTS;
import net.automatalib.words.ProceduralInputAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

interface ProceduralSystem<I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>>
        extends FiniteRepresentation, GraphViewable, InputAlphabetHolder<I> {

    /**
     * Refinement of {@link InputAlphabetHolder#getInputAlphabet()}' to add the constraint that an this system operates
     * on {@link ProceduralInputAlphabet}s.
     *
     * @return the input alphabet
     */
    @Override
    ProceduralInputAlphabet<I> getInputAlphabet();

    /**
     * Convenience method for {@link #getProceduralInputs(Collection)} which uses the
     * {@link #getInputAlphabet() input alphabet} of {@code this} {@link SPA} as {@code constraints}.
     *
     * @return a collection of defined inputs for {@code this} {@link SPA}'s procedures.
     */
    default Collection<I> getProceduralInputs() {
        return getProceduralInputs(this.getInputAlphabet());
    }

    /**
     * Returns a collection of input symbols which the procedural automata can process. The collection is computed by
     * the union of this {@link SPA}s {@link ProceduralInputAlphabet#getInternalAlphabet() internal symbols} and the
     * {@link #getProcedures() available procedure keys}.
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
     * Returns the initial procedure of this {@link SBA}, i.e. the call symbol with which each accepted word has to
     * start.
     *
     * @return the initial procedure
     */
    @Nullable I getInitialProcedure();

    /**
     * In a complete {@link SBA} every {@link #getInputAlphabet() call symbol} should be mapped to a corresponding
     * procedure.
     *
     * @return the procedures of this {@link SBA}
     */
    Map<I, M> getProcedures();

    default @Nullable M getProcedure(I callSymbol) {
        assert getInputAlphabet().isCallSymbol(callSymbol);
        return getProcedures().get(callSymbol);
    }

    /**
     * Return the size of this {@link SPA} which is given by the sum of the sizes of all
     * {@link #getProcedures() procedures}. Note that this value does not necessarily correspond to the classical notion
     * of {@link SimpleAutomaton#size()}, since semantically an {@link SPA}s may be infinite-sized {@link SimpleTS}.
     *
     * @return the size of this {@link SPA}
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
        // explicit type specification is required by checker-framework
        return new ProceduralGraphView<@Nullable Object, I>(alphabet.getInternalAlphabet(),
                                                            this.getProceduralInputs(alphabet),
                                                            this.getProcedures());
    }
}
