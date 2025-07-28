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
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.graph.Graphs;
import net.automatalib.util.graph.apsp.APSPResult;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class KWayStateCoverTestsIterator<S, I, T, A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>>
        implements Iterator<Word<I>> {

    private final A automaton;
    private final List<? extends I> alphabet;
    private final Random random;
    private final int k;
    private final int randomWalkLen;
    private final CombinationMethod method;

    private final Iterator<Word<I>> iterator;

    public KWayStateCoverTestsIterator(A automaton, Collection<? extends I> inputs) {
        this(automaton, inputs, new Random());
    }

    public KWayStateCoverTestsIterator(A automaton, Collection<? extends I> inputs, Random random) {
        this(automaton, inputs, random, 2, 20, CombinationMethod.Permutations);
    }

    public KWayStateCoverTestsIterator(A automaton,
                                       Collection<? extends I> inputs,
                                       Random random,
                                       int k,
                                       int randomWalkLen,
                                       CombinationMethod method) {
        this.automaton = automaton;
        this.alphabet = CollectionUtil.randomAccessList(inputs);
        this.k = k;
        this.randomWalkLen = randomWalkLen;
        this.method = method;
        this.random = random;

        if (automaton.size() == 0) {
            this.iterator = Collections.emptyIterator();
        } else {
            final FirstPhaseIterator firstIterator = new FirstPhaseIterator();
            final SecondPhaseIterator secondPhaseIterator = new SecondPhaseIterator();
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

    static <I> Word<I> getRandomChoices(List<? extends I> alphabet, int count, Random random) {
        WordBuilder<I> choices = new WordBuilder<>(count);

        for (int i = 0; i < count; i++) {
            choices.add(alphabet.get(random.nextInt(alphabet.size())));
        }

        return choices.toWord();
    }

    /**
     * Performs random walks if the automaton only has a single state.
     */
    private final class FirstPhaseIterator extends AbstractSimplifiedIterator<Word<I>> {

        private int idx = 0;

        @Override
        protected boolean calculateNext() {
            if (automaton.size() == 1) {
                if (idx++ < randomWalkLen) {
                    super.nextValue = getRandomChoices(alphabet, randomWalkLen, random);
                    return true;
                }
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

        private APSPResult<S, TransitionEdge<I, T>> apsp;

        public SecondPhaseIterator() {
            List<S> states = new ArrayList<>(automaton.getStates());
            Collections.shuffle(states, random);
            this.combIter = method.getCombinations(states, k);
            this.cache = new HashSet<>();
        }

        @Override
        protected boolean calculateNext() {

            final S initial = automaton.getInitialState();
            final APSPResult<S, TransitionEdge<I, T>> apsp = getAPSP();

            while (combIter.hasNext()) {
                final List<S> comb = combIter.next();
                final Set<List<TransitionEdge<I, T>>> prefixes = new HashSet<>(HashUtil.capacity(comb.size()));

                for (S c : comb) {
                    prefixes.add(apsp.getShortestPath(initial, c));
                }

                if (!cache.add(prefixes)) {
                    continue;
                }

                final List<TransitionEdge<I, T>> firstPath = apsp.getShortestPath(initial, comb.get(0));
                assert firstPath != null;

                final WordBuilder<I> pathBuilder = new WordBuilder<>();
                for (TransitionEdge<I, T> e : firstPath) {
                    pathBuilder.add(e.getInput());
                }

                /*
                 * in case of non-strongly connected automata test case might not be possible as a path between 2 states
                 * might not exist
                 */
                boolean possibleTestCase = true;
                for (int index = 0; index < comb.size() - 1; index++) {
                    final List<? extends TransitionEdge<I, ?>> pathBetweenStates =
                            apsp.getShortestPath(comb.get(index), comb.get(index + 1));

                    if (pathBetweenStates == null || pathBetweenStates.isEmpty()) {
                        possibleTestCase = false;
                        break;
                    }

                    for (TransitionEdge<I, ?> pathBetweenState : pathBetweenStates) {
                        pathBuilder.append(pathBetweenState.getInput());
                    }
                }

                if (!possibleTestCase) {
                    continue;
                }

                // Add random walk at the end
                for (I p : getRandomChoices(alphabet, randomWalkLen, random)) {
                    pathBuilder.append(p);
                }

                super.nextValue = pathBuilder.toWord();
                return true;
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

    public enum CombinationMethod {
        Combinations {
            @Override
            <S> Iterator<List<S>> getCombinations(List<S> states, int k) {
                throw new NoSuchMethodError("TODO");
            }
        },
        Permutations {
            @Override
            <S> Iterator<List<S>> getCombinations(List<S> states, int k) {
                return IterableUtil.allTuples(states, k).iterator();
            }
        };

        abstract <S> Iterator<List<S>> getCombinations(List<S> states, int k);
    }
}
