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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.graph.TransitionEdge;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.common.util.collection.AbstractTwoLevelIterator;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.util.graph.Graphs;
import net.automatalib.util.graph.apsp.APSPResult;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A randomized transition cover test generator based on the concepts of mutation testing as described in the paper <a
 * href="https://doi.org/10.1007/978-3-319-57288-8_2">Learning from Faults: Mutation Testing in Active Automata
 * Learning</a> by Bernhard K. Aichernig and Martin Tappler.
 * <p>
 * This iterator selects test cases based on k-way transitions coverage. It does that by generating random test words
 * and finding the smallest subset with the highest coverage. In other words, this iterator generates test words by
 * running random paths that cover all pairwise / k-way transitions.
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
public class KWayTransitionCoverTestsIterator<S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>>
        implements Iterator<Word<I>> {

    public static final int DEFAULT_R_WALK_LEN = 10;
    public static final int DEFAULT_NUM_GEN_PATHS = 1_000;
    public static final int DEFAULT_MAX_PATH_LENGTH = 50;
    public static final int DEFAULT_MAX_NUM_STEPS = 0;
    public static final int DEFAULT_K = 2;

    private final A automaton;
    private final List<? extends I> alphabet;
    private final Random random;
    private final int randomWalkLen;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final int k;
    private final OptimizationMetric optimizationMetric;

    private final Iterator<Word<I>> iterator;

    /**
     * Convenience constructor which uses a fresh {@code random} object.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     *
     * @see #KWayTransitionCoverTestsIterator(UniversalDeterministicAutomaton, Collection, Random)
     */
    public KWayTransitionCoverTestsIterator(A automaton, Collection<? extends I> inputs) {
        this(automaton, inputs, new Random());
    }

    /**
     * Convenience constructor. Uses {@code randomWalkLen=10}, {@code numGeneratePaths = 1_000},
     * {@code maxPathLen = 50}, {@code maxNumberOfSteps = 0}, {@code k = 2},
     * {@code optimizationMetric = OptimizationMetric.STEPS}, and {@code generationMethod = GenerationMethod.RANDOM}.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     * @param random
     *         the random number generator to use
     *
     * @see #KWayTransitionCoverTestsIterator(UniversalDeterministicAutomaton, Collection, Random, int, int, int, int,
     * int, OptimizationMetric, GenerationMethod)
     */
    public KWayTransitionCoverTestsIterator(A automaton, Collection<? extends I> inputs, Random random) {
        this(automaton,
             inputs,
             random,
             DEFAULT_R_WALK_LEN,
             DEFAULT_NUM_GEN_PATHS,
             DEFAULT_MAX_PATH_LENGTH,
             DEFAULT_MAX_NUM_STEPS,
             DEFAULT_K,
             OptimizationMetric.STEPS,
             GenerationMethod.RANDOM);
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
     *         the number of steps that are added by {@link GenerationMethod#PREFIX prefix}-generated paths
     * @param numGeneratePaths
     *         number of {@link GenerationMethod#RANDOM randomly}-generated tests used to find the optimal subset
     * @param maxPathLen
     *         the maximum step size of {@link GenerationMethod#RANDOM randomly}-generated paths
     * @param maxNumberOfSteps
     *         threshold for the number of steps after which no more new test words will be generated (<=0 = no limit)
     * @param k
     *         k value used for K-Way transitions, i.e.,the number of steps between the start and the end of a
     *         transition
     * @param optimizationMetric
     *         the metric after which test cases are minimized
     * @param generationMethod
     *         defines how the tests are generated
     */
    public KWayTransitionCoverTestsIterator(A automaton,
                                            Collection<? extends I> inputs,
                                            Random random,
                                            int randomWalkLen,
                                            int numGeneratePaths,
                                            int maxPathLen,
                                            int maxNumberOfSteps,
                                            int k,
                                            OptimizationMetric optimizationMetric,
                                            GenerationMethod generationMethod) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;

        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.randomWalkLen = randomWalkLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.k = Math.min(k, automaton.size());
        this.optimizationMetric = optimizationMetric;

        final S initial = automaton.getInitialState();

        if (automaton.size() == 0 || initial == null) {
            this.iterator = Collections.emptyIterator();
        } else {
            this.iterator = generationMethod.getIterator(this, initial);
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

    private Set<Path<S, I>> generateRandomPaths(A hypothesis, S initial) {
        final Set<Path<S, I>> result = new HashSet<>(HashUtil.capacity(numGeneratePaths));

        for (int i = 0; i < numGeneratePaths; i++) {
            final int randomLength = random.nextInt(maxPathLen - k + 1) + k; // Ensuring length is at least `k`
            final Word<I> steps = Word.fromList(RandomUtil.sample(random, alphabet, randomLength));
            final Path<S, I> path = createPath(hypothesis, initial, steps);
            result.add(path);
        }

        return result;
    }

    private Path<S, I> createPath(A hypothesis, S initial, Word<I> steps) {
        final Set<KWayTransition<S, I>> transitions = new HashSet<>();

        final List<@Nullable S> prevStates = new ArrayList<>(steps.size());
        final List<@Nullable S> endStates = new ArrayList<>(steps.size());

        S iter = initial;

        for (I i : steps) {
            prevStates.add(iter);
            iter = iter == null ? null : hypothesis.getSuccessor(iter, i);
            endStates.add(iter);
        }

        for (int i = 0; i < steps.size() - k + 1; i++) {
            final @Nullable S prevState = prevStates.get(i);
            final @Nullable S endState = endStates.get(i + k - 1);
            final Word<I> chunk = steps.subWord(i, i + k);

            final KWayTransition<S, I> transition = new KWayTransition<>(prevState, endState, chunk);

            transitions.add(transition);
        }

        return new Path<>(steps, transitions);
    }

    private Iterator<Word<I>> generatePrefixSteps(A hypothesis, S initial) {
        final List<S> states = new ArrayList<>(hypothesis.getStates());
        Collections.reverse(states);
        return new PrefixStepsIterator(states.iterator(), initial);
    }

    private @Nullable Path<S, I> selectOptimalPath(Set<KWayTransition<S, I>> covered, Set<Path<S, I>> paths) {
        final Path<S, I> max = Collections.max(paths, optimizationMetric.getPathComparator(covered));
        return sizeOfSetDifference(max.kWayTransitions, covered) > 0 ? max : null;
    }

    static <T> int sizeOfSetDifference(Set<T> minuend, Set<T> subtrahend) {
        /*
         * This method is performance-critical so we do a little bit more involved computation.
         */
        final Set<T> smaller, bigger;
        if (minuend.size() < subtrahend.size()) {
            smaller = minuend;
            bigger = subtrahend;
        } else {
            smaller = subtrahend;
            bigger = minuend;
        }

        int size = minuend.size();
        for (T t : smaller) {
            if (bigger.contains(t)) {
                size--;
            }
        }
        return size;
    }

    private class GreedySetCoverIterator extends AbstractSimplifiedIterator<Word<I>> {

        private final S initial;
        private final Set<Path<S, I>> paths;
        private final Set<KWayTransition<S, I>> covered;
        private final int sizeOfUniverse;

        private int stepCount;

        GreedySetCoverIterator(S initial) {
            this.initial = initial;
            this.paths = generateRandomPaths(automaton, initial);
            this.covered = new HashSet<>();
            this.stepCount = 0;
            this.sizeOfUniverse = automaton.getStates().size() * (int) Math.pow(alphabet.size(), k);
        }

        @Override
        protected boolean calculateNext() {
            while (sizeOfUniverse > covered.size() && (maxNumberOfSteps == 0 || stepCount <= maxNumberOfSteps)) {
                final Path<S, I> path = selectOptimalPath(covered, paths);

                if (path != null) {
                    covered.addAll(path.kWayTransitions);
                    paths.remove(path);
                    stepCount += path.steps.size();
                    super.nextValue = path.steps;

                    if (paths.isEmpty()){
                        computeNewPaths();
                    }
                    return true;
                } else {
                    computeNewPaths();
                }
            }
            return false;
        }

        private void computeNewPaths() {
            final Iterator<Word<I>> prefixIterator = generatePrefixSteps(automaton, initial);
            while (prefixIterator.hasNext()) {
                final Word<I> generatePrefixStep = prefixIterator.next();
                paths.add(createPath(automaton, initial, generatePrefixStep));
            }
        }
    }

    private class PrefixStepsIterator extends AbstractTwoLevelIterator<S, List<I>, Word<I>> {

        private final APSPResult<S, TransitionEdge<I, T>> apsp;
        private final S initial;

        PrefixStepsIterator(Iterator<S> iterator, S initial) {
            super(iterator);
            this.apsp = Graphs.findAPSP(automaton.transitionGraphView(alphabet));
            this.initial = initial;
        }

        @Override
        protected Iterator<List<I>> l2Iterator(S state) {
            /*
             * The original code shuffles all tuples globally. Since we can't do this lazily, we approximate this
             * behavior by at least shuffling the input symbols for a randomized tuple order.
             */
            final List<I> inputs = new ArrayList<>(alphabet);
            Collections.shuffle(inputs, random);
            final Iterable<List<I>> lists = IterableUtil.allTuples(inputs, k);
            return lists.iterator();
        }

        @Override
        protected Word<I> combine(S state, List<I> steps) {
            final List<TransitionEdge<I, T>> prefix = apsp.getShortestPath(initial, state);
            if (prefix == null) {
                return Word.epsilon();
            }

            final WordBuilder<I> wb = new WordBuilder<>();
            for (TransitionEdge<I, T> edge : prefix) {
                wb.append(edge.getInput());
            }

            wb.addAll(steps);
            wb.addAll(RandomUtil.sample(random, alphabet, randomWalkLen));

            return wb.toWord();
        }
    }

    private static final class KWayTransition<S, I> {

        private final @Nullable S startState;
        private final @Nullable S endState;
        private final Word<I> steps;

        /**
         * Since we need to compute the hash code quite often, cache it since we're immutable.
         */
        private final int hashCode;

        KWayTransition(@Nullable S startState, @Nullable S endState, Word<I> steps) {
            this.startState = startState;
            this.endState = endState;
            this.steps = steps;

            this.hashCode = computeHashCode(startState, endState, steps);
        }

        private int computeHashCode(@Nullable S startState, @Nullable S endState, Word<I> steps) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(startState);
            result = prime * result + Objects.hashCode(endState);
            result = prime * result + Objects.hashCode(steps);
            return result;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final KWayTransition<?, ?> that = (KWayTransition<?, ?>) o;
            return Objects.equals(startState, that.startState) && Objects.equals(endState, that.endState) &&
                   Objects.equals(steps, that.steps);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static final class Path<S, I> {

        private final Word<I> steps;
        private final Set<KWayTransition<S, I>> kWayTransitions;

        Path(Word<I> steps, Set<KWayTransition<S, I>> kWayTransitions) {
            this.steps = steps;
            this.kWayTransitions = kWayTransitions;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == this) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Path<?, ?> path = (Path<?, ?>) o;
            return Objects.equals(steps, path.steps) && Objects.equals(kWayTransitions, path.kWayTransitions);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(steps);
            result = 31 * result + Objects.hashCode(kWayTransitions);
            return result;
        }
    }

    /**
     * Method by which the prefixes of test words should be generated.
     */
    public enum GenerationMethod {
        /**
         * Generate prefixes randomly.
         */
        RANDOM {
            @Override
            <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                    KWayTransitionCoverTestsIterator<S, I, T, A> self,
                    S initial) {
                return self.new GreedySetCoverIterator(initial);
            }
        },
        /**
         * Generate prefixes based on access sequences.
         */
        PREFIX {
            @Override
            <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                    KWayTransitionCoverTestsIterator<S, I, T, A> self,
                    S initial) {
                return self.generatePrefixSteps(self.automaton, initial);
            }
        };

        abstract <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                KWayTransitionCoverTestsIterator<S, I, T, A> self,
                S initial);
    }

    /**
     * The metric by which to optimize path selection.
     */
    public enum OptimizationMetric {
        /**
         * Selects the paths maximum coverage per step, thus reducing the number of total steps.
         */
        STEPS {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> ((double) sizeOfSetDifference(p.kWayTransitions, covered)) /
                                                       p.steps.size());
            }
        },
        /**
         * Selects the paths with maximum coverage, thus reducing number of test words.
         */
        QUERIES {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> sizeOfSetDifference(p.kWayTransitions, covered));
            }
        };

        abstract <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered);
    }
}
