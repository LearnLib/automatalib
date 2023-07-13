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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Iterables;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author frohme
 */
public class EmptySPMM<I, O> implements SPMM<Void, I, Void, O> {

    private final SPAAlphabet<I> alphabet;
    private final SPAOutputAlphabet<O> outputAlphabet;

    public EmptySPMM(SPAAlphabet<I> alphabet, SPAOutputAlphabet<O> outputAlphabet) {
        this.alphabet = alphabet;
        this.outputAlphabet = outputAlphabet;
    }

    @Override
    public @Nullable I getInitialProcedure() {
        return null;
    }

    @Override
    public SPAAlphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public SPAOutputAlphabet<O> getOutputAlphabet() {
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

    @Nullable
    @Override
    public Void getInitialState() {
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
