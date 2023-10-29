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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.Maps;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.vpa.DefaultNSEVPA;
import net.automatalib.automaton.vpa.Location;
import net.automatalib.automaton.vpa.SEVPA;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.copy.AutomatonCopyMethod;
import net.automatalib.util.automaton.copy.AutomatonLowLevelCopy;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

final class NSEVPAConverter {

    private NSEVPAConverter() {
        // prevent instantiation
    }

    public static <I> SEVPA<?, I> convert(SPA<?, I> spa) {
        final Map<@Nullable I, List<Pair<Word<I>, Word<I>>>> contextPairs = computeContextPairs(spa);
        return constructNSEVPA(spa, contextPairs);
    }

    private static <I> Map<@Nullable I, List<Pair<Word<I>, Word<I>>>> computeContextPairs(SPA<?, I> spa) {

        final ATRSequences<I> atrSequences = SPAs.computeATRSequences(spa);
        final ProceduralInputAlphabet<I> inputAlphabet = spa.getInputAlphabet();
        final Map<@Nullable I, List<Pair<Word<I>, Word<I>>>> contextPairs =
                Maps.newHashMapWithExpectedSize(inputAlphabet.getNumCalls() + 1);

        contextPairs.put(null, Collections.singletonList(Pair.of(Word.epsilon(), Word.epsilon())));

        // add remaining pairs
        for (Entry<I, DFA<?, I>> e : spa.getProcedures().entrySet()) {
            contextPairs.put(e.getKey(), computeContextPair(e.getKey(), e.getValue(), inputAlphabet, atrSequences));
        }

        return contextPairs;
    }

    private static <I> List<Pair<Word<I>, Word<I>>> computeContextPair(I procedure,
                                                                       DFA<?, I> dfa,
                                                                       ProceduralInputAlphabet<I> alphabet,
                                                                       ATRSequences<I> atr) {

        final Alphabet<I> pAlphabet = alphabet.getProceduralAlphabet();
        final DFA<?, I> actualDFA;

        // TODO: Maybe replace with a thin wrapper instead of copying
        if (hasSink(dfa, pAlphabet)) {
            actualDFA = dfa;
        } else {
            final CompactDFA<I> compactDFA = new CompactDFA<>(pAlphabet);
            AutomatonLowLevelCopy.copy(AutomatonCopyMethod.BFS, dfa, pAlphabet, compactDFA);
            final Integer sink = compactDFA.addState(false);
            for (I i : pAlphabet) {
                compactDFA.setTransition(sink, i, sink);
            }
            actualDFA = compactDFA;
        }

        final List<Word<I>> cs = Automata.characterizingSet(actualDFA, pAlphabet);
        final List<Pair<Word<I>, Word<I>>> result = new ArrayList<>(cs.size());

        final Word<I> as = atr.accessSequences.get(procedure);
        final Word<I> rs = atr.returnSequences.get(procedure);

        for (Word<I> w : cs) {
            result.add(Pair.of(as, alphabet.expand(w, atr.terminatingSequences::get).concat(rs)));
        }

        return result;
    }

    private static <I> SEVPA<?, I> constructNSEVPA(SPA<?, I> spa,
                                                   Map<@Nullable I, List<Pair<Word<I>, Word<I>>>> contextPairs) {

        final ProceduralInputAlphabet<I> alphabet = spa.getInputAlphabet();
        final Map<Word<I>, Location> mainLocations = new HashMap<>();
        final Map<BitSet, Location> mainSignatures = new HashMap<>();

        final DefaultNSEVPA<I> vpa = new DefaultNSEVPA<>(alphabet);
        final Location init = vpa.addInitialLocation(false);

        mainLocations.put(Word.epsilon(), init);
        mainSignatures.put(OneSEVPAConverter.computeSignature(spa, contextPairs.get(null), Word.epsilon()), init);

        final List<ModuleContext<I>> locationContexts = new ArrayList<>();
        locationContexts.add(new ModuleContext<>(null, Word.epsilon(), mainLocations, mainSignatures));

        for (I i : alphabet.getCallAlphabet()) {
            final Map<Word<I>, Location> moduleLocation = new HashMap<>();
            final Map<BitSet, Location> moduleSignature = new HashMap<>();

            final Location entry = vpa.addModuleEntryLocation(i, false);
            moduleLocation.put(Word.epsilon(), entry);
            moduleSignature.put(OneSEVPAConverter.computeSignature(spa, contextPairs.get(i), Word.epsilon()), entry);

            locationContexts.add(new ModuleContext<>(i, Word.epsilon(), moduleLocation, moduleSignature));
        }

        int queuePtr = 0;
        while (queuePtr < locationContexts.size()) {

            final ModuleContext<I> context = locationContexts.get(queuePtr++);
            final I procedure = context.procedure;
            final Word<I> sp = context.sp;
            final Map<Word<I>, Location> locationMap = context.locationMap;
            final Map<BitSet, Location> signatureMap = context.signatureMap;

            final Location loc = locationMap.get(sp);

            for (I i : alphabet.getInternalAlphabet()) {
                final Word<I> lp = sp.append(i);
                final BitSet sig = OneSEVPAConverter.computeSignature(spa, contextPairs.get(procedure), lp);

                Location succ = signatureMap.get(sig);
                if (succ == null) {
                    final Location newLoc;
                    if (procedure == null) {
                        newLoc = vpa.addLocation(sig.get(0));
                    } else {
                        newLoc = vpa.addLocation(procedure, false);
                    }
                    locationMap.put(lp, newLoc);
                    signatureMap.put(sig, newLoc);
                    locationContexts.add(new ModuleContext<>(procedure, lp, locationMap, signatureMap));

                    succ = newLoc;
                }
                vpa.setInternalSuccessor(loc, i, succ);
            }

            for (I i : alphabet.getCallAlphabet()) {
                final int stackSym = vpa.encodeStackSym(loc, i);

                for (int j = 0; j < queuePtr; j++) {
                    final ModuleContext<I> otherContext = locationContexts.get(j);
                    final I otherProcedure = otherContext.procedure;
                    final Word<I> otherSp = otherContext.sp;
                    final Map<Word<I>, Location> otherLocationMap = otherContext.locationMap;
                    final Map<BitSet, Location> otherSignatureMap = otherContext.signatureMap;

                    final Location otherLoc = otherLocationMap.get(otherSp);

                    // returning from sp to otherSP
                    final Word<I> lp1 = otherSp.append(i).concat(sp).append(alphabet.getReturnSymbol());
                    final BitSet sig1 = OneSEVPAConverter.computeSignature(spa, contextPairs.get(otherProcedure), lp1);

                    Location succ1 = otherSignatureMap.get(sig1);
                    if (succ1 == null) {
                        final Location newLoc;
                        if (otherProcedure == null) {
                            newLoc = vpa.addLocation(sig1.get(0));
                        } else {
                            newLoc = vpa.addLocation(otherProcedure, false);
                        }
                        otherLocationMap.put(lp1, newLoc);
                        otherSignatureMap.put(sig1, newLoc);
                        locationContexts.add(new ModuleContext<>(otherProcedure,
                                                                 lp1,
                                                                 otherLocationMap,
                                                                 otherSignatureMap));

                        succ1 = newLoc;
                    }
                    vpa.setReturnSuccessor(loc, alphabet.getReturnSymbol(), vpa.encodeStackSym(otherLoc, i), succ1);

                    // returning from otherSp to sp
                    final Word<I> lp2 = sp.append(i).concat(otherSp).append(alphabet.getReturnSymbol());
                    final BitSet sig2 = OneSEVPAConverter.computeSignature(spa, contextPairs.get(procedure), lp2);

                    Location succ2 = signatureMap.get(sig2);
                    if (succ2 == null) {
                        final Location newLoc;
                        if (procedure == null) {
                            newLoc = vpa.addLocation(sig2.get(0));
                        } else {
                            newLoc = vpa.addLocation(procedure, false);
                        }
                        locationMap.put(lp2, newLoc);
                        signatureMap.put(sig2, newLoc);
                        locationContexts.add(new ModuleContext<>(procedure, lp2, locationMap, signatureMap));

                        succ2 = newLoc;
                    }
                    vpa.setReturnSuccessor(otherLoc, alphabet.getReturnSymbol(), stackSym, succ2);
                }
            }
        }

        return vpa;
    }

    private static <S, I> boolean hasSink(DFA<S, I> dfa, Alphabet<I> alphabet) {
        for (S s : dfa) {
            if (isSink(dfa, alphabet, s)) {
                return true;
            }
        }

        return false;
    }

    private static <S, I> boolean isSink(DFA<S, I> dfa, Collection<? extends I> inputs, S state) {

        if (dfa.isAccepting(state)) {
            return false;
        }

        for (I i : inputs) {
            final S succ = dfa.getSuccessor(state, i);
            if (!Objects.equals(succ, state)) {
                return false;
            }
        }

        return true;
    }

    private static class ModuleContext<I> {

        final @Nullable I procedure;
        final Word<I> sp;
        final Map<Word<I>, Location> locationMap;
        final Map<BitSet, Location> signatureMap;

        ModuleContext(@Nullable I procedure, Word<I> sp, Map<Word<I>, Location> locationMap, Map<BitSet, Location> signatureMap) {
            this.procedure = procedure;
            this.sp = sp;
            this.locationMap = locationMap;
            this.signatureMap = signatureMap;
        }
    }
}
