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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.util.collections.AbstractThreeLevelIterator;
import net.automatalib.commons.util.collections.AbstractTwoLevelIterator;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.collections.ReusableIterator;
import net.automatalib.util.automata.cover.Covers;
import net.automatalib.util.automata.equivalence.CharacterizingSets;
import net.automatalib.util.automata.procedural.ATSequences;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

class ProceduralWMethodTestsIterator<I, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>>
        extends AbstractTwoLevelIterator<I, Word<I>, Word<I>> {

    private static final List<List<Word<?>>> EPSILON =
            Collections.singletonList(Collections.singletonList(Word.epsilon()));

    private final ProceduralInputAlphabet<I> alphabet;
    private final Collection<I> proceduralInputs;
    private final Map<I, M> procedures;
    private final ATSequences<I> atSequences;
    private final int maxDepth;

    private final List<I> continuableSymbols;
    private final List<Word<I>> continuableWords;
    private final List<Word<I>> nonContinuableWords;

    ProceduralWMethodTestsIterator(ProceduralInputAlphabet<I> alphabet,
                                   Collection<I> proceduralInputs,
                                   Map<I, M> procedures,
                                   ATSequences<I> atSequences,
                                   int maxDepth) {
        super(atSequences.accessSequences.keySet().iterator());

        this.alphabet = alphabet;
        this.proceduralInputs = proceduralInputs;
        this.procedures = procedures;
        this.atSequences = atSequences;
        this.maxDepth = maxDepth;

        this.continuableSymbols = new ArrayList<>(alphabet.size() - 1);
        this.continuableWords = new ArrayList<>(alphabet.size());
        this.nonContinuableWords = new ArrayList<>(alphabet.getNumCalls() + 1);

        // internal symbols
        this.continuableSymbols.addAll(alphabet.getInternalAlphabet());
        for (I i : alphabet.getInternalAlphabet()) {
            this.continuableWords.add(Word.fromLetter(i));
        }
        this.continuableWords.add(Word.epsilon());

        // call symbols
        for (I i : alphabet.getCallAlphabet()) {
            if (this.atSequences.terminatingSequences.containsKey(i)) {
                this.continuableSymbols.add(i);
                this.continuableWords.add(Word.fromLetter(i));
            } else {
                this.nonContinuableWords.add(Word.fromLetter(i));
            }
        }

        // return symbols
        this.nonContinuableWords.add(Word.fromLetter(alphabet.getReturnSymbol()));
    }

    @Override
    protected Iterator<Word<I>> l2Iterator(I callSymbol) {
        @SuppressWarnings("assignment.type.incompatible") // we only iterate over existing procedures
        final @NonNull M p = procedures.get(callSymbol);
        return proceduralTestWords(p);
    }

    @Override
    protected Word<I> combine(I callSymbol, Word<I> testSequence) {
        @SuppressWarnings("assignment.type.incompatible") // we only iterate over accessible procedures
        final @NonNull Word<I> as = this.atSequences.accessSequences.get(callSymbol);

        if (testSequence.isEmpty()) {
            return as;
        }

        @SuppressWarnings("methodref.return.invalid") // we only iterate over accessible procedures
        final Word<I> exp = this.alphabet.expand(testSequence.prefix(-1), this.atSequences.terminatingSequences::get);

        return Word.fromWords(as, exp, Word.fromLetter(testSequence.lastSymbol()));
    }

    private Iterator<Word<I>> proceduralTestWords(M automaton) {

        final Iterable<Word<I>> sCov =
                new ReusableIterator<>(Covers.stateCoverIterator(automaton, this.continuableSymbols));
        final Iterator<Word<I>> cSet = CharacterizingSets.characterizingSetIterator(automaton, this.proceduralInputs);

        final Iterator<Word<I>> continuableIter = new ContinuableIterator(sCov, cSet);
        final Iterator<Word<I>> nonContinuableIter = new NonContinuableIterator(sCov);

        // we need sCov in both iterators but cSet only in the continuable one.
        // therefore, start with the non-continuable one in hopes of saving unnecessary computations
        return Iterators.concat(nonContinuableIter, continuableIter);
    }

    private class ContinuableIterator extends AbstractThreeLevelIterator<Word<I>, List<Word<I>>, List<I>, Word<I>> {

        private final Iterable<Word<I>> sCov;

        ContinuableIterator(Iterable<Word<I>> sCov, Iterator<Word<I>> cSet) {
            super(cSet);
            this.sCov = sCov;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Iterator<List<Word<I>>> l2Iterator(Word<I> cSet) {
            final Iterator<List<Word<I>>> epsilon = ((List<List<Word<I>>>) (List<?>) EPSILON).iterator();
            return Iterators.concat(epsilon, CollectionsUtil.cartesianProduct(sCov, continuableWords).iterator());
        }

        @Override
        protected Iterator<List<I>> l3Iterator(Word<I> cSet, List<Word<I>> tCov) {
            return CollectionsUtil.allTuples(continuableSymbols, 0, maxDepth).iterator();
        }

        @Override
        protected Word<I> combine(Word<I> cSet, List<Word<I>> tCov, List<I> tuples) {
            final WordBuilder<I> wb = new WordBuilder<>();

            for (Word<I> w : tCov) {
                wb.append(w);
            }

            wb.append(tuples);
            wb.append(cSet);
            return wb.toWord();
        }
    }

    private class NonContinuableIterator extends AbstractTwoLevelIterator<Word<I>, Word<I>, Word<I>> {

        NonContinuableIterator(Iterable<Word<I>> sCov) {
            super(sCov.iterator());
        }

        @Override
        protected Iterator<Word<I>> l2Iterator(Word<I> sCov) {
            return nonContinuableWords.iterator();
        }

        @Override
        protected Word<I> combine(Word<I> sCov, Word<I> nonCon) {
            return sCov.concat(nonCon);
        }
    }
}
