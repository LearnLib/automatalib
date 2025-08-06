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
import java.util.LinkedHashSet;
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
 * This iterates selects test cases based on k-way transitions coverage. It does that by generating random test words
 * and finding the smallest subset with the highest coverage. In other words, this iterator generates test words by
 * running random paths that cover all pairwise / k-way transitions.
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
    private final int k;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final OptimizationMetric optimizationMetric;
    private final int randomWalkLen;

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
     * {@code generationMethod = GenerationMethod.RANDOM}, and {@code optimizationMetric = OptimizationMetric.STEPS}.
     *
     * @param automaton
     *         the automaton for which to generate test cases
     * @param inputs
     *         the inputs to consider for test case generation
     * @param random
     *         the random number generator to use
     *
     * @see #KWayTransitionCoverTestsIterator(UniversalDeterministicAutomaton, Collection, Random, int, int, int, int,
     * int, GenerationMethod, OptimizationMetric)
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
             GenerationMethod.RANDOM,
             OptimizationMetric.STEPS);
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
     *         the number of steps that are added by 'prefix' generated paths
     * @param numGeneratePaths
     *         number of random queries used to find the optimal subset
     * @param maxPathLen
     *         the maximum step size of a generated path
     * @param maxNumberOfSteps
     *         maximum number of steps that will be executed on the automaton (<=0 = no limit)
     * @param k
     *         k value used for K-Way transitions, i.e the number of steps between the start and the end of a
     *         transition
     * @param generationMethod
     *         defines how the queries are generated 'random' or 'prefix'
     * @param optimizationMetric
     *         minimize either the number of 'steps' or 'queries' that are executed
     */
    public KWayTransitionCoverTestsIterator(A automaton,
                                            Collection<? extends I> inputs,
                                            Random random,
                                            int randomWalkLen,
                                            int numGeneratePaths,
                                            int maxPathLen,
                                            int maxNumberOfSteps,
                                            int k,
                                            GenerationMethod generationMethod,
                                            OptimizationMetric optimizationMetric) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;

        this.k = Math.min(k, automaton.size());
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.optimizationMetric = optimizationMetric;
        this.randomWalkLen = randomWalkLen;

        final S initial = automaton.getInitialState();

        if (automaton.size() == 0 || initial == null) {
            this.iterator = Collections.emptyIterator();
        } else {
            this.iterator = generationMethod.getIterator(this);
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

    private Set<Path<S, I>> generateRandomPaths(A hypothesis) {
        Set<Path<S, I>> result = new LinkedHashSet<>(HashUtil.capacity(numGeneratePaths));

        for (int i = 0; i < numGeneratePaths; i++) {
            int randomLength = random.nextInt(maxPathLen - k + 1) + k; // Ensuring length is at least `k`
            Word<I> steps = Word.fromList(RandomUtil.sample(random, alphabet, randomLength));
            Path<S, I> path = createPath(hypothesis, steps);
            result.add(path);
        }

        return result;
    }

    private Path<S, I> createPath(A hypothesis, Word<I> steps) {
        Set<KWayTransition<S, I>> transitions = new HashSet<>();
        List<KWayTransition<S, I>> transitionsLog = new ArrayList<>();

        List<S> prevStates = new ArrayList<>(steps.size());
        List<S> endStates = new ArrayList<>(steps.size());

        S iter = hypothesis.getInitialState();

        for (I i : steps) {
            prevStates.add(iter);
            iter = hypothesis.getSuccessor(iter, i);
            endStates.add(iter);
        }

        for (int i = 0; i < steps.size() - k + 1; i++) {
            S prevState = prevStates.get(i);
            S endState = endStates.get(i + k - 1);
            Word<I> chunk = steps.subWord(i, i + k);

            KWayTransition<S, I> transition = new KWayTransition<>(prevState, endState, chunk);

            transitionsLog.add(transition);
            transitions.add(transition);
        }

        return new Path<>(hypothesis.getInitialState(),
                          endStates.get(endStates.size() - 1),
                          steps,
                          transitions,
                          transitionsLog);
    }

    private Iterator<Word<I>> generatePrefixSteps(A hypothesis) {
        List<S> states = new ArrayList<>(hypothesis.getStates());
        Collections.reverse(states);
        return new PrefixStepsIterator(states.iterator());
    }

    private Path<S, I> selectOptimalPath(Set<KWayTransition<S, I>> covered, Collection<Path<S, I>> paths) {
        Path<S, I> max = Collections.max(paths, optimizationMetric.getPathComparator(covered));
        return max.kWayTransitions.size() == covered.size() ? null : max;
    }

    public enum GenerationMethod {
        RANDOM {
            @Override
            <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                    KWayTransitionCoverTestsIterator<S, I, T, A> self) {
                return self.new GreedySetCoverIterator();
            }
        },
        PREFIX {
            @Override
            <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                    KWayTransitionCoverTestsIterator<S, I, T, A> self) {
                return self.generatePrefixSteps(self.automaton);
            }
        };

        abstract <S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> Iterator<Word<I>> getIterator(
                KWayTransitionCoverTestsIterator<S, I, T, A> self);
    }

    public enum OptimizationMetric {
        STEPS {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> ((double) computeSizeOfDiff(p.kWayTransitions, covered)) /
                                                       p.steps.size());
            }
        },
        QUERIES {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> p.kWayTransitions.size() - covered.size());
            }
        };

        abstract <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered);

        /**
         * Computes the size of the set difference without actually materializing the difference.
         *
         * @return size of the difference
         */
        private static <S, I> int computeSizeOfDiff(Set<KWayTransition<S, I>> transitions,
                                                    Set<KWayTransition<S, I>> covered) {
            int size = transitions.size();
            for (KWayTransition<S, I> t : covered) {
                if (transitions.contains(t)) {
                    size--;
                }
            }

            return size;
        }
    }

    private class GreedySetCoverIterator extends AbstractSimplifiedIterator<Word<I>> {

        private final Set<Path<S, I>> paths;
        private final int sizeOfUniverse;
        private final Set<KWayTransition<S, I>> covered;

        private int stepCount;

        GreedySetCoverIterator() {
            this.paths = generateRandomPaths(automaton);
            this.covered = new HashSet<>();
            this.stepCount = 0;
            this.sizeOfUniverse = automaton.getStates().size() * (int) Math.pow(alphabet.size(), k);
        }

        @Override
        protected boolean calculateNext() {
            if (sizeOfUniverse > covered.size()) {
                Path<S, I> path = selectOptimalPath(covered, paths);

                if (path != null) {
                    covered.addAll(path.kWayTransitions);
                    paths.remove(path);
                    stepCount += path.steps.size();
                    super.nextValue = path.steps;
                    return true;
                }

                if (paths.isEmpty()) {
                    Iterator<Word<I>> prefixIterator = generatePrefixSteps(automaton);
                    while (prefixIterator.hasNext()) {
                        Word<I> generatePrefixStep = prefixIterator.next();
                        paths.add(createPath(automaton, generatePrefixStep));
                    }
                }

                if (maxNumberOfSteps != 0 && stepCount > maxNumberOfSteps) {
                    return false;
                }
            }
            return false;
        }
    }

    private class PrefixStepsIterator extends AbstractTwoLevelIterator<S, List<I>, Word<I>> {

        private final APSPResult<S, TransitionEdge<I, T>> apsp;
        private final S initial;

        PrefixStepsIterator(Iterator<S> listIterator) {
            super(listIterator);
            this.apsp = Graphs.findAPSP(automaton.transitionGraphView(alphabet));
            this.initial = automaton.getInitialState();
        }

        @Override
        protected Iterator<List<I>> l2Iterator(S state) {
            Iterable<List<I>> lists = IterableUtil.allTuples(alphabet, k);
            return lists.iterator();
        }

        @Override
        protected Word<I> combine(S state, List<I> steps) {
            List<TransitionEdge<I, T>> prefix = apsp.getShortestPath(initial, state);
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

        private final S startState;
        private final S endState;
        private final Word<I> steps;

        KWayTransition(S startState, S endState, Word<I> steps) {
            this.startState = startState;
            this.endState = endState;
            this.steps = steps;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            KWayTransition<?, ?> that = (KWayTransition<?, ?>) o;
            return Objects.equals(startState, that.startState) && Objects.equals(endState, that.endState) &&
                   Objects.equals(steps, that.steps);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(startState);
            result = 31 * result + Objects.hashCode(endState);
            result = 31 * result + Objects.hashCode(steps);
            return result;
        }
    }

    private static final class Path<S, I> {

        private final S startState;
        private final S endState;
        private final Word<I> steps;
        private final Set<KWayTransition<S, I>> kWayTransitions;
        private final List<KWayTransition<S, I>> transitionsLog;

        Path(S startState,
             S endState,
             Word<I> steps,
             Set<KWayTransition<S, I>> kWayTransitions,
             List<KWayTransition<S, I>> transitionsLog) {
            this.startState = startState;
            this.endState = endState;
            this.steps = steps;
            this.kWayTransitions = kWayTransitions;
            this.transitionsLog = transitionsLog;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Path<?, ?> path = (Path<?, ?>) o;
            return Objects.equals(startState, path.startState) && Objects.equals(endState, path.endState) &&
                   Objects.equals(steps, path.steps) && Objects.equals(kWayTransitions, path.kWayTransitions) &&
                   Objects.equals(transitionsLog, path.transitionsLog);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(startState);
            result = 31 * result + Objects.hashCode(endState);
            result = 31 * result + Objects.hashCode(steps);
            result = 31 * result + Objects.hashCode(kWayTransitions);
            result = 31 * result + Objects.hashCode(transitionsLog);
            return result;
        }
    }
}
