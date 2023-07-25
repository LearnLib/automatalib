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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.procedural.SBA;
import net.automatalib.commons.util.collections.AbstractTwoLevelIterator;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.procedural.ATSequences;
import net.automatalib.util.automata.procedural.SBAUtil;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class SBAWMethodTestsIterator<I> extends AbstractTwoLevelIterator<I, Word<I>, Word<I>> {

    private final ATSequences<I> atSequences;

    private final SBA<?, I> sba;
    private final ProceduralInputAlphabet<I> alphabet;
    private final int maxDepth;

    private final List<I> continuableSymbols;
    private final List<I> nonContinuableSymbols;

    public SBAWMethodTestsIterator(SBA<?, I> sba) {
        this(sba, sba.getInputAlphabet());
    }

    public SBAWMethodTestsIterator(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet) {
        this(sba, alphabet, 0);
    }

    public SBAWMethodTestsIterator(SBA<?, I> sba, ProceduralInputAlphabet<I> alphabet, int maxDepth) {
        super(alphabet.getCallAlphabet().iterator());

        this.atSequences = SBAUtil.computeATSequences(sba, alphabet);

        this.sba = sba;
        this.alphabet = alphabet;
        this.maxDepth = maxDepth;

        this.continuableSymbols = new ArrayList<>(alphabet.size() - 1);
        this.nonContinuableSymbols = new ArrayList<>(alphabet.getNumCalls() + 1);

        this.continuableSymbols.addAll(alphabet.getInternalAlphabet());
        this.nonContinuableSymbols.add(alphabet.getReturnSymbol());

        for (I i : alphabet.getCallAlphabet()) {
            if (this.atSequences.terminatingSequences.containsKey(i)) {
                this.continuableSymbols.add(i);
            } else {
                this.nonContinuableSymbols.add(i);
            }
        }
    }

    @Override
    protected Iterator<Word<I>> l2Iterator(I callSymbol) {
        final DFA<?, I> dfa = sba.getProcedure(callSymbol);

        if (dfa == null) {
            return Collections.emptyIterator();
        }

        return proceduralTestWords(dfa);
    }

    @Override
    protected Word<I> combine(I callSymbol, Word<I> testSequence) {
        @SuppressWarnings("assignment.type.incompatible") // we check validity in the constructor
        final @NonNull Word<I> as = this.atSequences.accessSequences.get(callSymbol);

        if (testSequence.isEmpty()) {
            return as;
        }

        @SuppressWarnings("assignment.type.incompatible") // we check redundancy-free-ness in the constructor
        final Word<I> ts = this.alphabet.expand(testSequence.prefix(-1), this.atSequences.terminatingSequences::get);

        return Word.fromWords(as, ts, Word.fromLetter(testSequence.lastSymbol()));
    }

    private Iterator<Word<I>> proceduralTestWords(DFA<?, I> dfa) {

        final List<Word<I>> sCov = Automata.stateCover(dfa, this.continuableSymbols);
        final List<Word<I>> cSet = Automata.characterizingSet(dfa, this.alphabet);

        List<Word<I>> result = new ArrayList<>();
        final WordBuilder<I> wb = new WordBuilder<>();

        for (Word<I> sC : sCov) {
            for (I con : this.continuableSymbols) {
                for (List<I> tu : CollectionsUtil.allTuples(this.continuableSymbols, 0, maxDepth)) {
                    for (Word<I> c : cSet) {
                        wb.append(sC);
                        wb.append(con);
                        wb.append(tu);
                        wb.append(c);
                        result.add(wb.toWord());
                        wb.clear();
                    }
                }
            }
        }

        // special case for epsilon as transition cover set element
        for (List<I> tu : CollectionsUtil.allTuples(this.continuableSymbols, 0, maxDepth)) {
            for (Word<I> c : cSet) {
                wb.append(tu);
                wb.append(c);
                result.add(wb.toWord());
                wb.clear();
            }
        }

        for (Word<I> sC : sCov) {
            for (I con : this.nonContinuableSymbols) {
                wb.append(sC);
                wb.append(con);
                result.add(wb.toWord());
                wb.clear();
            }
        }

        return result.iterator();
    }
}
