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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
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
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;

abstract class AbstractSolveDD<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    // Attributes that are constant for a given CFPS
    private final Map<L, ModalProcessGraph<?, L, ?, AP, ?>> mpgs;
    private final @KeyFor("mpgs") L mainProcess;
    private final Map<L, NodeIDs<?>> nodeIDs;

    // Attributes that change for each formula
    protected DependencyGraph<L, AP> dependGraph;
    private int currentBlockIndex;

    // Per-procedure attributes
    private Map<L, List<T>> propTransformers;
    private Map<L, Map<Integer, Set<Integer>>> predecessors;
    private Map<L, BitSet> workSet; // Keeps track of which state's property transformers have to be updated.
    // Per-action attributes
    private Map<L, T> mustTransformers;
    private Map<L, T> mayTransformers;

    AbstractSolveDD(ModalContextFreeProcessSystem<L, AP> mcfps) {
        this.mpgs = mcfps.getMPGs();

        final L mainProcess = mcfps.getMainProcess();
        if (!this.mpgs.containsKey(mainProcess)) {
            throw new IllegalArgumentException("The main process has no corresponding MPG.");
        }
        this.mainProcess = mainProcess;

        this.nodeIDs = Maps.newHashMapWithExpectedSize(this.mpgs.size());

        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            final L mpgLabel = labelMpg.getKey();
            final ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();

            if (mpg.getInitialNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no start state.");
            }
            if (mpg.getFinalNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no end state.");
            }

            nodeIDs.put(mpgLabel, mpg.nodeIDs());
        }
    }

    public boolean solve(FormulaNode<L, AP> formula) {
        this.solveInternal(formula, false, Collections.emptyList());
        return isSat();
    }

    public SolverHistory<T, L, AP> solveAndRecordHistory(FormulaNode<L, AP> formula) {
        final List<SolverState<T, L, AP>> history = new ArrayList<>();
        this.solveInternal(formula, true, history);
        return new SolverHistory<>(nodeIDs, mustTransformers, mayTransformers, history);
    }

    private void solveInternal(FormulaNode<L, AP> formula, boolean recordHistory, List<SolverState<T, L, AP>> history) {
        FormulaNode<L, AP> ast = ctlToMuCalc(formula).toNNF();

        initialize(ast);

        if (recordHistory) {
            history.add(new SolverState<>(getPropTransformersCopy(), getWorkSetCopy(), computeSatisfiedSubformulas()));
        }

        boolean workSetIsEmpty = false;
        while (!workSetIsEmpty) {
            workSetIsEmpty = true;
            for (Entry<L, BitSet> entry : workSet.entrySet()) {
                final BitSet mpgWorkSet = entry.getValue();
                if (!mpgWorkSet.isEmpty()) {
                    workSetIsEmpty = false;
                    final int nodeId = mpgWorkSet.nextSetBit(0);
                    final L mpgLabel = entry.getKey();
                    final List<T> compositions = updatedStateAndGetCompositions(nodeId, mpgLabel);

                    if (recordHistory) {
                        history.add(new SolverState<>(getPropTransformersCopy(),
                                                      compositions,
                                                      nodeId,
                                                      mpgLabel,
                                                      getWorkSetCopy(),
                                                      computeSatisfiedSubformulas()));
                    }
                }
            }
        }
    }

    private FormulaNode<L, AP> ctlToMuCalc(FormulaNode<L, AP> ctlFormula) {
        CTLToMuCalc<L, AP> transformation = new CTLToMuCalc<>();
        return transformation.toMuCalc(ctlFormula);
    }

    private Map<L, List<T>> getPropTransformersCopy() {
        //TODO: only create copy of updated state
        Map<L, List<T>> propTransformersCopy = Maps.newHashMapWithExpectedSize(propTransformers.size());
        for (Map.Entry<L, List<T>> mpgPropTransformers : propTransformers.entrySet()) {
            List<T> propTransformerCopies = new ArrayList<>(mpgPropTransformers.getValue().size());
            for (T propTransformer : mpgPropTransformers.getValue()) {
                propTransformerCopies.add(propTransformer.copy());
            }
            propTransformersCopy.put(mpgPropTransformers.getKey(), propTransformerCopies);
        }
        return propTransformersCopy;
    }

    private Map<L, BitSet> getWorkSetCopy() {
        Map<L, BitSet> workSetCopy = new HashMap<>();
        for (Map.Entry<L, BitSet> mpgWorkSet : workSet.entrySet()) {
            workSetCopy.put(mpgWorkSet.getKey(), (BitSet) mpgWorkSet.getValue().clone());
        }
        return workSetCopy;
    }

    private Map<L, List<List<FormulaNode<L, AP>>>> computeSatisfiedSubformulas() {
        Map<L, List<List<FormulaNode<L, AP>>>> satisfiedSubformulas = new HashMap<>();
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> mpgLabelAndMpg : mpgs.entrySet()) {
            L mpgLabel = mpgLabelAndMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = mpgLabelAndMpg.getValue();
            List<List<FormulaNode<L, AP>>> mpgSatisfiedSubformulas = new ArrayList<>();
            for (int nodeId = 0; nodeId < mpg.size(); nodeId++) {
                mpgSatisfiedSubformulas.add(getSatisfiedSubformulas(nodeId, mpgLabel));
            }
            satisfiedSubformulas.put(mpgLabel, mpgSatisfiedSubformulas);
        }
        return satisfiedSubformulas;
    }

    private void initialize(FormulaNode<L, AP> ast) {
        this.dependGraph = new DependencyGraph<>(ast);
        this.currentBlockIndex = dependGraph.getBlocks().size() - 1;
        this.mustTransformers = new HashMap<>();
        this.mayTransformers = new HashMap<>();

        initDDManager();

        initWorkList();
        initTransformers();
        initPredecessorsMapping();
    }

    protected abstract void initDDManager();

    private void initWorkList() {
        Map<L, BitSet> workSet = Maps.newHashMapWithExpectedSize(mpgs.size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            BitSet mpgBitSet = new BitSet(mpg.size());

            // Add all states to work set except final state, which is never updated
            mpgBitSet.set(0, mpg.size());
            mpgBitSet.set(getIdOfFinalNode(mpgLabel, mpg), false);
            workSet.put(mpgLabel, mpgBitSet);
        }

        this.workSet = workSet;
    }

    private void initTransformers() {
        Map<L, List<T>> propTransformers = Maps.newHashMapWithExpectedSize(mpgs.size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            List<T> transformers = new ArrayList<>(mpg.size());
            int finalNodeId = getIdOfFinalNode(mpgLabel, mpg);
            for (int nodeId = 0; nodeId < mpg.size(); nodeId++) {
                if (nodeId == finalNodeId) {
                    transformers.add(createInitTransformerEnd());
                } else {
                    transformers.add(createInitState());
                }
            }
            propTransformers.put(mpgLabel, transformers);
        }

        this.propTransformers = propTransformers;
    }

    private int getIdOfFinalNode(L mpgLabel, ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        return getNodeId(mpgLabel, mpg.getFinalNode());
    }

    protected abstract T createInitTransformerEnd();

    protected abstract T createInitState();

    private void initPredecessorsMapping() {
        Map<L, Map<Integer, Set<Integer>>> predecessors = Maps.newHashMapWithExpectedSize(mpgs.size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            predecessors.put(mpgLabel, initPredecessorsMapping(mpgLabel, mpg));
        }

        this.predecessors = predecessors;
    }

    private <N, E> Map<Integer, Set<Integer>> initPredecessorsMapping(L mpgLabel,
                                                                      ModalProcessGraph<N, L, E, AP, ?> mpg) {
        Map<Integer, Set<Integer>> nodeToPredecessors = new HashMap<>();
        for (N sourceNode : mpg.getNodes()) {
            int sourceNodeId = getNodeId(mpgLabel, sourceNode);
            for (E outgoingEdge : mpg.getOutgoingEdges(sourceNode)) {
                N targetNode = mpg.getTarget(outgoingEdge);
                int targetNodeId = getNodeId(mpgLabel, targetNode);
                Set<Integer> nodePredecessors = nodeToPredecessors.getOrDefault(targetNodeId, new HashSet<>());
                nodePredecessors.add(sourceNodeId);
                nodeToPredecessors.put(targetNodeId, nodePredecessors);
            }
        }

        return nodeToPredecessors;
    }

    private boolean isSat() {
        ModalProcessGraph<?, L, ?, AP, ?> mainMpg = mpgs.get(mainProcess);
        return isSat(getInitialNodeId(mainProcess, mainMpg), mainProcess);
    }

    private boolean isSat(int initialNodeId, L mpgLabel) {
        List<FormulaNode<L, AP>> satisfiedFormulas = getSatisfiedSubformulas(initialNodeId, mpgLabel);
        for (FormulaNode<L, AP> node : satisfiedFormulas) {
            if (node.getVarNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    private int getInitialNodeId(L mpgLabel, ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        return getNodeId(mpgLabel, mpg.getInitialNode());
    }

    private List<FormulaNode<L, AP>> getSatisfiedSubformulas(int nodeId, L mpgLabel) {
        Set<Integer> output =
                propTransformers.get(mpgLabel).get(nodeId).evaluate(toBoolArray(getAllAPDeadlockedState()));
        List<FormulaNode<L, AP>> satisfiedSubFormulas = new ArrayList<>();
        for (FormulaNode<L, AP> node : dependGraph.getFormulaNodes()) {
            if (output.contains(node.getVarNumber())) {
                satisfiedSubFormulas.add(node);
            }
        }
        return satisfiedSubFormulas;
    }

    private <N> int getNodeId(L mpgLabel, N node) {
        return getNodeIDs(mpgLabel).getNodeId(node);
    }

    private boolean[] toBoolArray(Set<Integer> satisfiedVars) {
        boolean[] arr = new boolean[dependGraph.getNumVariables()];
        for (Integer satisfiedVar : satisfiedVars) {
            arr[satisfiedVar] = true;
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    private <N> NodeIDs<N> getNodeIDs(L mpgLabel) {
        return (NodeIDs<N>) nodeIDs.get(mpgLabel);
    }

    private Set<Integer> getAllAPDeadlockedState() {
        return getAllAPDeadlockedState(mpgs.get(mainProcess));
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> Set<Integer> getAllAPDeadlockedState(ModalProcessGraph<N, L, E, AP, TP> mainMpg) {
        Set<Integer> satisfiedVariables = new HashSet<>();
        @NonNull N finalNode = mainMpg.getFinalNode();
        for (int blockIdx = dependGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            EquationalBlock<L, AP> block = dependGraph.getBlock(blockIdx);
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

    private List<T> updatedStateAndGetCompositions(int nodeId, L mpgLabel) {
        initUpdate(nodeId, mpgLabel);
        T stateTransformer = getTransformer(mpgLabel, nodeId);
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mpgs.get(mpgLabel);
        List<T> compositions = createCompositions(nodeId, mpgLabel, mpg);
        T updatedTransformer = getUpdatedPropertyTransformer(nodeId, mpgLabel, stateTransformer, mpg, compositions);
        updateTransformerAndWorkSet(nodeId, mpgLabel, stateTransformer, updatedTransformer);
        return compositions;
    }

    private void updateTransformerAndWorkSet(int nodeId, L mpgLabel, T stateTransformer, T updatedTransformer) {
        if (!stateTransformer.equals(updatedTransformer)) {
            propTransformers.get(mpgLabel).set(nodeId, updatedTransformer);
            updateWorkSet(nodeId, mpgLabel);
        }
        if (workSetIsEmpty() && currentBlockIndex > 0) {
            currentBlockIndex--;
            initWorkList();
        }
    }

    private void updateWorkSet(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mpgs.get(mpgLabel);
        if (Objects.equals(mpg.getInitialNode(), getNode(mpgLabel, nodeId))) {
            updateWorkSetStartState(mpgLabel);
        }
        addPredecessorsToWorkSet(nodeId, mpgLabel);
    }

    private boolean workSetIsEmpty() {
        for (BitSet bitSet : workSet.values()) {
            if (!bitSet.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void addPredecessorsToWorkSet(int nodeId, L mpgId) {
        for (Integer predecessorId : predecessors.get(mpgId).getOrDefault(nodeId, Collections.emptySet())) {
            addToWorkSet(mpgId, predecessorId);
        }
    }

    private void updateWorkSetStartState(L mpgLabel) {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mpgs.entrySet()) {
            updateWorkSetStartState(mpgLabel, labelMpg.getKey(), labelMpg.getValue());
        }
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> void updateWorkSetStartState(L mpgLabelOfUpdatedProcess,
                                                                                        L mpgLabel,
                                                                                        ModalProcessGraph<N, L, E, AP, TP> mpg) {
        for (N node : mpg.getNodes()) {
            for (E outgoingEdge : mpg.getOutgoingEdges(node)) {
                if (Objects.equals(mpg.getEdgeLabel(outgoingEdge), mpgLabelOfUpdatedProcess)) {
                    int nodeId = getNodeId(mpgLabel, node);
                    addToWorkSet(mpgLabel, nodeId);
                }
            }
        }
    }

    private void initUpdate(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mpgs.get(mpgLabel);
        if (getNode(mpgLabel, nodeId).equals(mpg.getFinalNode())) {
            throw new IllegalArgumentException("End State must not be updated!");
        }
        workSet.get(mpgLabel).set(nodeId, false);
    }

    @SuppressWarnings("unchecked")
    private <N> N getNode(L mpgLabel, int nodeId) {
        return (N) getNodeIDs(mpgLabel).getNode(nodeId);
    }

    private <E> T getEdgeTransformer(E edge, ModalProcessGraph<?, L, E, AP, ?> mpg) {
        T edgeTransformer;
        L edgeLabel = mpg.getEdgeLabel(edge);
        if (isProcessEdge(mpg, edge)) {
            int initialNodeId = getInitialNodeId(edgeLabel, mpgs.get(edgeLabel));
            edgeTransformer = propTransformers.get(edgeLabel).get(initialNodeId);
        } else {
            if (isMustEdge(mpg, edge)) {
                if (mustTransformers.containsKey(edgeLabel)) {
                    edgeTransformer = mustTransformers.get(edgeLabel);
                } else {
                    edgeTransformer = createInitTransformerEdge(edgeLabel, mpg.getEdgeProperty(edge));
                    mustTransformers.put(edgeLabel, edgeTransformer);
                }
            } else {
                if (mayTransformers.containsKey(edgeLabel)) {
                    edgeTransformer = mayTransformers.get(edgeLabel);
                } else {
                    edgeTransformer = createInitTransformerEdge(edgeLabel, mpg.getEdgeProperty(edge));
                    mayTransformers.put(edgeLabel, edgeTransformer);
                }
            }
        }
        return edgeTransformer;
    }

    private <E> boolean isProcessEdge(ModalProcessGraph<?, L, E, AP, ?> mpg, E edge) {
        return mpg.getEdgeProperty(edge).isProcess();
    }

    private <E> boolean isMustEdge(ModalProcessGraph<?, L, E, AP, ?> mpg, E edge) {
        return mpg.getEdgeProperty(edge).isMust();
    }

    protected abstract <TP extends ModalEdgeProperty> T createInitTransformerEdge(L edgeLabel, TP edgeProperty);

    private T getUpdatedPropertyTransformer(int nodeId,
                                            L mpgLabel,
                                            T stateTransformer,
                                            ModalProcessGraph<?, L, ?, AP, ?> mpg,
                                            List<T> compositions) {
        EquationalBlock<L, AP> currentBlock = dependGraph.getBlock(currentBlockIndex);
        Set<AP> atomicPropositions = mpg.getAtomicPropositions(getNode(mpgLabel, nodeId));
        return stateTransformer.createUpdate(atomicPropositions, compositions, currentBlock);
    }

    private <N, E> List<T> createCompositions(int nodeId, L mpgLabel, ModalProcessGraph<N, L, E, AP, ?> mpg) {
        List<T> compositions = new ArrayList<>();
        N node = getNode(mpgLabel, nodeId);
        for (E edge : mpg.getOutgoingEdges(node)) {
            N targetNode = mpg.getTarget(edge);
            int targetNodeId = getNodeId(mpgLabel, targetNode);
            T edgeTransformer = getEdgeTransformer(edge, mpg);
            T succTransformer = getTransformer(mpgLabel, targetNodeId);
            T composition = edgeTransformer.compose(succTransformer);
            composition.setIsMust(isMustEdge(mpg, edge));
            compositions.add(composition);
        }
        return compositions;
    }

    private T getTransformer(L mpgLabel, int nodeId) {
        return propTransformers.get(mpgLabel).get(nodeId);
    }

    private void addToWorkSet(L mpgId, int nodeId) {
        workSet.get(mpgId).set(nodeId, true);
    }

}
