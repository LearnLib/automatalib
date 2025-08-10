/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.util.graph.Graphs;
import net.automatalib.util.graph.apsp.APSPResult;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A randomized state cover test generator based on the concepts of mutation testing as described in the paper <a
 * href="https://doi.org/10.1007/978-3-319-57288-8_2">Learning from Faults: Mutation Testing in Active Automata
 * Learning</a> by Bernhard K. Aichernig and Martin Tappler.
 * <p>
 * A test case will be computed for every k-combination or k-permutation of states with additional random walk at the
 * end.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 * @param <T>
 *         transition type
 * @param <A>
 *         automaton type
 */
public class KWayStateCoverTestsIterator<S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>>
        implements Iterator<Word<I>> {

    public static final int DEFAULT_R_WALK_LEN = 20;
    public static final int DEFAULT_K = 2;

    private final A automaton;
    private final List<? extends I> alphabet;
    private final Random random;
    private final int randomWalkLen;
    private final int k;
    private final CombinationMethod method;

    private final Iterator<Word<I>> iterator;

    /**
     * Convenience constructor which uses a fresh {@code random} object.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     *
     * @see #KWayStateCoverTestsIterator(UniversalDeterministicAutomaton, Collection, Random)
     */
    public KWayStateCoverTestsIterator(A automaton, Collection<? extends I> inputs) {
        this(automaton, inputs, new Random());
    }

    /**
     * Convenience constructor. Uses {@code k=2}, {@code randomWalkLen = 20}, and
     * {@code method = CombinationMethod.PERMUTATIONS}.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     * @param random
     *         the random number generator to use
     *
     * @see #KWayStateCoverTestsIterator(UniversalDeterministicAutomaton, Collection, Random, int, int,
     * CombinationMethod)
     */
    public KWayStateCoverTestsIterator(A automaton, Collection<? extends I> inputs, Random random) {
        this(automaton, inputs, random, DEFAULT_R_WALK_LEN, DEFAULT_K, CombinationMethod.PERMUTATIONS);
    }

    /**
     * Constructor.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     * @param random
     *         the random number generator to use
     * @param randomWalkLen
     *         length of random walk performed at the end of each combination/permutation
     * @param k
     *         k value used for k-wise combinations/permutations of states
     * @param method
     *         the method for computing combinations
     */
    public KWayStateCoverTestsIterator(A automaton,
                                       Collection<? extends I> inputs,
                                       Random random,
                                       int randomWalkLen,
                                       int k,
                                       CombinationMethod method) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;
        this.randomWalkLen = randomWalkLen;
        this.k = Math.min(k, automaton.size());
        this.method = method;

        final S initial = automaton.getInitialState();

        if (automaton.size() == 0 || initial == null) {
            this.iterator = Collections.emptyIterator();
        } else {
            final FirstPhaseIterator firstIterator = new FirstPhaseIterator();
            final SecondPhaseIterator secondPhaseIterator = new SecondPhaseIterator(initial);
            this.iterator = IteratorUtil.concat(firstIterator, secondPhaseIterator);
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Word<I> next() {
        return iterator.next();
    }

    /**
     * Performs random walks if the automaton only has a single state.
     */
    private final class FirstPhaseIterator extends AbstractSimplifiedIterator<Word<I>> {

        private int idx;

        @Override
        protected boolean calculateNext() {
            if (automaton.size() == 1 && idx++ < randomWalkLen) {
                super.nextValue = Word.fromList(RandomUtil.sample(random, alphabet, randomWalkLen));
                return true;
            }
            return false;
        }
    }

    /**
     * Performs the actual k-way coverage for automata with more than a single state.
     */
    private final class SecondPhaseIterator extends AbstractSimplifiedIterator<Word<I>> {

        private final Iterator<List<S>> combIter;
        private final Set<Set<List<TransitionEdge<I, T>>>> cache;
        private final S initial;

        private APSPResult<S, TransitionEdge<I, T>> apsp;

        SecondPhaseIterator(S initial) {
            this.initial = initial;
            List<S> states = new ArrayList<>(automaton.getStates());
            Collections.shuffle(states, random);
            this.combIter = method.getCombinations(states, k);
            this.cache = new HashSet<>();
        }

        @Override
        protected boolean calculateNext() {

            final APSPResult<S, TransitionEdge<I, T>> apsp = getAPSP();

            while (combIter.hasNext()) {
                final List<S> comb = combIter.next();
                final Set<List<TransitionEdge<I, T>>> prefixes = new HashSet<>(HashUtil.capacity(comb.size()));

                List<TransitionEdge<I, T>> path = null;

                for (S c : comb) {
                    List<TransitionEdge<I, T>> sp = apsp.getShortestPath(initial, c);
                    if (sp != null) {
                        prefixes.add(sp);
                        if (path == null) {
                            path = sp;
                        }
                    }
                }

                if (path == null || !cache.add(prefixes)) {
                    continue;
                }

                final WordBuilder<I> pathBuilder = new WordBuilder<>();
                for (TransitionEdge<I, T> e : path) {
                    pathBuilder.append(e.getInput());
                }

                /*
                 * in case of non-strongly connected automata test case might not be possible as a path between 2 states
                 * might not exist
                 */
                boolean possibleTestCase = true;
                for (int index = 0; index < comb.size() - 1; index++) {
                    final List<TransitionEdge<I, T>> pathBetweenStates =
                            apsp.getShortestPath(comb.get(index), comb.get(index + 1));

                    if (pathBetweenStates == null || pathBetweenStates.isEmpty()) {
                        possibleTestCase = false;
                        break;
                    }

                    for (TransitionEdge<I, ?> t : pathBetweenStates) {
                        pathBuilder.append(t.getInput());
                    }
                }

                if (possibleTestCase) {
                    pathBuilder.append(RandomUtil.sample(random, alphabet, randomWalkLen));
                    super.nextValue = pathBuilder.toWord();
                    return true;
                }
            }

            return false;
        }

        /**
         * Compute all-pair-shortest-paths lazily, in case this iterator is never queried.
         *
         * @return the all-pair-shortest-paths result
         */
        private APSPResult<S, TransitionEdge<I, T>> getAPSP() {
            if (this.apsp == null) {
                this.apsp = Graphs.findAPSP(automaton.transitionGraphView(alphabet));
            }
            return this.apsp;
        }
    }

    /**
     * The specific method for generating combinations of states during exploration.
     */
    public enum CombinationMethod {
        /**
         * Generate all k-combinations of states.
         *
         * @see CollectionUtil#allCombintationsIterator(Collection, int)
         */
        COMBINATIONS {
            @Override
            <S> Iterator<List<S>> getCombinations(List<S> states, int k) {
                return CollectionUtil.allCombintationsIterator(states, k);
            }
        },
        /**
         * Generate all k-permutations of states.
         *
         * @see CollectionUtil#allPermutationsIterator(Collection, int)
         */
        PERMUTATIONS {
            @Override
            <S> Iterator<List<S>> getCombinations(List<S> states, int k) {
                return CollectionUtil.allPermutationsIterator(states, k);
            }
        };

        abstract <S> Iterator<List<S>> getCombinations(List<S> states, int k);
    }
}
