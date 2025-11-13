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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A randomized state cover test generator based on the concepts of mutation testing as described in the paper <a
 * href="https://doi.org/10.1007/978-3-319-57288-8_2">Learning from Faults: Mutation Testing in Active Automata
 * Learning</a> by Bernhard K. Aichernig and Martin Tappler.
 * <p>
 * A test case will be computed for every k-combination or k-permutation of states with additional random walk at the
 * end.
 * <p>
 * <b>Implementation detail:</b> Note that this test generator heavily relies on the sampling of states. If the given
 * automaton has very few or very many states, the number of generated test cases may be very low or high, respectively.
 * As a result, it may be advisable to {@link IteratorUtil#concat(Iterator[]) combine} this generator with other
 * generators or limit the number of generated test cases.
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
        extends AbstractSimplifiedIterator<Word<I>> {

    public static final int DEFAULT_R_WALK_LEN = 20;
    public static final int DEFAULT_K = 2;

    private final List<? extends I> alphabet;
    private final Random random;
    private final int randomWalkLen;

    private final Iterator<List<S>> combIter;
    private final Set<Set<List<TransitionEdge<I, T>>>> cache;
    private final @Nullable S initial;
    private final APSPResult<S, TransitionEdge<I, T>> apsp;

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
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;
        this.randomWalkLen = randomWalkLen;

        this.cache = new HashSet<>();
        this.apsp = Graphs.findAPSP(automaton.transitionGraphView(alphabet));
        this.initial = automaton.getInitialState();

        if (this.initial == null) {
            this.combIter = Collections.emptyIterator();
        } else {
            final List<S> states = new ArrayList<>(automaton.getStates());
            Collections.shuffle(states, random);
            this.combIter = method.getCombinations(states, Math.min(k, automaton.size()));
        }

    }

    @Override
    protected boolean calculateNext() {

        while (combIter.hasNext()) {
            final List<S> comb = combIter.next();
            final Set<List<TransitionEdge<I, T>>> prefixes = new HashSet<>(HashUtil.capacity(comb.size()));

            List<TransitionEdge<I, T>> path = null;
            assert initial != null;

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
