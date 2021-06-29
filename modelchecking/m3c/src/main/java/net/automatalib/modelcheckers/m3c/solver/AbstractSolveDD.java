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

abstract class AbstractSolveDD<T extends AbstractPropertyTransformer<T, L, AP>, L, AP> {

    protected ModalContextFreeProcessSystem<L, AP> mcfps;
    protected DependencyGraph<L, AP> dependGraph;
    protected int currentBlockIndex;
    // Property transformer for every state
    protected Map<L, List<T>> propTransformers;
    private FormulaNode<L, AP> ast;
    // For each act \in Actions there is a property Transformer
    private Map<L, T> mustTransformers;
    private Map<L, T> mayTransformers;
    private Map<L, Map<Integer, Set<Integer>>> predecessors;
    private Map<L, NodeIDs<?>> nodeIDs;
    // Keeps track of which state's property transformers have to be updated.
    private Map<L, BitSet> workSet;

    AbstractSolveDD(ModalContextFreeProcessSystem<L, AP> mcfps) {
        this.mcfps = mcfps;
        checkCFPS();
    }

    private FormulaNode<L, AP> ctlToMuCalc(FormulaNode<L, AP> ctlFormula) {
        CTLToMuCalc<L, AP> transformation = new CTLToMuCalc<>();
        return transformation.toMuCalc(ctlFormula);
    }

    public boolean solve(FormulaNode<L, AP> formula) {
        this.ast = ctlToMuCalc(formula);
        this.ast = this.ast.toNNF();

        initialize();

        while (true) {
            boolean workSetIsEmpty = true;
            for (Entry<L, BitSet> entry : workSet.entrySet()) {
                BitSet mpgWorkSet = entry.getValue();
                if (!mpgWorkSet.isEmpty()) {
                    workSetIsEmpty = false;
                    int nodeId = mpgWorkSet.nextSetBit(0);
                    L mpgLabel = entry.getKey();
                    updateState(nodeId, mpgLabel);
                }
            }
            if (workSetIsEmpty) {
                break;
            }
        }

        return isSat();
    }

    private void checkCFPS() {
        if (mcfps.getMainProcess() == null) {
            throw new IllegalArgumentException("The mcfps must have an assigned main process.");
        }
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            if (mpg.getInitialNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no start state.");
            }
            if (mpg.getFinalNode() == null) {
                throw new IllegalArgumentException("MPG " + mpgLabel + " has no end state.");
            }
        }
    }

    public void initialize() {
        this.workSet = Maps.newHashMapWithExpectedSize(mcfps.getMPGs().size());
        this.dependGraph = new DependencyGraph<>(ast);
        this.propTransformers = Maps.newHashMapWithExpectedSize(mcfps.getMPGs().size());
        this.currentBlockIndex = dependGraph.getBlocks().size() - 1;
        this.mustTransformers = new HashMap<>();
        this.mayTransformers = new HashMap<>();
        initDDManager();
        initNodeIds();
        fillWorkList();
        initTransformers();
        initPredecessorsMapping();
    }

    protected abstract void initDDManager();

    private void fillWorkList() {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            BitSet mpgBitSet = new BitSet(mpg.size());

            // Add all states to work set except final state, which is never updated
            mpgBitSet.set(0, mpg.size());
            mpgBitSet.set(getIdOfFinalNode(mpgLabel, mpg), false);
            workSet.put(mpgLabel, mpgBitSet);
        }
    }

    private void initTransformers() {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
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
    }

    private void initNodeIds() {
        nodeIDs = Maps.newHashMapWithExpectedSize(mcfps.getMPGs().size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> entry : mcfps.getMPGs().entrySet()) {
            nodeIDs.put(entry.getKey(), entry.getValue().nodeIDs());
        }
    }

    private int getIdOfFinalNode(L mpgLabel, ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        return getNodeId(mpgLabel, mpg.getFinalNode());
    }

    protected abstract T createInitTransformerEnd();

    protected abstract T createInitState();

    private void initPredecessorsMapping() {
        predecessors = Maps.newHashMapWithExpectedSize(mcfps.getMPGs().size());
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            initPredecessorsMapping(mpgLabel, mpg);
        }
    }

    private <N, E> void initPredecessorsMapping(L mpgLabel, ModalProcessGraph<N, L, E, AP, ?> mpg) {
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
        predecessors.put(mpgLabel, nodeToPredecessors);
    }

    private boolean isSat() {
        ModalProcessGraph<?, L, ?, AP, ?> mainMpg = mcfps.getMPGs().get(mcfps.getMainProcess());
        return isSat(getInitialNodeId(mcfps.getMainProcess(), mainMpg), mcfps.getMainProcess());
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
        return getAllAPDeadlockedState(mcfps.getMPGs().get(mcfps.getMainProcess()));
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> Set<Integer> getAllAPDeadlockedState(ModalProcessGraph<N, L, E, AP, TP> mainMpg) {
        Set<Integer> satisfiedVariables = new HashSet<>();
        N finalNode = mainMpg.getFinalNode();
        for (int blockIdx = dependGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            EquationalBlock<L, AP> block = dependGraph.getBlock(blockIdx);
            for (FormulaNode<L, AP> node : block.getNodes()) {
                if (node instanceof TrueNode) {
                    satisfiedVariables.add(node.getVarNumber());
                } else if (node instanceof AtomicNode) {
                    Set<AP> atomicProposition = ((AtomicNode<L, AP>) node).getPropositions();
                    for (AP ap : mainMpg.getAtomicPropositions(finalNode)) {
                        if (ap.equals(atomicProposition)) {
                            satisfiedVariables.add(node.getVarNumber());
                            break;
                        }
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

    private void updateState(int nodeId, L mpgLabel) {
        initUpdate(nodeId, mpgLabel);
        T stateTransformer = getTransformer(mpgLabel, nodeId);
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
        T updatedTransformer = getUpdatedPropertyTransformer(nodeId, mpgLabel, stateTransformer, mpg);
        updateTransformerAndWorkSet(nodeId, mpgLabel, stateTransformer, updatedTransformer);
    }

    private void updateTransformerAndWorkSet(int nodeId, L mpgLabel, T stateTransformer, T updatedTransformer) {
        if (!stateTransformer.equals(updatedTransformer)) {
            propTransformers.get(mpgLabel).set(nodeId, updatedTransformer);
            updateWorkSet(nodeId, mpgLabel);
        }
        if (workSetIsEmpty() && currentBlockIndex > 0) {
            currentBlockIndex--;
            fillWorkList();
        }
    }

    private void updateWorkSet(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
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
        for (Integer predecessorId : predecessors.get(mpgId).getOrDefault(nodeId, new HashSet<>())) {
            workSet.get(mpgId).set(predecessorId, true);
        }
    }

    private void updateWorkSetStartState(L mpgLabel) {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            updateWorkSetStartState(mpgLabel, labelMpg.getKey(), labelMpg.getValue());
        }
    }

    private <N, E, TP extends ProceduralModalEdgeProperty> void updateWorkSetStartState(L mpgLabelOfUpdatedProcess,
                                                                                        L mpgLabel,
                                                                                        ModalProcessGraph<N, L, E, AP, TP> mpg) {
        for (N node : mpg.getNodes()) {
            for (E outgoingEdge : mpg.getOutgoingEdges(node)) {
                if (mpg.getEdgeLabel(outgoingEdge).equals(mpgLabelOfUpdatedProcess)) {
                    int nodeId = getNodeId(mpgLabel, node);
                    workSet.get(mpgLabel).set(nodeId, true);
                }
            }
        }
    }

    private void initUpdate(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
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
            int initialNodeId = getInitialNodeId(edgeLabel, mcfps.getMPGs().get(edgeLabel));
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
                                            ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        List<T> compositions = createCompositions(nodeId, mpgLabel, mpg);
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

}
