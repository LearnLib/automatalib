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
package net.automatalib.util.automaton.procedural;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.automaton.vpa.SEVPA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.graph.UniversalGraph;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.graph.Graphs;
import net.automatalib.util.graph.apsp.APSPResult;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Operations on {@link SPA}s.
 */
public final class SPAs {

    private SPAs() {
        // prevent instantiation
    }

    /**
     * Computes a set of access sequences, terminating sequences, and return sequences for a given {@link SPA}. This is
     * a convenience method for {@link #computeATRSequences(SPA, ProceduralInputAlphabet)} that automatically uses the
     * {@link SPA#getInputAlphabet() input alphabet} of the given {@code spa}.
     *
     * @param spa
     *         the {@link SPA} for which the sequences should be computed
     * @param <I>
     *         input symbol type
     *
     * @return an {@link ATRSequences} object which contains the respective sequences.
     *
     * @see #computeATRSequences(SPA, ProceduralInputAlphabet)
     */
    public static <I> ATRSequences<I> computeATRSequences(SPA<?, I> spa) {
        return computeATRSequences(spa, spa.getInputAlphabet());
    }

    /**
     * Computes a set of access sequences, terminating sequences, and return sequences for a given {@link SPA} limited
     * to the symbols of the given {@link ProceduralInputAlphabet}.
     *
     * @param spa
     *         the {@link SPA} for which the sequences should be computed
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for computing the respective sequences
     * @param <I>
     *         input symbol type
     *
     * @return an {@link ATRSequences} object which contains the respective sequences.
     *
     * @see #computeAccessAndReturnSequences(SPA, ProceduralInputAlphabet, Map)
     * @see #computeTerminatingSequences(SPA, ProceduralInputAlphabet)
     */
    public static <I> ATRSequences<I> computeATRSequences(SPA<?, I> spa, ProceduralInputAlphabet<I> alphabet) {

        final Map<I, Word<I>> terminatingSequences = computeTerminatingSequences(spa, alphabet);
        final Pair<Map<I, Word<I>>, Map<I, Word<I>>> accessAndReturnSequences =
                computeAccessAndReturnSequences(spa, alphabet, terminatingSequences);

        final Map<I, Word<I>> accessSequences = accessAndReturnSequences.getFirst();
        final Map<I, Word<I>> returnSequences = accessAndReturnSequences.getSecond();

        return new ATRSequences<>(accessSequences, terminatingSequences, returnSequences);
    }

    /**
     * Computes for a given {@link SPA} a set of terminating sequences using the given
     * {@link ProceduralInputAlphabet alphabet}. Terminating sequences transfer a procedure from its initial state to an
     * accepting state. This method furthermore checks that the hierarchy of calls is well-defined, i.e. it only
     * includes procedural invocations <i>p</i> for determining a terminating sequence if <i>p</i> has a valid
     * terminating sequence itself.
     *
     * @param spa
     *         the {@link SPA} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for determining the terminating
     *         sequences
     * @param <I>
     *         input symbol type
     *
     * @return A map from procedures (restricted to the call symbols of the given alphabet) to the terminating
     * sequences. This map may be partial as some procedures may not have a well-defined terminating sequence for the
     * given alphabet.
     */
    public static <I> Map<I, Word<I>> computeTerminatingSequences(SPA<?, I> spa, ProceduralInputAlphabet<I> alphabet) {
        return ProceduralUtil.computeTerminatingSequences(spa.getProcedures(), alphabet, DFA::accepts);
    }

    /**
     * Computes for a given {@link SPA} a set of access sequences and return sequences using the given
     * {@link ProceduralInputAlphabet alphabet}. An access sequence (for procedure <i>p</i>) transfers an {@link SPA}
     * from its initial state to a state that is able to successfully execute a run of <i>p</i>, whereas the
     * corresponding return sequence transfers the {@link SPA} to the global accepting state from an accepting state of
     * <i>p</i>. This method furthermore checks that potentially nested calls are well-defined, i.e. it only includes
     * procedural invocations <i>p</i> for determining access/return sequences if <i>p</i> has a valid terminating
     * sequence and therefore can be expanded correctly.
     *
     * @param spa
     *         the {@link SPA} to analyze
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be used for determining the access and return
     *         sequences
     * @param terminatingSequences
     *         a {@link Map} of call symbols to terminating sequences used to expand nested invocations in access
     *         sequences
     * @param <I>
     *         input symbol type
     *
     * @return A pair of maps from procedures (restricted to the call symbols of the given alphabet) to the
     * access/return sequences. These maps may be partial as some procedures may not have well-defined
     * access/terminating sequences for the given alphabet.
     */
    public static <I> Pair<Map<I, Word<I>>, Map<I, Word<I>>> computeAccessAndReturnSequences(SPA<?, I> spa,
                                                                                             ProceduralInputAlphabet<I> alphabet,
                                                                                             Map<I, Word<I>> terminatingSequences) {
        final I initialProcedure = spa.getInitialProcedure();

        if (initialProcedure == null || spa.getProcedure(initialProcedure) == null) {
            return Pair.of(Collections.emptyMap(), Collections.emptyMap());
        }

        final Map<I, DFA<?, I>> submodels = spa.getProcedures();
        final Collection<I> proceduralInputs = spa.getProceduralInputs(alphabet);

        final Map<I, Word<I>> accessSequences = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
        final Map<I, Word<I>> returnSequences = new HashMap<>(HashUtil.capacity(alphabet.getNumCalls()));
        final Set<I> finishedProcedures = new HashSet<>(HashUtil.capacity(alphabet.getNumCalls()));

        // initial value
        accessSequences.put(initialProcedure, Word.fromLetter(initialProcedure));
        returnSequences.put(initialProcedure, Word.fromLetter(alphabet.getReturnSymbol()));
        finishedProcedures.add(initialProcedure);

        final Deque<I> pendingProcedures = new ArrayDeque<>();
        pendingProcedures.add(initialProcedure);

        while (!pendingProcedures.isEmpty()) {
            final I i = pendingProcedures.pop();
            final DFA<?, I> dfa = submodels.get(i);

            if (dfa != null) {
                final Collection<I> newProcedures = discoverAccessAndReturnSequences(alphabet,
                                                                                     proceduralInputs,
                                                                                     i,
                                                                                     dfa,
                                                                                     finishedProcedures,
                                                                                     accessSequences,
                                                                                     terminatingSequences,
                                                                                     returnSequences);
                pendingProcedures.addAll(newProcedures);
            }
        }

        return Pair.of(accessSequences, returnSequences);
    }

    private static <I> Collection<I> discoverAccessAndReturnSequences(ProceduralInputAlphabet<I> alphabet,
                                                                      Collection<I> proceduralInputs,
                                                                      I procedure,
                                                                      DFA<?, I> dfa,
                                                                      Set<I> finishedProcedures,
                                                                      Map<I, Word<I>> accessSequences,
                                                                      Map<I, Word<I>> terminatingSequences,
                                                                      Map<I, Word<I>> returnSequences) {

        final List<I> newASRS = new ArrayList<>();
        final List<Word<I>> acceptingPaths = exploreAccessSequences(dfa, proceduralInputs, alphabet::isCallSymbol);

        tc:
        for (Word<I> trace : acceptingPaths) {

            for (int i = 0; i < trace.length(); i++) {
                final I input = trace.getSymbol(i);

                if (alphabet.isCallSymbol(input)) {
                    if (!finishedProcedures.contains(input)) {

                        final Word<I> remainingTrace = trace.subWord(i + 1);
                        for (I r : remainingTrace) {
                            if (alphabet.isCallSymbol(r) && !terminatingSequences.containsKey(r)) {
                                // If we encounter a call symbol for which we do not have a terminating sequence,
                                // the remaining return sequences cannot be expanded properly.
                                // Therefore, skip the current trace.
                                continue tc;
                            }
                        }

                        // we only query existing terminating sequences, therefore nullity is fine
                        @SuppressWarnings("methodref.return")
                        final Mapping<I, Word<I>> tsMapping = terminatingSequences::get;

                        final WordBuilder<I> accessBuilder = new WordBuilder<>();
                        // we only invoke this method with finished procedures
                        @SuppressWarnings("assignment")
                        final @NonNull Word<I> as = accessSequences.get(procedure);
                        accessBuilder.append(as);
                        accessBuilder.append(alphabet.expand(trace.subWord(0, i), tsMapping));
                        accessBuilder.append(input);

                        accessSequences.put(input, accessBuilder.toWord());

                        final WordBuilder<I> terminatingBuilder = new WordBuilder<>();
                        // we only invoke this method with finished procedures
                        @SuppressWarnings("assignment")
                        final @NonNull Word<I> rs = returnSequences.get(procedure);
                        terminatingBuilder.append(alphabet.getReturnSymbol());
                        terminatingBuilder.append(alphabet.expand(remainingTrace, tsMapping));
                        terminatingBuilder.append(rs);

                        returnSequences.put(input, terminatingBuilder.toWord());

                        finishedProcedures.add(input);
                        newASRS.add(input);
                    } else if (!terminatingSequences.containsKey(input)) {
                        // If we encounter a call symbol for which we do not have a terminating sequence,
                        // all local access sequences of future call symbols cannot be expanded properly.
                        // Therefore, skip the current trace.
                        continue tc;
                    }
                }

                if (finishedProcedures.containsAll(alphabet.getCallAlphabet())) {
                    return newASRS;
                }
            }
        }
        return newASRS;
    }

    private static <S, I> List<Word<I>> exploreAccessSequences(DFA<S, I> dfa,
                                                               Collection<I> inputs,
                                                               Predicate<I> callPredicate) {
        final S init = dfa.getInitialState();

        if (init == null) {
            return Collections.emptyList();
        }

        final List<Word<I>> result = new ArrayList<>(inputs.size());
        final UniversalGraph<S, TransitionEdge<I, S>, ?, ?> tgv = dfa.transitionGraphView(inputs);
        final APSPResult<S, TransitionEdge<I, S>> apsp = Graphs.findAPSP(tgv, edge -> 0F);

        final List<S> acceptingStates = new ArrayList<>(dfa.size());
        for (S s : dfa) {
            if (dfa.isAccepting(s)) {
                acceptingStates.add(s);
            }
        }

        calls:
        for (I i : inputs) {
            if (callPredicate.test(i)) {
                for (S s : dfa) {
                    final S succ = dfa.getSuccessor(s, i);

                    if (succ != null) {
                        final List<TransitionEdge<I, S>> init2s = apsp.getShortestPath(init, s);

                        if (init2s != null) {
                            for (S acc : acceptingStates) {
                                final List<TransitionEdge<I, S>> succ2acc = apsp.getShortestPath(succ, acc);

                                if (succ2acc != null) {
                                    final WordBuilder<I> wb = new WordBuilder<>(init2s.size() + succ2acc.size() + 1);

                                    for (TransitionEdge<I, S> t : init2s) {
                                        wb.append(t.getInput());
                                    }
                                    wb.append(i);
                                    for (TransitionEdge<I, S> t : succ2acc) {
                                        wb.append(t.getInput());
                                    }

                                    result.add(wb.toWord());
                                    continue calls;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Convenience method for {@link #isMinimal(SPA, ProceduralInputAlphabet)} that uses {@link SPA#getInputAlphabet()}
     * as {@code alphabet}.
     *
     * @param spa
     *         the {@link SPA} to check
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     *
     * @see #isMinimal(SPA, ProceduralInputAlphabet)
     */
    public static <I> boolean isMinimal(SPA<?, I> spa) {
        return isMinimal(spa, spa.getInputAlphabet());
    }

    /**
     * Checks if a given {@link SPA} is redundancy-free, i.e. if for all {@link SPA#getProcedures() procedures} there
     * exists an access sequence, terminating sequence, and return sequence.
     *
     * @param spa
     *         the {@link SPA} to check
     * @param alphabet
     *         the {@link ProceduralInputAlphabet alphabet} whose symbols should be used for computing the respective
     *         sequences
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     */
    public static <I> boolean isMinimal(SPA<?, I> spa, ProceduralInputAlphabet<I> alphabet) {
        return isMinimal(alphabet, computeATRSequences(spa, alphabet));
    }

    /**
     * Checks if a pre-computed set of {@link ATRSequences} of an {@link SPA} is minimal.
     *
     * @param alphabet
     *         the {@link ProceduralInputAlphabet alphabet} which should be used for computing the respective sequences
     * @param atrSequences
     *         the pre-computed {@link ATRSequences}
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if {@code spa} is redundancy-free, {@code false} otherwise.
     *
     * @see #isMinimal(SPA, ProceduralInputAlphabet)
     */
    public static <I> boolean isMinimal(ProceduralInputAlphabet<I> alphabet, ATRSequences<I> atrSequences) {
        final Alphabet<I> callAlphabet = alphabet.getCallAlphabet();
        return atrSequences.accessSequences.keySet().containsAll(callAlphabet) &&
               atrSequences.terminatingSequences.keySet().containsAll(callAlphabet) &&
               atrSequences.returnSequences.keySet().containsAll(callAlphabet);
    }

    /**
     * Checks if the two given {@link SPA}s are equivalent, i.e. whether there exists a
     * {@link #findSeparatingWord(SPA, SPA, ProceduralInputAlphabet) separating word} for them.
     *
     * @param spa1
     *         the first {@link SPA}
     * @param spa2
     *         the second {@link SPA}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be considered for equivalence testing
     * @param <I>
     *         input symbol type
     *
     * @return {@code true} if the two {@link SPA}s are equivalent, {@code false} otherwise.
     *
     * @see #findSeparatingWord(SPA, SPA, ProceduralInputAlphabet)
     */
    public static <I> boolean testEquivalence(SPA<?, I> spa1, SPA<?, I> spa2, ProceduralInputAlphabet<I> alphabet) {
        return findSeparatingWord(spa1, spa2, alphabet) == null;
    }

    /**
     * Computes a separating word for the two given {@link SPA}s, if existent. A separating word is a {@link Word} such
     * that one {@link SPA} {@link SPA#accepts(Iterable) behaves} different from the other.
     *
     * @param spa1
     *         the first {@link SPA}
     * @param spa2
     *         the second {@link SPA}
     * @param alphabet
     *         the {@link ProceduralInputAlphabet} whose symbols should be considered for computing the separating word
     * @param <I>
     *         input symbol type
     *
     * @return a separating word, if existent, {@code null} otherwise.
     */
    public static <I> @Nullable Word<I> findSeparatingWord(SPA<?, I> spa1,
                                                           SPA<?, I> spa2,
                                                           ProceduralInputAlphabet<I> alphabet) {

        final ATRSequences<I> atr1 = computeATRSequences(spa1, alphabet);
        final ATRSequences<I> atr2 = computeATRSequences(spa2, alphabet);

        for (I procedure : alphabet.getCallAlphabet()) {
            final DFA<?, I> p1 = spa1.getProcedure(procedure);
            final DFA<?, I> p2 = spa2.getProcedure(procedure);

            if (p1 != null && p2 != null) {
                final Word<I> as1 = atr1.accessSequences.get(procedure);
                final Word<I> ts1 = atr1.terminatingSequences.get(procedure);
                final Word<I> rs1 = atr1.returnSequences.get(procedure);

                final Word<I> as2 = atr2.accessSequences.get(procedure);
                final Word<I> ts2 = atr2.terminatingSequences.get(procedure);
                final Word<I> rs2 = atr2.returnSequences.get(procedure);

                if (as1 != null && ts1 != null && rs1 != null && as2 != null && ts2 != null && rs2 != null) {
                    // we can embed both procedures and both procedures have accepting runs

                    final Set<I> localAlphabet = new HashSet<>(atr1.terminatingSequences.keySet());
                    localAlphabet.retainAll(atr2.terminatingSequences.keySet());
                    localAlphabet.addAll(alphabet.getInternalAlphabet());

                    final Word<I> sepWord = Automata.findSeparatingWord(p1, p2, localAlphabet);

                    if (sepWord != null) {
                        // select ATR based on accepting DFA, so that we don't change acceptance due to incompatible access/return sequences
                        final ATRSequences<I> acceptingATR = p1.accepts(sepWord) ? atr1 : atr2;

                        final Word<I> as = acceptingATR.accessSequences.get(procedure);
                        final Word<I> ts = alphabet.expand(sepWord, acceptingATR.terminatingSequences::get);
                        final Word<I> rs = acceptingATR.returnSequences.get(procedure);

                        return Word.fromWords(as, ts, rs);
                    }
                } else if (as1 != null && ts1 != null && rs1 != null) {
                    return Word.fromWords(as1, ts1, rs1);
                } else if (as2 != null && ts2 != null && rs2 != null) {
                    return Word.fromWords(as2, ts2, rs2);
                } // else no procedures can be embedded
            } else if (p1 != null) {
                final Word<I> as = atr1.accessSequences.get(procedure);
                final Word<I> ts = atr1.terminatingSequences.get(procedure);
                final Word<I> rs = atr1.returnSequences.get(procedure);

                if (as != null && ts != null && rs != null) {
                    return Word.fromWords(as, ts, rs);
                }
            } else if (p2 != null) {
                final Word<I> as = atr2.accessSequences.get(procedure);
                final Word<I> ts = atr2.terminatingSequences.get(procedure);
                final Word<I> rs = atr2.returnSequences.get(procedure);

                if (as != null && ts != null && rs != null) {
                    return Word.fromWords(as, ts, rs);
                }
            } // else both procedures are null and therefore skip this call symbol
        }

        return null;
    }

    /**
     * Transforms the given {@link SPA} into a language-equivalent {@link OneSEVPA}.
     *
     * @param spa
     *         the {@link SPA} to transform
     * @param <I>
     *         input symbol type
     *
     * @return the language-equivalent {@link OneSEVPA}
     */
    public static <I> OneSEVPA<?, I> toOneSEVPA(SPA<?, I> spa) {
        return OneSEVPAConverter.convert(spa);
    }

    /**
     * Transforms the given {@link SPA} into a language-equivalent {@link SEVPA N-SEVPA}.
     *
     * @param spa
     *         the {@link SPA} to transform
     * @param <I>
     *         input symbol type
     *
     * @return the language-equivalent {@link SEVPA N-SEVPA}
     */
    public static <I> SEVPA<?, I> toNSEVPA(SPA<?, I> spa) {
        return NSEVPAConverter.convert(spa);
    }

    /**
     * Returns a {@link ContextFreeModalProcessSystem}-based view on the language of a given {@link SPA} such that there
     * exists a {@code w}-labeled path to the final node of the returned CFMPS' main procedure if and only if {@code w}
     * is accepted by the given {@link SPA}. This allows one to model-check language properties of {@link SPA}s with
     * tools such as M3C.
     *
     * @param spa
     *         the {@link SPA} to convert
     * @param <I>
     *         input symbol type
     *
     * @return the {@link ContextFreeModalProcessSystem}-based view on the given {@code spa}.
     */
    public static <I> ContextFreeModalProcessSystem<I, Void> toCFMPS(SPA<?, I> spa) {
        return new CFMPSViewSPA<>(spa);
    }

}
