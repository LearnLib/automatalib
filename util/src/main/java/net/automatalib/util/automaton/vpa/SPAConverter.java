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
package net.automatalib.util.automaton.vpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.alphabet.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.GrowingMapAlphabet;
import net.automatalib.api.alphabet.Alphabet;
import net.automatalib.api.alphabet.GrowingAlphabet;
import net.automatalib.api.alphabet.ProceduralInputAlphabet;
import net.automatalib.api.alphabet.VPAlphabet;
import net.automatalib.api.automaton.procedural.SPA;
import net.automatalib.api.automaton.vpa.OneSEVPA;
import net.automatalib.api.word.Word;
import net.automatalib.api.word.WordBuilder;
import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.procedural.StackSPA;
import net.automatalib.common.smartcollection.ArrayStorage;
import net.automatalib.common.util.Pair;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.fsa.MutableDFAs;
import net.automatalib.util.automaton.procedural.ATRSequences;
import net.automatalib.util.automaton.procedural.SPAs;

final class SPAConverter {

    private SPAConverter() {
        // prevent initialization
    }

    public static <L, AI, CI> ConversionResult<AI, CI> convert(OneSEVPA<L, AI> sevpa,
                                                               VPAlphabet<AI> alphabet,
                                                               CI mainProcedure,
                                                               SymbolMapper<AI, CI> symbolMapper,
                                                               boolean minimize) {
        if (alphabet.getNumReturns() != 1) {
            throw new IllegalArgumentException("Currently only single return symbols are supported.");
        }

        // build alphabet
        final Map<AI, Map<L, CI>> procedureMap = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Map<CI, AI> reverseMapping = new HashMap<>();
        final GrowingAlphabet<CI> callAlphabet = new GrowingMapAlphabet<>();
        final GrowingAlphabet<CI> intAlphabet = new GrowingMapAlphabet<>();

        callAlphabet.addSymbol(mainProcedure);

        for (AI ai : alphabet.getCallAlphabet()) {
            final Map<L, CI> locationMap = Maps.newHashMapWithExpectedSize(sevpa.size());
            for (L l : sevpa.getLocations()) {
                final CI cc = symbolMapper.mapCallSymbol(ai);
                locationMap.put(l, cc);
                callAlphabet.addSymbol(cc);
                reverseMapping.put(cc, ai);
            }
            procedureMap.put(ai, locationMap);
        }

        for (AI ai : alphabet.getInternalAlphabet()) {
            final CI ci = symbolMapper.mapInternalSymbol(ai);
            intAlphabet.addSymbol(ci);
            reverseMapping.put(ci, ai);
        }
        final AI aRet = alphabet.getReturnSymbol(0);
        final CI cRet = symbolMapper.mapReturnSymbol(aRet);
        reverseMapping.put(cRet, aRet);

        final ProceduralInputAlphabet<CI> spaAlphabet =
                new DefaultProceduralInputAlphabet<>(intAlphabet, callAlphabet, cRet);

        // build procedures
        final Map<CI, CompactDFA<CI>> procedures = Maps.newHashMapWithExpectedSize(callAlphabet.size() * sevpa.size());
        final Map<L, Integer> l2sMap = Maps.newHashMapWithExpectedSize(sevpa.size());
        final CompactDFA<CI> template = buildTemplate(sevpa, alphabet, spaAlphabet, symbolMapper, procedureMap, l2sMap);

        for (L l : sevpa.getLocations()) {
            final CompactDFA<CI> lCopy = new CompactDFA<>(template);
            lCopy.setAccepting(l2sMap.get(l), true);
            for (AI ai : alphabet.getCallAlphabet()) {
                final CI ci = procedureMap.get(ai).get(l);
                procedures.put(ci, lCopy);
            }
        }

        // build main procedure
        final CompactDFA<CI> mCopy = new CompactDFA<>(template);
        for (L l : sevpa.getLocations()) {
            if (sevpa.isAcceptingLocation(l)) {
                mCopy.setAccepting(l2sMap.get(l), true);
            }
        }
        procedures.put(mainProcedure, mCopy);

        // prepare DTs
        final Map<AI, Node<AI, CI>> dts = Maps.newHashMapWithExpectedSize(alphabet.getNumCalls());
        final Collection<Pair<Word<AI>, Word<AI>>> cs = OneSEVPAs.findCharacterizingSet(sevpa, alphabet);
        final ArrayStorage<Word<AI>> as = OneSEVPAs.computeAccessSequences(sevpa, alphabet);

        // build SPA
        StackSPA<?, CI> spa = new StackSPA<>(spaAlphabet, mainProcedure, procedures);

        if (minimize) {
            // minimize SPA
            final ATRSequences<CI> atr = SPAs.computeATRSequences(spa);
            final Set<CI> accessibleProcedures = atr.accessSequences.keySet();
            final DefaultProceduralInputAlphabet<CI> minimizedAlphabet = new DefaultProceduralInputAlphabet<>(
                    spaAlphabet.getInternalAlphabet(),
                    Alphabets.fromCollection(accessibleProcedures),
                    spaAlphabet.getReturnSymbol());
            final Alphabet<CI> minimizedProceduralInputAlphabet = minimizedAlphabet.getProceduralAlphabet();

            procedures.keySet().retainAll(accessibleProcedures);

            for (Entry<CI, CompactDFA<CI>> e : new HashMap<>(procedures).entrySet()) {
                final CI proc = e.getKey();
                final CompactDFA<CI> oldDFA = e.getValue();
                final CompactDFA<CI> newDFA = new CompactDFA<>(minimizedProceduralInputAlphabet);

                Automata.minimize(oldDFA, minimizedProceduralInputAlphabet, newDFA);
                procedures.put(proc, newDFA);
            }

            // build (minimized) DTs
            for (AI ai : alphabet.getCallAlphabet()) {
                final Map<L, CI> l2ciMap = procedureMap.get(ai);
                final List<L> locations = new ArrayList<>();

                boolean sink = false; // we need one concretization for rejected words
                for (Entry<L, CI> e : l2ciMap.entrySet()) {
                    if (accessibleProcedures.contains(e.getValue())) {
                        locations.add(e.getKey());
                    } else if (!sink) {
                        locations.add(e.getKey());
                        sink = true;
                    }
                }

                final Node<AI, CI> dt = buildDT(sevpa, locations, l2ciMap, cs, as);
                dts.put(ai, dt);
            }

            spa = new StackSPA<>(minimizedAlphabet, mainProcedure, procedures);
        } else {
            // build (regular) dts
            for (AI ai : alphabet.getCallAlphabet()) {
                final Node<AI, CI> dt = buildDT(sevpa, sevpa.getLocations(), procedureMap.get(ai), cs, as);
                dts.put(ai, dt);
            }
        }

        return new ConversionResult<>(spa,
                                      dts,
                                      reverseMapping,
                                      new Mapper<>(alphabet, mainProcedure, dts, symbolMapper, sevpa::computeOutput));
    }

    private static <L, AI, CI> CompactDFA<CI> buildTemplate(OneSEVPA<L, AI> sevpa,
                                                            VPAlphabet<AI> alphabet,
                                                            ProceduralInputAlphabet<CI> spaAlphabet,
                                                            SymbolMapper<AI, CI> symbolMapper,
                                                            Map<AI, Map<L, CI>> procedureMap,
                                                            Map<L, Integer> map) {

        final Alphabet<CI> proceduralAlphabet = spaAlphabet.getProceduralAlphabet();
        final CompactDFA<CI> dfa = new CompactDFA<>(proceduralAlphabet, sevpa.size());
        final AI r = alphabet.getReturnSymbol(0);

        final L initLoc = sevpa.getInitialLocation();
        for (L l : sevpa.getLocations()) {
            final Integer s = dfa.addState();
            map.put(l, s);
            dfa.setInitial(s, Objects.equals(l, initLoc));
        }

        for (L l : sevpa.getLocations()) {
            final Integer s = map.get(l);
            for (AI ai : alphabet.getInternalAlphabet()) {
                final CI ci = symbolMapper.mapInternalSymbol(ai);
                final L succ = sevpa.getInternalSuccessor(l, ai);
                final Integer sSucc = map.get(succ);
                dfa.setTransition(s, ci, sSucc);
            }

            for (AI ai : alphabet.getCallAlphabet()) {
                for (L l2 : sevpa.getLocations()) {
                    final int sym = sevpa.encodeStackSym(l, ai);
                    final L succ = sevpa.getReturnSuccessor(l2, r, sym);
                    final Integer sSucc = map.get(succ);

                    final CI cRet = procedureMap.get(ai).get(l2);
                    dfa.setTransition(s, cRet, sSucc);
                }
            }
        }

        MutableDFAs.complete(dfa, proceduralAlphabet);
        return dfa;
    }

    private static <L, AI, CI> Node<AI, CI> buildDT(OneSEVPA<L, AI> sevpa,
                                                    Collection<L> nodes,
                                                    Map<L, CI> l2ciMap,
                                                    Collection<Pair<Word<AI>, Word<AI>>> cSet,
                                                    ArrayStorage<Word<AI>> as) {
        if (nodes.size() == 1) {
            return new Node<>(l2ciMap.get(nodes.iterator().next()));
        }

        for (Pair<Word<AI>, Word<AI>> cs : cSet) {
            final List<L> acc = new ArrayList<>(nodes.size());
            final List<L> rej = new ArrayList<>(nodes.size());

            for (L l : nodes) {
                if (sevpa.accepts(Word.fromWords(cs.getFirst(), as.get(sevpa.getLocationId(l)), cs.getSecond()))) {
                    acc.add(l);
                } else {
                    rej.add(l);
                }
            }

            if (!acc.isEmpty() && !rej.isEmpty()) {
                return new Node<>(cs.getFirst(),
                                  cs.getSecond(),
                                  buildDT(sevpa, acc, l2ciMap, cSet, as),
                                  buildDT(sevpa, rej, l2ciMap, cSet, as));
            }
        }

        throw new IllegalStateException("Characterizing set was unable to split locations. This should not happen.");
    }

    /**
     * The result of an {@link OneSEVPA}-to-{@link SPA} conversion. This class holds<ul>
     * <li>the {@link #spa transformed SPA},</li>
     * <li>a {@link #dts map} from abstract call symbols to the respective decision trees which can determine for an
     * abstract (well-matched) procedural invocation, its concretized call symbol in the SPA,</li>
     * <li>a {@link #reverseMapping reverse mapping} from concretized input symbols to their originating abstract ones,
     * and</li>
     * <li>a {@link #mapper mapper} that translates abstract words to concrete ones for the {@link #spa SPA} using the
     * provided {@link #dts decision trees}.</li>
     * </ul>
     *
     * @param <AI>
     *         abstract input symbol type
     * @param <CI>
     *         concrete input symbol type
     */
    public static class ConversionResult<AI, CI> {

        public final SPA<?, CI> spa;
        public final Map<AI, Node<AI, CI>> dts;
        public final Map<CI, AI> reverseMapping;
        public final Function<Word<AI>, Word<CI>> mapper;

        ConversionResult(SPA<?, CI> spa,
                         Map<AI, Node<AI, CI>> dts,
                         Map<CI, AI> reverseMapping,
                         Function<Word<AI>, Word<CI>> mapper) {
            this.spa = spa;
            this.dts = Collections.unmodifiableMap(dts);
            this.reverseMapping = Collections.unmodifiableMap(reverseMapping);
            this.mapper = mapper;
        }
    }

    public static class Node<AI, CI> {

        private final Word<AI> prefix;
        private final Word<AI> suffix;
        private final CI label;

        private final Node<AI, CI> trueSucc;
        private final Node<AI, CI> falseSucc;

        Node(CI label) {
            this(null, null, label, null, null);
        }

        Node(Word<AI> prefix, Word<AI> suffix, Node<AI, CI> trueSucc, Node<AI, CI> falseSucc) {
            this(prefix, suffix, null, trueSucc, falseSucc);
        }

        private Node(Word<AI> prefix, Word<AI> suffix, CI label, Node<AI, CI> trueSucc, Node<AI, CI> falseSucc) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.label = label;
            this.trueSucc = trueSucc;
            this.falseSucc = falseSucc;
        }

        public CI getLabel() {
            return label;
        }

        public Word<AI> getPrefix() {
            return prefix;
        }

        public Word<AI> getSuffix() {
            return suffix;
        }

        public boolean isLeaf() {
            return label != null;
        }

        public Node<AI, CI> sift(Word<AI> w, Predicate<Word<AI>> oracle) {
            if (this.isLeaf()) {
                return this;
            }

            final Word<AI> q = Word.fromWords(prefix, w, suffix);
            final boolean answer = oracle.test(q);

            return answer ? this.trueSucc.sift(w, oracle) : this.falseSucc.sift(w, oracle);
        }
    }

    static class Mapper<AI, CI> implements Function<Word<AI>, Word<CI>> {

        private final VPAlphabet<AI> alphabet;
        private final CI initialCall;
        private final Map<AI, Node<AI, CI>> dts;
        private final SymbolMapper<AI, CI> mapper;
        private final Predicate<Word<AI>> answerer;

        Mapper(VPAlphabet<AI> alphabet,
               CI initialCall,
               Map<AI, Node<AI, CI>> dts,
               SymbolMapper<AI, CI> mapper,
               Predicate<Word<AI>> answerer) {
            this.alphabet = alphabet;
            this.initialCall = initialCall;
            this.dts = dts;
            this.mapper = mapper;
            this.answerer = answerer;
        }

        @Override
        public Word<CI> apply(Word<AI> w) {
            assert alphabet.isWellMatched(w);

            final WordBuilder<CI> wb = new WordBuilder<>(w.length());

            wb.append(initialCall);

            for (int i = 0; i < w.length(); i++) {
                final AI sym = w.getSymbol(i);

                if (alphabet.isCallSymbol(sym)) {
                    final Node<AI, CI> dt = dts.get(sym);
                    final Word<AI> wm = alphabet.longestWellMatchedPrefix(w.subWord(i + 1));
                    final Node<AI, CI> leaf = dt.sift(wm, answerer);
                    wb.append(leaf.getLabel());
                } else if (alphabet.isInternalSymbol(sym)) {
                    wb.append(mapper.mapInternalSymbol(sym));
                } else if (alphabet.isReturnSymbol(sym)) {
                    wb.append(mapper.mapReturnSymbol(sym));
                } else { // symbol not contained in alphabet
                    throw new IllegalArgumentException("Unknown symbol: " + sym);
                }
            }

            wb.append(mapper.mapReturnSymbol(alphabet.getReturnSymbol(0)));

            return wb.toWord();
        }
    }
}
