package net.automatalib.util.fixedpoint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.words.impl.Alphabets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Closures {

    private Closures() {
        // prevent instantiation
    }

    public static <A extends UniversalAutomaton<S1, I, T1, SP1, TP1>, B extends MutableAutomaton<S2, I, T2, SP2, TP2>, S1, S2, I, T1, T2, SP1, SP2, TP1, TP2> Pair<Map<Set<S1>, S2>, B> simpleClosure(
            A ts,
            Collection<I> inputs,
            Collection<I> allInputs,
            AutomatonCreator<B, I> creator,
            TransitionPredicate<S1, I, T1> transitionFilter
    ) {
        return Worksets.map(new StateClosureAlgorithm<>(ts, inputs, creator, toClosureOperator(ts, allInputs, (s, i, t) -> ! transitionFilter.apply(s, i, t)), transitionFilter));
    }

    public static <A extends UniversalAutomaton<S1, I, T1, SP1, TP1>, B extends MutableAutomaton<S2, I, T2, SP2, TP2>, S1, S2, I, T1, T2, SP1, SP2, TP1, TP2> Pair<Map<Set<S1>, S2>, B> closure(
            A ts,
            Collection<I> inputs,
            AutomatonCreator<B, I> creator,
            Function<Set<S1>, Set<S1>> closureOperator,
            TransitionPredicate<S1, I, T1> transitionFilter
    ) {
        return Worksets.map(new StateClosureAlgorithm<>(ts, inputs, creator, closureOperator, transitionFilter));
    }


    /**
     * Creates a closure operator op: Set[S] -> Set[S] from an TransitionPredicate over the given transition system.
     *
     * The returned operator calculates the closure of a given set S by adding all states s'
     * to S which can be reached by at least one state of S trough a transition for which the predicate is true.
     * This step is repeated until stabilisation (closure semantics).
     *
     * @param ts
     * @param inputs
     * @param transitionFilter
     * @param <S>
     * @param <I>
     * @param <T>
     * @param <SP>
     * @param <TP>
     * @return
     */
    public static <S, I, T, SP, TP> Function<Set<S>, Set<S>> toClosureOperator(UniversalAutomaton<S, I, T, SP, TP> ts, Collection<I> inputs, TransitionPredicate<S, I, T> transitionFilter) {
        return new Function<Set<S>, Set<S>>() {
            @Override
            public Set<S> apply(Set<S> states) {
                Set<S> result = new HashSet<>(states);
                Deque<S> stack = new ArrayDeque<>(states);

                while (!stack.isEmpty())  {
                    S state = stack.pop();

                    for (I symbol : inputs) {
                        for (T transition : ts.getTransitions(state, symbol)) {
                            if (transitionFilter.apply(state, symbol, transition)) {
                                S successor = ts.getSuccessor(transition);
                                if (!result.contains(successor)) {
                                    result.add(successor);
                                    stack.push(successor);
                                }
                            }
                        }
                    }

                }
                return result;
            }
        };
    }

    private static final class StateClosureAlgorithm<A extends UniversalAutomaton<S1, I, T1, SP1, TP1>, B extends MutableAutomaton<S2, I, T2, SP2, TP2>, S1, S2, I, T1, T2, SP1, SP2, TP1, TP2> implements WorksetMappingAlgorithm<Set<S1>, S2, B> {

        private static final Logger LOGGER = LoggerFactory.getLogger(StateClosureAlgorithm.class);

        private final A inputTS;
        private final B result;
        private final Collection<I> inputs;
        private final Function<Set<S1>, Set<S1>> closureOperator;
        private final TransitionPredicate<S1, I, T1> transitionFilter;

        public StateClosureAlgorithm(A ts,
                                     Collection<I> inputs,
                                     AutomatonCreator<B, I> creator,
                                     Function<Set<S1>, Set<S1>> closureOperator,
                                     TransitionPredicate<S1, I, T1> transitionFilter) {

            this.inputTS = ts;
            this.inputs = inputs;
            this.result = creator.createAutomaton(Alphabets.fromCollection(inputs));
            this.closureOperator = closureOperator;
            this.transitionFilter = transitionFilter;
        }

        @Override
        public int expectedElementCount() {
            return inputTS.size();
        }

        @Override
        public Collection<Set<S1>> initialize(Map<Set<S1>, S2> mapping) {
            Set<S1> initialStateClosure = closureOperator.apply(inputTS.getInitialStates());
            S2 initialState = result.addInitialState();

            mapping.put(initialStateClosure, initialState);

            return Collections.singleton(initialStateClosure);
        }

        @Override
        public Collection<Set<S1>> update(Map<Set<S1>, S2> mapping, Set<S1> currentT) {

            List<Set<S1>> discovered = new ArrayList<>(currentT.size());

            for (I input : inputs) {

                Set<S1> reachable = new HashSet<>(currentT.size());

                for (S1 state : currentT) {
                    for (T1 transition : inputTS.getTransitions(state, input)) {
                        if (transitionFilter.apply(state, input, transition)) {
                            reachable.add(inputTS.getSuccessor(transition));
                        }
                    }
                }

                Set<S1> closure = closureOperator.apply(reachable);
                if (closure.isEmpty()) {
                    continue;
                }
                S2 mappedStated = mapping.get(closure);
                if (mappedStated == null) {
                    mappedStated = result.addState();
                    mapping.put(closure, mappedStated);
                    discovered.add(closure);
                }
                result.addTransition(mapping.get(currentT), input, mappedStated, null);
            }

            return discovered;
        }

        @Override
        public B result() {
            return result;
        }
    }

}
