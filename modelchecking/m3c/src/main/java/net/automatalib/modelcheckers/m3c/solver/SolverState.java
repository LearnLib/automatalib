package net.automatalib.modelcheckers.m3c.solver;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class SolverState<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    private final Map<L, List<T>> propTransformers;
    private final List<T> compositions;
    private final Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas;
    private final int updatedStateId;
    private final @Nullable L updatedStateMPG;
    private final Map<L, BitSet> workSet;

    SolverState(Map<L, List<T>> propTransformers,
                Map<L, BitSet> workSet,
                Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas) {
        this(propTransformers, Collections.emptyList(), -1, null, workSet, satisfiedSubformulas);
    }

    SolverState(Map<L, List<T>> propTransformers,
                List<T> compositions,
                int updatedStateId,
                @Nullable L updatedStateMPG,
                Map<L, BitSet> workSet,
                Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas) {
        this.propTransformers = propTransformers;
        this.compositions = compositions;
        this.updatedStateId = updatedStateId;
        this.updatedStateMPG = updatedStateMPG;
        this.workSet = workSet;
        this.satisfiedSubformulas = satisfiedSubformulas;
    }

    public Map<L, List<T>> getPropTransformers() {
        return propTransformers;
    }

    public List<T> getCompositions() {
        return compositions;
    }

    public int getUpdatedStateId() {
        return updatedStateId;
    }

    public @Nullable L getUpdatedStateMPG() {
        return updatedStateMPG;
    }

    public Map<L, BitSet> getWorkSet() {
        return workSet;
    }

    public Map<L, List<List<FormulaNode<L, AP>>>> getSatisfiedSubformulas() {
        return satisfiedSubformulas;
    }

}
