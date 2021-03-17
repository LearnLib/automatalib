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
import java.util.Objects;
import java.util.Set;

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
import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserCTL;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserMuCalc;
import net.automatalib.modelcheckers.m3c.formula.visitor.CTLToMuCalc;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;
import net.automatalib.ts.modal.transition.ModalEdgeProperty;

public abstract class SolveDD<T extends PropertyTransformer<T>, L, AP> {

    protected ModalContextFreeProcessSystem<L, AP> mcfps;
    protected DependencyGraph dependGraph;
    protected int currentBlockIndex;
    // Property transformer for every state
    protected List<List<T>> propTransformers;
    private FormulaNode ast;
    // For each act \in Actions there is a property Transformer
    private Map<L, T> mustTransformers;
    private Map<L, T> mayTransformers;
    private List<Map<Integer, Set<Integer>>> predecessors;
    private List<NodeIDs<?>> nodeIDs;
    // Keeps track of which state's property transformers have to be updated.
    private BitSet[] workSet;

    public SolveDD(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) throws ParseException {
        this(mcfps, formulaIsCtl ? ParserCTL.parse(formula) : ParserMuCalc.parse(formula), formulaIsCtl);
    }

    public SolveDD(ModalContextFreeProcessSystem<L, AP> mcfps, FormulaNode formula, boolean formulaIsCtl) {
        this.mcfps = mcfps;
        if (formulaIsCtl) {
            this.ast = ctlToMuCalc(formula);
        } else {
            this.ast = formula;
        }
        this.ast = this.ast.toNNF();
    }

    protected static FormulaNode ctlToMuCalc(FormulaNode ctlFormula) {
        CTLToMuCalc transformation = new CTLToMuCalc();
        return transformation.toMuCalc(ctlFormula);
    }


    public void solve() {
        checkCFPS();
        initialize();
        while (true) {
            boolean workSetIsEmpty = true;
            for (int i = 0; i < workSet.length; i++) {
                BitSet mpgWorkSet = workSet[i];
                if (!mpgWorkSet.isEmpty()) {
                    workSetIsEmpty = false;
                    int nodeId = mpgWorkSet.nextSetBit(0);
                    L mpgLabel = mcfps.getProcessAlphabet().getSymbol(i);
                    updateState(nodeId, mpgLabel);
                }
            }
            if (workSetIsEmpty) {
                break;
            }
        }
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
        this.workSet = new BitSet[mcfps.getMPGs().size()];
        this.dependGraph = new DependencyGraph(ast);
        this.propTransformers = new ArrayList<>(mcfps.getMPGs().size());
        for (int i = 0; i < mcfps.getMPGs().size(); i++) {
            this.propTransformers.add(null);
        }
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

    protected void fillWorkList() {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            BitSet mpgBitSet = new BitSet(mpg.size());

            // Add all states to work set except final state, which is never updated
            mpgBitSet.set(0, mpg.size());
            mpgBitSet.set(getIdOfFinalNode(mpgLabel, mpg), false);
            int mpgIdx = getMpgId(mpgLabel);
            workSet[mpgIdx] = mpgBitSet;
        }
    }

    protected void initTransformers() {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            List<T> transformers = new ArrayList<>(mpg.size());
            for (int i = 0; i < mpg.size(); i++) {
                transformers.add(null);
            }
            int finalNodeId = getIdOfFinalNode(mpgLabel, mpg);
            for (int nodeId = 0; nodeId < mpg.size(); nodeId++) {
                if (nodeId == finalNodeId) {
                    transformers.set(nodeId, createInitTransformerEnd());
                } else {
                    transformers.set(nodeId, createInitState());
                }
            }
            int mpgIdx = getMpgId(mpgLabel);
            propTransformers.set(mpgIdx, transformers);
        }
    }

    protected void initNodeIds() {
        nodeIDs = new ArrayList<>(mcfps.getMPGs().size());
        for (int i = 0; i < mcfps.getMPGs().size(); i++) {
            nodeIDs.add(null);
        }
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            int mpgId = getMpgId(labelMpg.getKey());
            nodeIDs.set(mpgId, labelMpg.getValue().nodeIDs());
        }
    }

    private int getIdOfFinalNode(L mpgLabel, ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        return getNodeId(mpgLabel, mpg.getFinalNode());
    }

    protected abstract T createInitTransformerEnd();

    protected abstract T createInitState();

    private void initPredecessorsMapping() {
        predecessors = new ArrayList<>(mcfps.getMPGs().size());
        for (int i = 0; i < mcfps.getMPGs().size(); i++) {
            predecessors.add(null);
        }
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            L mpgLabel = labelMpg.getKey();
            ModalProcessGraph<?, L, ?, AP, ?> mpg = labelMpg.getValue();
            initPredecessorsMapping(mpgLabel, mpg, getMpgId(mpgLabel));
        }
    }

    private <N, E> void initPredecessorsMapping(L mpgLabel, ModalProcessGraph<N, L, E, AP, ?> mpg, int mpgId) {
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
        predecessors.set(mpgId, nodeToPredecessors);
    }

    public boolean isSat() {
        ModalProcessGraph<?, L, ?, AP, ?> mainMpg = mcfps.getMPGs().get(mcfps.getMainProcess());
        return isSat(getInitialNodeId(mcfps.getMainProcess(), mainMpg), getMpgId(mcfps.getMainProcess()));
    }

    private boolean isSat(int initialNodeId, int mpgId) {
        List<FormulaNode> satisfiedFormulas = getSatisfiedSubformulas(initialNodeId, mpgId);
        for (FormulaNode node : satisfiedFormulas) {
            if (node.getVarNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    private int getInitialNodeId(L mpgLabel, ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        return getNodeId(mpgLabel, mpg.getInitialNode());
    }

    protected int getMpgId(L mpgLabel) {
        return mcfps.getProcessAlphabet().getSymbolIndex(mpgLabel);
    }

    public List<FormulaNode> getSatisfiedSubformulas(int nodeId, int mpgId) {
        Set<Integer> output = propTransformers.get(mpgId).get(nodeId).evaluate(toBoolArray(getAllAPDeadlockedState()));
        List<FormulaNode> satisfiedSubFormulas = new ArrayList<>();
        for (FormulaNode node : dependGraph.getFormulaNodes()) {
            if (output.contains(node.getVarNumber())) {
                satisfiedSubFormulas.add(node);
            }
        }
        return satisfiedSubFormulas;
    }

    protected <N> int getNodeId(L mpgLabel, N node) {
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
        return (NodeIDs<N>) nodeIDs.get(getMpgId(mpgLabel));
    }

    public Set<Integer> getAllAPDeadlockedState() {
        return getAllAPDeadlockedState(mcfps.getMPGs().get(mcfps.getMainProcess()));
    }

    private <N, E, TP extends ModalEdgeProperty> Set<Integer> getAllAPDeadlockedState(ModalProcessGraph<N, L, E, AP, TP> mainMpg) {
        Set<Integer> satisfiedVariables = new HashSet<>();
        N finalNode = mainMpg.getFinalNode();
        for (int blockIdx = dependGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            EquationalBlock block = dependGraph.getBlock(blockIdx);
            for (FormulaNode node : block.getNodes()) {
                if (node instanceof TrueNode) {
                    satisfiedVariables.add(node.getVarNumber());
                } else if (node instanceof AtomicNode) {
                    String atomicProposition = ((AtomicNode) node).getProposition();
                    for (AP ap : mainMpg.getAtomicPropositions(finalNode)) {
                        if (ap.toString().equals(atomicProposition)) {
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

    public void updateState(int nodeId, L mpgLabel) {
        initUpdate(nodeId, mpgLabel);
        T stateTransformer = getTransformer(mpgLabel, nodeId);
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
        T updatedTransformer = getUpdatedPropertyTransformer(nodeId, mpgLabel, stateTransformer, mpg);
        updateTransformerAndWorkSet(nodeId, mpgLabel, stateTransformer, updatedTransformer);
    }

    protected void updateTransformerAndWorkSet(int nodeId, L mpgLabel, T stateTransformer, T updatedTransformer) {
        if (!stateTransformer.equals(updatedTransformer)) {
            propTransformers.get(getMpgId(mpgLabel)).set(nodeId, updatedTransformer);
            updateWorkSet(nodeId, mpgLabel);
        }
        if (workSetIsEmpty() && currentBlockIndex > 0) {
            currentBlockIndex--;
            fillWorkList();
        }
    }

    protected void updateWorkSet(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
        if (Objects.equals(mpg.getInitialNode(), getNode(mpgLabel, nodeId))) {
            updateWorkSetStartState(mpgLabel);
        }
        addPredecessorsToWorkSet(nodeId, getMpgId(mpgLabel));
    }

    private boolean workSetIsEmpty() {
        for (BitSet bitSet : workSet) {
            if (!bitSet.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected void addPredecessorsToWorkSet(int nodeId, int mpgId) {
        for (Integer predecessorId : predecessors.get(mpgId).getOrDefault(nodeId, new HashSet<>())) {
            workSet[mpgId].set(predecessorId, true);
        }
    }

    protected void updateWorkSetStartState(L mpgLabel) {
        for (Map.Entry<L, ModalProcessGraph<?, L, ?, AP, ?>> labelMpg : mcfps.getMPGs().entrySet()) {
            updateWorkSetStartState(mpgLabel, labelMpg.getKey(), labelMpg.getValue());
        }
    }

    private <N, E, TP extends ModalEdgeProperty> void updateWorkSetStartState(L mpgLabelOfUpdatedProcess,
                                                                              L mpgLabel,
                                                                              ModalProcessGraph<N, L, E, AP, TP> mpg) {
        for (N node : mpg.getNodes()) {
            for (E outgoingEdge : mpg.getOutgoingEdges(node)) {
                if (mpg.getEdgeLabel(outgoingEdge).equals(mpgLabelOfUpdatedProcess)) {
                    int nodeId = getNodeId(mpgLabel, node);
                    int mpgId = getMpgId(mpgLabel);
                    workSet[mpgId].set(nodeId, true);
                }
            }
        }
    }

    protected void initUpdate(int nodeId, L mpgLabel) {
        ModalProcessGraph<?, L, ?, AP, ?> mpg = mcfps.getMPGs().get(mpgLabel);
        if (getNode(mpgLabel, nodeId).equals(mpg.getFinalNode())) {
            throw new IllegalArgumentException("End State must not be updated!");
        }
        workSet[getMpgId(mpgLabel)].set(nodeId, false);
    }

    @SuppressWarnings("unchecked")
    protected <N> N getNode(L mpgLabel, int nodeId) {
        return (N) getNodeIDs(mpgLabel).getNode(nodeId);
    }

    public <E> T getEdgeTransformer(E edge, ModalProcessGraph<?, L, E, AP, ?> mpg) {
        T edgeTransformer;
        L edgeLabel = mpg.getEdgeLabel(edge);
        if (mcfps.getProcessAlphabet().containsSymbol(edgeLabel)) {
            int initialNodeId = getInitialNodeId(edgeLabel, mcfps.getMPGs().get(edgeLabel));
            edgeTransformer = propTransformers.get(getMpgId(edgeLabel)).get(initialNodeId);
        } else {
            if (mpg.getEdgeProperty(edge).isMust()) {
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

    protected abstract <TP extends ModalEdgeProperty> T createInitTransformerEdge(L edgeLabel, TP edgeProperty);

    protected T getUpdatedPropertyTransformer(int nodeId,
                                              L mpgLabel,
                                              T stateTransformer,
                                              ModalProcessGraph<?, L, ?, AP, ?> mpg) {
        List<T> compositions = createCompositions(nodeId, mpgLabel, mpg);
        EquationalBlock currentBlock = dependGraph.getBlock(currentBlockIndex);
        Set<AP> atomicPropositions = mpg.getAtomicPropositions(getNode(mpgLabel, nodeId));
        return stateTransformer.createUpdate(atomicPropositions, compositions, currentBlock);
    }

    protected <N, E> List<T> createCompositions(int nodeId, L mpgLabel, ModalProcessGraph<N, L, E, AP, ?> mpg) {
        List<T> compositions = new ArrayList<>();
        N node = getNode(mpgLabel, nodeId);
        for (E edge : mpg.getOutgoingEdges(node)) {
            N targetNode = mpg.getTarget(edge);
            int targetNodeId = getNodeId(mpgLabel, targetNode);
            T edgeTransformer = getEdgeTransformer(edge, mpg);
            T succTransformer = getTransformer(mpgLabel, targetNodeId);
            T composition = edgeTransformer.compose(succTransformer);
            composition.setIsMust(mpg.getEdgeProperty(edge).isMust());
            compositions.add(composition);
        }
        return compositions;
    }

    protected T getTransformer(L mpgLabel, int nodeId) {
        return propTransformers.get(getMpgId(mpgLabel)).get(nodeId);
    }

    public BitSet[] getWorkSet() {
        return workSet;
    }

    public DependencyGraph getDependGraph() {
        return dependGraph;
    }

    public ModalContextFreeProcessSystem<L, AP> getCfps() {
        return mcfps;
    }

    public List<T> getPropertyTransformers(L mpgLabel) {
        return propTransformers.get(getMpgId(mpgLabel));
    }

    public Map<L, T> getMustTransformers() {
        return mustTransformers;
    }

    public Map<L, T> getMayTransformers() {
        return mayTransformers;
    }

}
