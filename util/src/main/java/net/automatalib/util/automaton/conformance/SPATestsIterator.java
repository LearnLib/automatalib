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
package net.automatalib.util.automaton.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.common.util.collection.AbstractTwoLevelIterator;
import net.automatalib.util.automaton.procedural.ATRSequences;
import net.automatalib.util.automaton.procedural.SPAUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A conformance test iterator for {@link SPA}s that applies a given regular conformance test to each procedure of the
 * {@link SPA}.
 *
 * @param <I>
 *         input symbol type
 */
public class SPATestsIterator<I> extends AbstractTwoLevelIterator<I, Word<I>, Word<I>> {

    private final SPA<?, I> spa;
    private final ProceduralInputAlphabet<I> alphabet;
    private final BiFunction<DFA<?, I>, Collection<I>, Iterator<Word<I>>> conformanceTestProvider;

    private final Collection<I> proceduralInputs;
    private final ATRSequences<I> atrSequences;

    public SPATestsIterator(SPA<?, I> spa,
                            BiFunction<DFA<?, I>, Collection<I>, Iterator<Word<I>>> conformanceTestProvider) {
        this(spa, spa.getInputAlphabet(), conformanceTestProvider);
    }

    public SPATestsIterator(SPA<?, I> spa,
                            ProceduralInputAlphabet<I> alphabet,
                            BiFunction<DFA<?, I>, Collection<I>, Iterator<Word<I>>> conformanceTestProvider) {
        super(availableProceduresIterator(spa, alphabet));

        this.spa = spa;
        this.alphabet = alphabet;
        this.conformanceTestProvider = conformanceTestProvider;
        this.proceduralInputs = spa.getProceduralInputs(alphabet);
        this.atrSequences = SPAUtil.computeATRSequences(spa, alphabet);
    }

    @Override
    protected Iterator<Word<I>> l2Iterator(I callSymbol) {
        @SuppressWarnings("assignment.type.incompatible") // we only iterate over existing procedures
        final @NonNull DFA<?, I> dfa = spa.getProcedure(callSymbol);
        return this.conformanceTestProvider.apply(dfa, this.proceduralInputs);
    }

    @Override
    protected Word<I> combine(I callSymbol, Word<I> testSequence) {
        @SuppressWarnings("assignment.type.incompatible") // we check minimality in the constructor
        final @NonNull Word<I> as = this.atrSequences.accessSequences.get(callSymbol);
        // we check minimality in the constructor
        @SuppressWarnings({"assignment.type.incompatible", "methodref.return.invalid"})
        final Word<I> ts = this.alphabet.expand(testSequence, this.atrSequences.terminatingSequences::get);
        @SuppressWarnings("assignment.type.incompatible") // we check minimality in the constructor
        final @NonNull Word<I> rs = this.atrSequences.returnSequences.get(callSymbol);

        return Word.fromWords(as, ts, rs);
    }

    private static <I> Iterator<I> availableProceduresIterator(SPA<?, I> spa, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, DFA<?, I>> procedures = spa.getProcedures();
        final List<I> result = new ArrayList<>(procedures.size());

        for (I i : alphabet.getCallAlphabet()) {
            if (procedures.containsKey(i)) {
                result.add(i);
            }
        }

        return result.iterator();
    }
}
