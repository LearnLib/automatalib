package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalFiniteAlphabetAutomaton;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.TransitionPredicate;
import net.automatalib.util.fixedpoint.Closures;

public class Subgraphs {

    private Subgraphs() {
        // prevent instantiation
    }

    public enum SubgraphType {
        DISREGARD_UNKNOWN_LABELS,
        HIDE_UNKNOWN_LABELS
    }

    private static <S, I, T> TransitionPredicate<S, I, T> transitionFilter(SubgraphType strategy, Collection<I> inputs) {
        return (S s, I i, T t) -> {
            if (strategy == SubgraphType.DISREGARD_UNKNOWN_LABELS) {
                return false;
            } else if (strategy == SubgraphType.HIDE_UNKNOWN_LABELS) {
                return !inputs.contains(i);
            }
            throw new IllegalStateException("Strategy unknown: " + strategy);
        };
    }

    /**
     * Returns the subgraph of ts with labels from inputs.
     *
     * Creates a new instance of creator and copies ts into it. All symbols not in inputs are
     * handled according to strategy.
     *
     */
    public static <A extends MutableAutomaton<S1, I, T1, SP1, TP1>, B extends UniversalFiniteAlphabetAutomaton<S2, I, T2, SP2, TP2>, S1, I, T1, SP1, TP1, S2, T2, SP2, TP2> Pair<Map<Set<S2>, S1>, A> subgraphView(
            AutomatonCreator<A, I> creator,
            SubgraphType type,
            B ts,
            Collection<I> inputs
    ) {


        Pair<Map<Set<S2>, S1>, A> closure = Closures.closure(ts,
                                                             inputs,
                                                             creator,
                                                             Closures.toClosureOperator(ts,
                                                                                        ts.getInputAlphabet(),
                                                                                        transitionFilter(type, inputs)),
                                                             (s, i, t) -> inputs.contains(i));

        return closure;
    }
}
