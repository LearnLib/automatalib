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
package net.automatalib.util.automaton.procedural;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.vpa.DefaultOneSEVPA;
import net.automatalib.automaton.vpa.Location;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

final class OneSEVPAConverter {

    private OneSEVPAConverter() {
        // prevent instantiation
    }

    public static <I> OneSEVPA<?, I> convert(SPA<?, I> spa) {
        final List<Pair<Word<I>, Word<I>>> contextPairs = computeContextPairs(spa);
        return constructOneSEVPA(spa, contextPairs);
    }

    private static <I> List<Pair<Word<I>, Word<I>>> computeContextPairs(SPA<?, I> spa) {

        final ATRSequences<I> atrSequences = SPAs.computeATRSequences(spa);
        final ProceduralInputAlphabet<I> inputAlphabet = spa.getInputAlphabet();
        final List<Pair<Word<I>, Word<I>>> contextPairs = new ArrayList<>();

        contextPairs.add(Pair.of(Word.epsilon(), Word.epsilon()));

        // add remaining pairs
        for (Entry<I, DFA<?, I>> e : spa.getProcedures().entrySet()) {
            contextPairs.addAll(computeContextPair(e.getKey(), e.getValue(), inputAlphabet, atrSequences));
        }

        return contextPairs;
    }

    private static <I> List<Pair<Word<I>, Word<I>>> computeContextPair(I procedure,
                                                                       DFA<?, I> dfa,
                                                                       ProceduralInputAlphabet<I> alphabet,
                                                                       ATRSequences<I> atr) {

        final List<Word<I>> cs = Automata.characterizingSet(dfa, alphabet.getProceduralAlphabet());
        final List<Pair<Word<I>, Word<I>>> result = new ArrayList<>(cs.size());

        final Word<I> as = atr.accessSequences.get(procedure);
        final Word<I> rs = atr.returnSequences.get(procedure);

        for (Word<I> w : cs) {
            result.add(Pair.of(as, alphabet.expand(w, atr.terminatingSequences::get).concat(rs)));
        }

        return result;
    }

    private static <I> OneSEVPA<?, I> constructOneSEVPA(SPA<?, I> spa, List<Pair<Word<I>, Word<I>>> contextPairs) {

        final ProceduralInputAlphabet<I> alphabet = spa.getInputAlphabet();
        final Map<Word<I>, Location> locationMap = new HashMap<>();
        final Map<BitSet, Location> signatureMap = new HashMap<>();

        final DefaultOneSEVPA<I> vpa = new DefaultOneSEVPA<>(alphabet);
        final Location init = vpa.addInitialLocation(false);

        locationMap.put(Word.epsilon(), init);
        signatureMap.put(computeSignature(spa, contextPairs, Word.epsilon()), init);

        final List<Word<I>> shortPrefixes = new ArrayList<>();
        shortPrefixes.add(Word.epsilon());

        int queuePtr = 0;
        while (queuePtr < shortPrefixes.size()) {

            final Word<I> sp = shortPrefixes.get(queuePtr++);
            final Location loc = locationMap.get(sp);

            for (I i : alphabet.getInternalAlphabet()) {
                final Word<I> lp = sp.append(i);
                final BitSet sig = computeSignature(spa, contextPairs, lp);

                Location succ = signatureMap.get(sig);
                if (succ == null) {
                    Location newLoc = vpa.addLocation(sig.get(0));
                    locationMap.put(lp, newLoc);
                    signatureMap.put(sig, newLoc);
                    shortPrefixes.add(lp);

                    succ = newLoc;
                }
                vpa.setInternalSuccessor(loc, i, succ);
            }

            for (I i : alphabet.getCallAlphabet()) {
                final int stackSym = vpa.encodeStackSym(loc, i);

                for (int j = 0; j < queuePtr; j++) {
                    final Word<I> otherSp = shortPrefixes.get(j);
                    final Location otherLoc = locationMap.get(otherSp);

                    // returning from sp to otherSP
                    final Word<I> lp1 = otherSp.append(i).concat(sp).append(alphabet.getReturnSymbol());
                    final BitSet sig1 = computeSignature(spa, contextPairs, lp1);

                    Location succ1 = signatureMap.get(sig1);
                    if (succ1 == null) {
                        Location newLoc = vpa.addLocation(sig1.get(0));
                        locationMap.put(lp1, newLoc);
                        signatureMap.put(sig1, newLoc);
                        shortPrefixes.add(lp1);

                        succ1 = newLoc;
                    }
                    vpa.setReturnSuccessor(loc, alphabet.getReturnSymbol(), vpa.encodeStackSym(otherLoc, i), succ1);

                    // returning from otherSp to sp
                    final Word<I> lp2 = sp.append(i).concat(otherSp).append(alphabet.getReturnSymbol());
                    final BitSet sig2 = computeSignature(spa, contextPairs, lp2);

                    Location succ2 = signatureMap.get(sig2);
                    if (succ2 == null) {
                        Location newLoc = vpa.addLocation(sig2.get(0));
                        locationMap.put(lp2, newLoc);
                        signatureMap.put(sig2, newLoc);
                        shortPrefixes.add(lp2);

                        succ2 = newLoc;
                    }
                    vpa.setReturnSuccessor(otherLoc, alphabet.getReturnSymbol(), stackSym, succ2);

                }
            }
        }

        return vpa;
    }

    static <I> BitSet computeSignature(SPA<?, I> spa,
                                       List<Pair<Word<I>, Word<I>>> contextPairs,
                                       Word<I> accessSequences) {

        final BitSet sig = new BitSet(contextPairs.size());

        for (int i = 0; i < contextPairs.size(); i++) {
            final Pair<Word<I>, Word<I>> cp = contextPairs.get(i);
            sig.set(i, accepts(spa, cp.getFirst(), accessSequences, cp.getSecond()));
        }

        return sig;
    }

    private static <S, I> boolean accepts(SPA<S, I> spa, Word<I> w1, Word<I> w2, Word<I> w3) {
        S iter = spa.getInitialState();

        for (int i = 0; i < w1.length(); i++) {
            iter = spa.getTransition(iter, w1.getSymbol(i));
        }

        for (int i = 0; i < w2.length(); i++) {
            iter = spa.getTransition(iter, w2.getSymbol(i));
        }

        for (int i = 0; i < w3.length(); i++) {
            iter = spa.getTransition(iter, w3.getSymbol(i));
        }

        return spa.isAccepting(iter);
    }
}
