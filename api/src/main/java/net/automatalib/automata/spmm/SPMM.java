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
package net.automatalib.automata.spmm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.automata.concepts.FiniteRepresentation;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.ts.output.MealyTransitionSystem;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author frohme
 */
public interface SPMM<S, I, T, O> extends MealyTransitionSystem<S, I, T, O>,
                                          SuffixOutput<I, Word<O>>,
                                          InputAlphabetHolder<I>,
                                          GraphViewable,
                                          FiniteRepresentation {

    @Override
    SPAAlphabet<I> getInputAlphabet();

    SPAOutputAlphabet<O> getOutputAlphabet();

    @Nullable I getInitialProcedure();

    Map<I, MealyMachine<?, I, ?, O>> getProcedures();

    default Collection<I> getProceduralInputs() {
        return getProceduralInputs(this.getInputAlphabet());
    }

    default Collection<I> getProceduralInputs(SPAAlphabet<I> constraints) {
        final List<I> symbols = new ArrayList<>(constraints.size());

        for (I i : getInputAlphabet()) {
            if (constraints.isInternalSymbol(i)) {
                symbols.add(i);
            }
        }

        for (I procedure : getProcedures().keySet()) {
            if (constraints.isCallSymbol(procedure)) {
                symbols.add(procedure);
            }
        }

        symbols.add(constraints.getReturnSymbol());

        return symbols;
    }

    @Override
    default int size() {
        int size = 0;

        for (MealyMachine<?, I, ?, O> m : getProcedures().values()) {
            size += m.size();
        }

        return size;
    }


    @Override
    default Word<O> computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {

        final S state = this.getState(prefix);

        final WordBuilder<O> result = Output.getBuilderFor(suffix);

        this.trace(state, suffix, result);

        return result.toWord();
    }

    @Override
    default Graph<?, ?> graphView() {
        final SPAAlphabet<I> alphabet = this.getInputAlphabet();
        return new SPMMGraphView<>(alphabet.getCallAlphabet(),
                                   this.getProceduralInputs(alphabet),
                                   this.getProcedures());
    }
}
