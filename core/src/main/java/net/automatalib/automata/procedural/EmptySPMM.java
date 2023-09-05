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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Iterables;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A utility implementation of an {@link SPMM} that transduces all input words to a sequence of erroneous output
 * symbols.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class EmptySPMM<I, O> implements SPMM<Void, I, Void, O> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final ProceduralOutputAlphabet<O> outputAlphabet;

    public EmptySPMM(ProceduralInputAlphabet<I> alphabet, ProceduralOutputAlphabet<O> outputAlphabet) {
        this.alphabet = alphabet;
        this.outputAlphabet = outputAlphabet;
    }

    @Override
    public @Nullable I getInitialProcedure() {
        return null;
    }

    @Override
    public ProceduralInputAlphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public ProceduralOutputAlphabet<O> getOutputAlphabet() {
        return this.outputAlphabet;
    }

    @Override
    public Map<I, MealyMachine<?, I, ?, O>> getProcedures() {
        return Collections.emptyMap();
    }

    @Override
    public Word<O> computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
        final int length = Iterables.size(suffix);
        return Word.fromList(Collections.nCopies(length, outputAlphabet.getErrorSymbol()));
    }

    @Override
    public Void getTransition(Void state, I input) {
        return null;
    }

    @Override
    public @Nullable Void getInitialState() {
        return null;
    }

    @Override
    public O getTransitionOutput(Void transition) {
        return outputAlphabet.getErrorSymbol();
    }

    @Override
    public Void getSuccessor(Void transition) {
        return null;
    }
}
