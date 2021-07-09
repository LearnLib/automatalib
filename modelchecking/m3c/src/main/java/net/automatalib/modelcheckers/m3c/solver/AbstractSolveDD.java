/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.concepts.NodeIDs;
import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DependencyGraph;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.visitor.CTLToMuCalc;
import net.automatalib.modelcheckers.m3c.transformer.AbstractPropertyTransformer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;
import net.automatalib.ts.modal.transition.ProceduralModalEdgeProperty;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractSolveDD<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    // Attributes that are constant for a given CFPS
    private final @KeyFor("workUnits") L mainProcess;

    // Attributes that change for each formula
    private DependencyGraph<L, AP> dependencyGraph;
    private int currentBlockIndex;

    // Per-procedure attributes
    private final Map<L, WorkUnit<?, ?>> workUnits;
    // Per-action attributes
    private Map<L, T> mustTransformers;
    private Map<L, T> mayTransformers;

    AbstractSolveDD(ModalContextFreeProcessSystem<L, AP> mcfps) {
        final Map<L, ModalProcessGraph<?, L, ?, AP, ?>> mpgs = mcfps.getMPGs();

        this.workUnits = Maps.newHashMapWithExpectedSize(mpgs.size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            final L mpgLabel = labelMpg.getKey();
            final ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();

            if (mpg.getInitialNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no start state.");
            }
            if (mpg.getFinalNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no end state.");
            }

            workUnits.put(mpgLabel, initializeWorkUnits(mpgLabel, mpg));
        }

        final L mainProcess = mcfps.getMainProcess();
        if (!workUnits.containsKey(mainProcess)) {
            throw new IllegalArgumentException("The main process has no corresponding MPG.");
        }

        this.mainProcess = mainProcess;
    }

    public boolean solve(FormulaNode<L, AP> formula) {
        this.solveInternal(formula, false, Collections.emptyList());

        final boolean sat = isSat();
        shutdownDDManager();
        return sat;
    }

    public SolverHistory<T, L, AP> solveAndRecordHistory(FormulaNode<L, AP> formula) {
        final List<SolverState<T, L, AP>> history = new ArrayList<>();
        this.solveInternal(formula, true, history);

        // TODO simplify?
        final Map<L, NodeIDs<?>> nodeIDs = Maps.newHashMapWithExpectedSize(this.workUnits.size());
        for (Entry<L, WorkUnit<?, ?>> e : this.workUnits.entrySet()) {
            nodeIDs.put(e.getKey(), e.getValue().mpg.nodeIDs());
        }

        shutdownDDManager();
        return new SolverHistory<>(nodeIDs, mustTransformers, mayTransformers, history);
    }

    private void solveInternal(FormulaNode<L, AP> formula, boolean recordHistory, List<SolverState<T, L, AP>> history) {
        FormulaNode<L, AP> ast = ctlToMuCalc(formula).toNNF();

        initialize(ast);

        if (recordHistory) {
            history.add(new SolverState<>(copyPropTransformers(), copyWorkSet(), computeSatisfiedSubformulas()));
        }

        boolean workSetIsEmpty = false;
        while (!workSetIsEmpty) {
            workSetIsEmpty = true;
            for (Entry<L, WorkUnit<?, ?>> entry : workUnits.entrySet()) {
                workSetIsEmpty &= solveInternal(entry.getValue(), recordHistory, history);
            }
        }
    }

    private <N> boolean solveInternal(WorkUnit<N, ?> unit, boolean recordHistory, List<SolverState<T, L, AP>> history) {
        if (!unit.workSet.isEmpty()) {
            final Iterator<N> iter = unit.workSet.iterator();
            final N node = iter.next();
            iter.remove();

            final L mpgLabel = unit.label;
            final List<T> compositions = updatedStateAndGetCompositions(unit, node);

            if (recordHistory) {
                //TODO: simplify?
                int nodeId = unit.mpg.nodeIDs().getNodeId(node);
                history.add(new SolverState<>(copyPropTransformers(),
                                              compositions,
                                              nodeId,
                                              mpgLabel,
                                              copyWorkSet(),
                                              computeSatisfiedSubformulas()));
            }

            return false;
        }

        return true;
    }

    private FormulaNode<L, AP> ctlToMuCalc(FormulaNode<L, AP> ctlFormula) {
        CTLToMuCalc<L, AP> transformation = new CTLToMuCalc<>();
        return transformation.toMuCalc(ctlFormula);
    }

    private Map<L, List<T>> copyPropTransformers() {
        final Map<L, List<T>> copy = Maps.newHashMapWithExpectedSize(workUnits.size());

        for (Map.Entry<L, WorkUnit<?, ?>> e : workUnits.entrySet()) {
            copy.put(e.getKey(), copyPropTransformers(e.getValue()));
        }

        return copy;
    }

    private <N> List<T> copyPropTransformers(WorkUnit<N, ?> unit) {

        final ModalProcessGraph<N, L, ?, AP, ?> mpg = unit.mpg;
        final NodeIDs<N> nodeIDs = mpg.nodeIDs();

        final List<T> result = new ArrayList<>(mpg.size());

        for (int i = 0; i < mpg.size(); i++) {
            result.add(unit.propTransformers.get(nodeIDs.getNode(i)));
        }

        return result;
    }

    private Map<L, BitSet> copyWorkSet() {
        final Map<L, BitSet> copy = Maps.newHashMapWithExpectedSize(workUnits.size());

        for (Map.Entry<L, WorkUnit<?, ?>> e : workUnits.entrySet()) {
            copy.put(e.getKey(), copyWorkSet(e.getValue()));
        }

        return copy;
    }

    private <N> BitSet copyWorkSet(WorkUnit<N, ?> unit) {

        final ModalProcessGraph<N, L, ?, AP, ?> mpg = unit.mpg;
        final NodeIDs<N> nodeIDs = mpg.nodeIDs();

        final BitSet result = new BitSet(mpg.size());

        for (int i = 0; i < mpg.size(); i++) {
            if (unit.workSet.contains(nodeIDs.getNode(i))) {
                result.set(i);
            }
        }

        return result;
    }

    private Map<L, List<List<FormulaNode<L, AP>>>> computeSatisfiedSubformulas() {
        final Map<L, List<List<FormulaNode<L, AP>>>> result = Maps.newHashMapWithExpectedSize(workUnits.size());

        for (Map.Entry<L, WorkUnit<?, ?>> e : workUnits.entrySet()) {
            result.put(e.getKey(), computeSatisfiedSubformulas(e.getValue()));
        }

        return result;
    }

    private <N> List<List<FormulaNode<L, AP>>> computeSatisfiedSubformulas(WorkUnit<N, ?> unit) {

        final ModalProcessGraph<N, L, ?, AP, ?> mpg = unit.mpg;
        final NodeIDs<N> nodeIDs = mpg.nodeIDs();

        final List<List<FormulaNode<L, AP>>> result = new ArrayList<>(mpg.size());

        for (int i = 0; i < mpg.size(); i++) {
            result.add(getSatisfiedSubformulas(unit, nodeIDs.getNode(i)));
        }

        return result;
    }

    private <N> WorkUnit<N, ?> initializeWorkUnits(@UnderInitialization AbstractSolveDD<T, L, AP> this,
                                                   L label,
                                                   ModalProcessGraph<N, L, ?, AP, ?> mpg) {
        return new WorkUnit<>(label, mpg, initPredecessorsMapping(mpg));
    }

    private static <N, L, E, AP> Mapping<N, @Nullable Set<N>> initPredecessorsMapping(ModalProcessGraph<N, L, E, AP, ?> mpg) {

        final MutableMapping<N, @Nullable Set<N>> nodeToPredecessors = mpg.createStaticNodeMapping();

        for (N sourceNode : mpg.getNodes()) {
            for (E outgoingEdge : mpg.getOutgoingEdges(sourceNode)) {
                N targetNode = mpg.getTarget(outgoingEdge);
                Set<N> nodePredecessors = nodeToPredecessors.get(targetNode);

                if (nodePredecessors == null) {
                    nodePredecessors = new HashSet<>();
                    nodeToPredecessors.put(targetNode, nodePredecessors);
                }

                nodePredecessors.add(sourceNode);
            }
        }

        return nodeToPredecessors;
    }

    private void initialize(FormulaNode<L, AP> ast) {
        this.dependencyGraph = new DependencyGraph<>(ast);
        this.currentBlockIndex = dependencyGraph.getBlocks().size() - 1;

        initDDManager(this.dependencyGraph);

        this.mustTransformers = new HashMap<>();
        this.mayTransformers = new HashMap<>();
        for (WorkUnit<?, ?> unit : workUnits.values()) {
            initialize(unit);
        }
    }

    private <N> void initialize(WorkUnit<N, ?> unit) {
        unit.workSet = newWorkSet(unit.mpg);
        unit.propTransformers = initTransformers(unit.mpg);
    }

    private void resetWorkSet() {
        for (WorkUnit<?, ?> value : this.workUnits.values()) {
            resetWorkSet(value);
        }
    }

    private <N> void resetWorkSet(WorkUnit<N, ?> unit) {
        unit.workSet = newWorkSet(unit.mpg);
    }

    private <N> Set<N> newWorkSet(ModalProcessGraph<N, L, ?, AP, ?> mpg) {
        // Add all states to work set except final state, which is never updated
        Set<N> workset = new HashSet<>(mpg.getNodes());
        workset.remove(mpg.getFinalNode());

        return workset;
    }

    private <N> MutableMapping<N, T> initTransformers(ModalProcessGraph<N, L, ?, AP, ?> mpg) {
        MutableMapping<N, @Nullable T> transformers = mpg.createStaticNodeMapping();
        N finalNode = mpg.getFinalNode();

        for (N n : mpg) {
            if (Objects.equals(n, finalNode)) {
                transformers.put(n, createInitTransformerEnd(dependencyGraph));
            } else {
                transformers.put(n, createInitState(dependencyGraph));
            }
        }
        return (MutableMapping<N, T>) transformers; // we put a transformer for every node, so it's no longer null
    }

    private boolean isSat() {
        return isSat(workUnits.get(mainProcess));
    }

    private <N> boolean isSat(WorkUnit<N, ?> unit) {
        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the constructor
        @NonNull N initialNode = unit.mpg.getInitialNode();
        return isSat(unit, initialNode);
    }

    private <N> boolean isSat(WorkUnit<N, ?> unit, N initialNode) {
        List<FormulaNode<L, AP>> satisfiedFormulas = getSatisfiedSubformulas(unit, initialNode);
        for (FormulaNode<L, AP> node : satisfiedFormulas) {
            if (node.getVarNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    private <N> List<FormulaNode<L, AP>> getSatisfiedSubformulas(WorkUnit<N, ?> unit, N node) {
        Set<Integer> output = unit.propTransformers.get(node).evaluate(toBoolArray(getAllAPDeadlockedState()));
        List<FormulaNode<L, AP>> satisfiedSubFormulas = new ArrayList<>();
        for (FormulaNode<L, AP> n : dependencyGraph.getFormulaNodes()) {
            if (output.contains(n.getVarNumber())) {
                satisfiedSubFormulas.add(n);
            }
        }
        return satisfiedSubFormulas;
    }

    private boolean[] toBoolArray(Set<Integer> satisfiedVars) {
        boolean[] arr = new boolean[dependencyGraph.getNumVariables()];
        for (Integer satisfiedVar : satisfiedVars) {
            arr[satisfiedVar] = true;
        }
        return arr;
    }

    private Set<Integer> getAllAPDeadlockedState() {
        return getAllAPDeadlockedState(workUnits.get(mainProcess).mpg);
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> Set<Integer> getAllAPDeadlockedState(ModalProcessGraph<N, L, E, AP, TP> mainMpg) {
        Set<Integer> satisfiedVariables = new HashSet<>();
        @SuppressWarnings("nullness") // we have checked non-nullness of final nodes in the constructor
        @NonNull N finalNode = mainMpg.getFinalNode();
        for (int blockIdx = dependencyGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            EquationalBlock<L, AP> block = dependencyGraph.getBlock(blockIdx);
            for (FormulaNode<L, AP> node : block.getNodes()) {
                if (node instanceof TrueNode) {
                    satisfiedVariables.add(node.getVarNumber());
                } else if (node instanceof AtomicNode) {
                    Set<AP> atomicPropositions = ((AtomicNode<L, AP>) node).getPropositions();
                    Set<AP> finalNodeAPs = mainMpg.getAtomicPropositions(finalNode);
                    if (finalNodeAPs.containsAll(atomicPropositions)) {
                        satisfiedVariables.add(node.getVarNumber());
                    }
                } else if (node instanceof BoxNode) {
                    /* End State has no outgoing edges */
                    satisfiedVariables.add(node.getVarNumber());
                } else if (node instanceof AndNode) {
                    if (satisfiedVariables.contains(node.getVarNumberLeft()) &&
                        satisfiedVariables.contains(node.getVarNumberRight())) {
                        satisfiedVariables.add(node.getVarNumber());
                    }
                } else if (node instanceof OrNode) {
                    if (satisfiedVariables.contains(node.getVarNumberLeft()) ||
                        satisfiedVariables.contains(node.getVarNumberRight())) {
                        satisfiedVariables.add(node.getVarNumber());
                    }
                } else if (node instanceof NotNode && !satisfiedVariables.contains(node.getVarNumberLeft())) {
                    satisfiedVariables.add(node.getVarNumber());
                }
            }
        }
        return satisfiedVariables;
    }

    private <N> List<T> updatedStateAndGetCompositions(WorkUnit<N, ?> unit, N node) {
        initUpdate(unit, node);
        T stateTransformer = getTransformer(unit, node);
        List<T> compositions = createCompositions(unit, node);
        T updatedTransformer = getUpdatedPropertyTransformer(unit, node, stateTransformer, compositions);
        updateTransformerAndWorkSet(unit, node, stateTransformer, updatedTransformer);
        return compositions;
    }

    private <N> void updateTransformerAndWorkSet(WorkUnit<N, ?> unit,
                                                 N node,
                                                 T stateTransformer,
                                                 T updatedTransformer) {
        if (!stateTransformer.equals(updatedTransformer)) {
            unit.propTransformers.put(node, updatedTransformer);
            updateWorkSet(unit, node);
        }
        if (workSetIsEmpty() && currentBlockIndex > 0) {
            currentBlockIndex--;
            resetWorkSet();
        }
    }

    private <N> void updateWorkSet(WorkUnit<N, ?> unit, N node) {
        if (Objects.equals(unit.mpg.getInitialNode(), node)) {
            updateWorkSetStartState(unit.label);
        }
        addPredecessorsToWorkSet(unit, node);
    }

    private boolean workSetIsEmpty() {
        for (WorkUnit<?, ?> unit : workUnits.values()) {
            if (!unit.workSet.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private <N> void addPredecessorsToWorkSet(WorkUnit<N, ?> unit, N node) {
        Set<N> preds = unit.predecessors.get(node);
        if (preds != null) {
            for (N pred : preds) {
                addToWorkSet(unit, pred);
            }
        }
    }

    private void updateWorkSetStartState(L mpgLabel) {
        for (WorkUnit<?, ?> unit : workUnits.values()) {
            updateWorkSetStartState(unit, mpgLabel);
        }
    }

    private <N, E> void updateWorkSetStartState(WorkUnit<N, E> unit, L mpgLabelOfUpdatedProcess) {
        final ModalProcessGraph<N, L, E, AP, ?> mpg = unit.mpg;
        for (N node : mpg) {
            for (E outgoingEdge : mpg.getOutgoingEdges(node)) {
                if (Objects.equals(mpg.getEdgeLabel(outgoingEdge), mpgLabelOfUpdatedProcess)) {
                    addToWorkSet(unit, node);
                }
            }
        }
    }

    private <N> void initUpdate(WorkUnit<N, ?> unit, N node) {
        assert !Objects.equals(node, unit.mpg.getFinalNode()) : "End State must not be updated!";
        unit.workSet.remove(node);
    }

    private <E> T getEdgeTransformer(WorkUnit<?, E> unit, E edge) {
        T edgeTransformer;
        ModalProcessGraph<?, L, E, AP, ?> mpg = unit.mpg;
        L edgeLabel = mpg.getEdgeLabel(edge);
        if (isProcessEdge(mpg, edge)) {
            final WorkUnit<?, ?> edgeUnit = workUnits.get(edgeLabel);
            assert edgeUnit != null;
            edgeTransformer = getInitialEdgeTransformer(edgeUnit);
        } else {
            if (isMustEdge(mpg, edge)) {
                if (mustTransformers.containsKey(edgeLabel)) {
                    edgeTransformer = mustTransformers.get(edgeLabel);
                } else {
                    edgeTransformer = createInitTransformerEdge(dependencyGraph, edgeLabel, mpg.getEdgeProperty(edge));
                    mustTransformers.put(edgeLabel, edgeTransformer);
                }
            } else {
                if (mayTransformers.containsKey(edgeLabel)) {
                    edgeTransformer = mayTransformers.get(edgeLabel);
                } else {
                    edgeTransformer = createInitTransformerEdge(dependencyGraph, edgeLabel, mpg.getEdgeProperty(edge));
                    mayTransformers.put(edgeLabel, edgeTransformer);
                }
            }
        }
        return edgeTransformer;
    }

    private <N> T getInitialEdgeTransformer(WorkUnit<N, ?> unit) {
        @SuppressWarnings("nullness") // we have checked non-nullness of initial nodes in the constructor
        @NonNull N initialNode = unit.mpg.getInitialNode();
        return unit.propTransformers.get(initialNode);
    }

    private <E> boolean isProcessEdge(ModalProcessGraph<?, L, E, AP, ?> mpg, E edge) {
        return mpg.getEdgeProperty(edge).isProcess();
    }

    private <E> boolean isMustEdge(ModalProcessGraph<?, L, E, AP, ?> mpg, E edge) {
        return mpg.getEdgeProperty(edge).isMust();
    }

    private <N> T getUpdatedPropertyTransformer(WorkUnit<N, ?> unit, N node, T stateTransformer, List<T> compositions) {
        EquationalBlock<L, AP> currentBlock = dependencyGraph.getBlock(currentBlockIndex);
        Set<AP> atomicPropositions = unit.mpg.getAtomicPropositions(node);
        return stateTransformer.createUpdate(atomicPropositions, compositions, currentBlock);
    }

    private <N, E> List<T> createCompositions(WorkUnit<N, E> unit, N node) {
        ModalProcessGraph<N, L, E, AP, ?> mpg = unit.mpg;
        List<T> compositions = new ArrayList<>();
        for (E edge : mpg.getOutgoingEdges(node)) {
            N targetNode = mpg.getTarget(edge);
            T edgeTransformer = getEdgeTransformer(unit, edge);
            T succTransformer = getTransformer(unit, targetNode);
            T composition = edgeTransformer.compose(succTransformer);
            composition.setIsMust(isMustEdge(mpg, edge));
            compositions.add(composition);
        }
        return compositions;
    }

    private <N> T getTransformer(WorkUnit<N, ?> unit, N node) {
        return unit.propTransformers.get(node);
    }

    private <N> void addToWorkSet(WorkUnit<N, ?> unit, N node) {
        unit.workSet.add(node);
    }

    protected abstract void initDDManager(DependencyGraph<L, AP> dependencyGraph);

    protected abstract T createInitTransformerEnd(DependencyGraph<L, AP> dependencyGraph);

    protected abstract T createInitState(DependencyGraph<L, AP> dependencyGraph);

    protected abstract <TP extends ModalEdgeProperty> T createInitTransformerEdge(DependencyGraph<L, AP> dependencyGraph,
                                                                                  L edgeLabel,
                                                                                  TP edgeProperty);

    protected abstract void shutdownDDManager();

    private class WorkUnit<N, E> {

        private final L label;
        private final ModalProcessGraph<N, L, E, AP, ?> mpg;
        private final Mapping<N, @Nullable Set<N>> predecessors;
        private MutableMapping<N, T> propTransformers;
        private Set<N> workSet; // Keeps track of which state's property transformers have to be updated.

        WorkUnit(L label, ModalProcessGraph<N, L, E, AP, ?> mpg, Mapping<N, @Nullable Set<N>> predecessors) {
            this.label = label;
            this.mpg = mpg;
            this.predecessors = predecessors;
        }
    }

}
