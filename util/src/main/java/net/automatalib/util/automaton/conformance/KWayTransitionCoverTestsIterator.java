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
import net.automatalib.util.graph.Graphs;
import net.automatalib.util.graph.apsp.APSPResult;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class KWayTransitionCoverTestsIterator<S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>>
        implements Iterator<Word<I>> {

    private final A automaton;
    private final List<? extends I> alphabet;
    private final Random random;
    private final int k;
    private final int numGeneratePaths;
    private final int maxPathLen;
    private final int maxNumberOfSteps;
    private final Optimize optimize;
    private final int randomWalkLen;

    // Cached paths for reuse
    private final List<Path<S, I>> cachedPaths = new ArrayList<>();

    private final Iterator<Word<I>> iterator;

    public KWayTransitionCoverTestsIterator(A automaton, Collection<? extends I> inputs) {
        this(automaton, inputs, new Random());
    }

    public KWayTransitionCoverTestsIterator(A automaton, Collection<? extends I> inputs, Random random) {
        this(automaton, inputs, random, 2, Method.Random, 1000, 50, 0, Optimize.Steps, 10);
    }

    public KWayTransitionCoverTestsIterator(A automaton,
                                            Collection<? extends I> inputs,
                                            Random random,
                                            int k,
                                            Method method,
                                            int numGeneratePaths,
                                            int maxPathLen,
                                            int maxNumberOfSteps,
                                            Optimize optimize,
                                            int randomWalkLen) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.random = random;

        this.k = k;
        this.numGeneratePaths = numGeneratePaths;
        this.maxPathLen = maxPathLen;
        this.maxNumberOfSteps = maxNumberOfSteps;
        this.optimize = optimize;
        this.randomWalkLen = randomWalkLen;

        switch (method) {
            case Prefix:
                this.iterator = generatePrefixSteps(automaton).iterator();
                break;
            case Random:
                this.iterator = new GreedySetCoverIterator();
                break;
            default:
                throw new IllegalArgumentException("Unknown method " + method);
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
            Word<I> steps = KWayStateCoverTestsIterator.getRandomChoices(alphabet, randomLength, random);
            Path<S, I> path = createPath(hypothesis, steps);
            result.add(path);
        }

        return result;
    }

    /**
     * * Creates Path object from provided automaton steps within specified Hypothesis context.** * *@return Newly
     * created Path instance corresponding to provided step sequence.**
     **/
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

    public Iterable<Word<I>> generatePrefixSteps(A hypothesis) {
        List<S> states = new ArrayList<>(hypothesis.getStates());
        Collections.reverse(states);
        return () -> new PrefixStepsIterator(states.iterator());
    }

    /**
     * Selects an optimal path from available candidates based on optimization strategy defined.
     *
     * @return Selected Path object or null if no suitable path is found.
     **/
    private Path<S, I> selectOptimalPath(Set<KWayTransition<S, I>> covered, Collection<Path<S, I>> paths) {
        Path<S, I> max = Collections.max(paths, optimize.getPathComparator(covered));
        return max.kWayTransitions.size() != covered.size() ? max : null;
    }

    private static <I> List<I> getRandomChoices(List<? extends I> alphabet, int count, Random random) {
        List<I> choices = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            choices.add(alphabet.get(random.nextInt(alphabet.size())));
        }

        return choices;
    }

    public enum Method {
        Random,
        Prefix
    }

    public enum Optimize {
        Steps {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> ((double) (p.kWayTransitions.size() - covered.size())) /
                                                       p.steps.size());
            }
        },
        Queries {
            @Override
            <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered) {
                return Comparator.comparingDouble(p -> (p.kWayTransitions.size() - covered.size()));
            }
        };

        abstract <S, I> Comparator<Path<S, I>> getPathComparator(Set<KWayTransition<S, I>> covered);
    }

    private class GreedySetCoverIterator extends AbstractSimplifiedIterator<Word<I>> {

        private final Set<Path<S, I>> paths;
        private final int sizeOfUniverse;
        private final Set<KWayTransition<S, I>> covered;

        private int stepCount;

        public GreedySetCoverIterator() {
            this.paths = generateRandomPaths(automaton);
            this.paths.addAll(cachedPaths);
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
//                    result.add(path);
                    super.nextValue = path.steps;
                    return true;
                }

                if (paths.isEmpty()) {
                    for (Word<I> generatePrefixStep : generatePrefixSteps(automaton)) {
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

        public PrefixStepsIterator(Iterator<S> listIterator) {
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
            wb.addAll(getRandomChoices(alphabet, randomWalkLen, random));

            return wb.toWord();
        }
    }

    private static final class KWayTransition<S, I> {

        private final S startState;
        private final S endState;
        private final Word<I> steps;

        private KWayTransition(S startState, S endState, Word<I> steps) {
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

        private Path(S startState,
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
