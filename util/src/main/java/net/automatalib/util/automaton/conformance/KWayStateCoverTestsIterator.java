/* Copyright (C) 2013-2026 TU Dortmund University
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import net.automatalib.automaton.DeterministicAutomaton.FiniteSemantics;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.common.util.mapping.Mappings;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.util.automaton.cover.Covers;
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
 * generators or {@link Stream#limit(long) limit} the number of generated test cases.
 *
 * @param <S>
 *         automaton state type
 * @param <I>
 *         input symbol type
 */
public class KWayStateCoverTestsIterator<S, I> extends AbstractSimplifiedIterator<Word<I>> {

    /**
     * The default value of k used in the k-way combinations/permutations.
     */
    public static final int DEFAULT_K = 2;

    /**
     * The default length of random walks performed at the end of each combination/permutation.
     */
    public static final int DEFAULT_R_WALK_LEN = 20;

    private final FiniteSemantics<S, I, ?> automaton;
    private final List<? extends I> alphabet;
    private final Random random;
    private final int randomWalkLen;

    private final Iterator<List<S>> combIter;
    private final Set<Set<Word<I>>> cache;
    private final @Nullable S initial;
    private final Map<S, Mapping<S, @Nullable Word<I>>> apsp;

    /**
     * Convenience constructor which uses a fresh {@code random} object.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     *
     * @see #KWayStateCoverTestsIterator(FiniteSemantics, Collection, Random)
     */
    public KWayStateCoverTestsIterator(FiniteSemantics<S, I, ?> automaton, Collection<? extends I> inputs) {
        this(automaton, inputs, new Random());
    }

    /**
     * Convenience constructor. Uses <code>k = {@value #DEFAULT_K}</code>, <code>randomWalkLen =
     * {@value #DEFAULT_R_WALK_LEN}</code>, and
     * <code>method = {@link CombinationMethod#PERMUTATIONS}</code>.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     * @param random
     *         the random number generator to use
     *
     * @see #KWayStateCoverTestsIterator(FiniteSemantics, Collection, Random, int, int, CombinationMethod)
     */
    public KWayStateCoverTestsIterator(FiniteSemantics<S, I, ?> automaton,
                                       Collection<? extends I> inputs,
                                       Random random) {
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
     *         length of random walks performed at the end of each combination/permutation
     * @param k
     *         k value used for k-way combinations/permutations of states
     * @param method
     *         the method for computing combinations
     */
    public KWayStateCoverTestsIterator(FiniteSemantics<S, I, ?> automaton,
                                       Collection<? extends I> inputs,
                                       Random random,
                                       int randomWalkLen,
                                       int k,
                                       CombinationMethod method) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;
        this.randomWalkLen = randomWalkLen;

        this.cache = new HashSet<>();
        this.initial = automaton.getInitialState();

        if (this.initial == null) {
            this.combIter = Collections.emptyIterator();
            this.apsp = Collections.emptyMap();
        } else {
            final List<S> states = new ArrayList<>(automaton.getStates());
            Collections.shuffle(states, random);
            this.combIter = method.getCombinations(states, Math.min(k, automaton.size()));
            this.apsp = new HashMap<>(HashUtil.capacity(automaton.size()));
            this.apsp.put(this.initial, Covers.cover(automaton, inputs, this.initial, w -> {}, w -> {}));
        }

    }

    @Override
    protected boolean calculateNext() {

        while (combIter.hasNext()) {
            final List<S> comb = combIter.next();
            final Set<Word<I>> prefixes = new HashSet<>(HashUtil.capacity(comb.size()));

            Word<I> path = null;
            assert initial != null;

            for (S c : comb) {
                Word<I> sp = apsp.getOrDefault(initial, Mappings.nullMapping()).get(c);
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

            final WordBuilder<I> pathBuilder = new WordBuilder<>(path);

            /*
             * in case of non-strongly connected automata test case might not be possible as a path between 2 states
             * might not exist
             */
            boolean possibleTestCase = true;
            for (int index = 0; index < comb.size() - 1; index++) {
                final Word<I> pathBetweenStates = apsp.computeIfAbsent(comb.get(index),
                                                                       k -> Covers.cover(automaton,
                                                                                         alphabet,
                                                                                         k,
                                                                                         w -> {},
                                                                                         w -> {}))
                                                      .get(comb.get(index + 1));

                if (pathBetweenStates == null || pathBetweenStates.isEmpty()) {
                    possibleTestCase = false;
                    break;
                }

                pathBuilder.append(pathBetweenStates);
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
