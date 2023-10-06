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
package net.automatalib.util.automata.conformance;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.procedural.SBA;
import net.automatalib.util.automata.procedural.SBAUtil;
import net.automatalib.words.ProceduralInputAlphabet;

/**
 * A conformance test iterator for {@link SBA}s that is based on the {@link WMethodTestsIterator W-method}. Note that
 * this implementation takes care of the special {@link SBA} semantics revolving around return transitions or
 * non-terminating procedures and only expands procedural test-cases that can be properly evaluated on the global
 * {@link SBA}.
 *
 * @param <I>
 *         input symbol type
 *
 * @see WMethodTestsIterator
 */
public class SBAWMethodTestsIterator<I> extends ProceduralWMethodTestsIterator<I, DFA<?, I>> {

    public SBAWMethodTestsIterator(SBA<?, I> sba) {
        this(sba, sba.getInputAlphabet());
    }

    public SBAWMethodTestsIterator(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {
        this(sba, alphabet, 0);
    }

    public SBAWMethodTestsIterator(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet, int maxDepth) {
        super(alphabet,
              sba.getProceduralInputs(alphabet),
              sba.getProcedures(),
              SBAUtil.computeATSequences(sba, alphabet),
              maxDepth);
    }

}
