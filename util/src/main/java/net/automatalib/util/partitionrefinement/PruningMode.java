package net.automatalib.util.partitionrefinement;

/**
 * Allows for controlling how automata are pruned during minimization.
 */
public enum PruningMode {
    /**
     * Prune the automaton <i>before</i> the computation of equivalent states. This might be more efficient if the
     * automaton contains a large number of unreachable states, as it reduces the number of states on which equivalence
     * needs to be computed. However, since the equivalence computation is practically extremely fast,
     * {@link #PRUNE_AFTER} is usually the better choice. This value, however, always guarantees a correct (i.e.,
     * minimal and initially connected) result.
     */
    PRUNE_BEFORE,
    /**
     * Prune after the computation of equivalent states. Since the number of equivalence classes is usually smaller than
     * the number of states of the original automaton, this usually is more efficient (unless the automaton contains
     * many unreachable states), and guarantees a correct result.
     */
    PRUNE_AFTER,
    /**
     * Do not prune at all. Note that if the automaton to minimize is not initially connected (i.e., there are states
     * which cannot be reached from the initial state), the returned automaton might or might not be initially
     * connected, meaning it is possibly non-minimal.
     */
    DONT_PRUNE
}
