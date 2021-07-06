package net.automatalib.modelcheckers.m3c.solver;

import java.util.List;
import java.util.Map;

import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;

public final class SolverHistory<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, NodeIDs<?>> nodeIDs;
    private final Map<L, T> mustTransformers;
    private final Map<L, T> mayTransformers;
    private final List<SolverState<T, L, AP>> solverStates;

    SolverHistory(Map<L, NodeIDs<?>> nodeIDs,
                  Map<L, T> mustTransformers,
                  Map<L, T> mayTransformers,
                  List<SolverState<T, L, AP>> solverStates) {
        this.nodeIDs = nodeIDs;
        this.mustTransformers = mustTransformers;
        this.mayTransformers = mayTransformers;
        this.solverStates = solverStates;
    }

    public List<SolverState<T, L, AP>> getSolverStates() {
        return solverStates;
    }

    public Map<L, T> getMustTransformers() {
        return mustTransformers;
    }

    public Map<L, T> getMayTransformers() {
        return mayTransformers;
    }

    public Map<L, NodeIDs<?>> getNodeIDs() {
        return nodeIDs;
    }

    //TODO boolean isSat();

}
