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
package net.automatalib.incremental.dfa;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.concept.InputAlphabetHolder;

/**
 * Abstract base class for {@link IncrementalDFABuilder}s. This class takes care of holding the input alphabet and its
 * size.
 *
 * @param <I>
 *         input symbol class
 */
public abstract class AbstractIncrementalDFABuilder<I> implements IncrementalDFABuilder<I>, InputAlphabetHolder<I> {

    protected final Alphabet<I> inputAlphabet;
    protected int alphabetSize;

    /**
     * Constructor.
     *
     * @param inputAlphabet
     *         the input alphabet
     */
    public AbstractIncrementalDFABuilder(Alphabet<I> inputAlphabet) {
        this.inputAlphabet = inputAlphabet;
        this.alphabetSize = inputAlphabet.size();
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        if (!this.inputAlphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.inputAlphabet).addSymbol(symbol);
        }

        this.alphabetSize = this.inputAlphabet.size();
    }

}
