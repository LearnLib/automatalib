/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.util.automaton.conformance;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.procedural.SPMMs;

/**
 * A conformance test iterator for {@link SPMM}s that is based on the W-method.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @see WMethodTestsIterator
 */
public class SPMMWMethodTestsIterator<I, O> extends ProceduralWMethodTestsIterator<I, MealyMachine<?, I, ?, O>> {

    public SPMMWMethodTestsIterator(SPMM<?, I, ?, O> spmm) {
        this(spmm, spmm.getInputAlphabet());
    }

    public SPMMWMethodTestsIterator(SPMM<?, I, ?, O> spmm, ProceduralInputAlphabet<I> alphabet) {
        this(spmm, alphabet, 0);
    }

    public SPMMWMethodTestsIterator(SPMM<?, I, ?, O> spmm, ProceduralInputAlphabet<I> alphabet, int maxDepth) {
        super(alphabet,
              spmm.getProceduralInputs(alphabet),
              spmm.getProcedures(),
              SPMMs.computeATSequences(spmm, alphabet),
              maxDepth);
    }

}
