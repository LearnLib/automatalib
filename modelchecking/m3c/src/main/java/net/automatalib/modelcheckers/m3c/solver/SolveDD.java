package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.ProceduralProcessGraph;
import net.automatalib.modelcheckers.m3c.cfps.State;
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
import net.automatalib.modelcheckers.m3c.util.Converter;

public abstract class SolveDD<T extends PropertyTransformer> {

    protected CFPS cfps;
    protected DependencyGraph dependGraph;
    protected int currentBlockIndex;
    // Property transformer for every state
    protected PropertyTransformer[] propTransformers;
    private FormulaNode ast;
    // For each act \in Actions there is a property Transformer
    private Map<String, PropertyTransformer> mustTransformers;
    private Map<String, PropertyTransformer> mayTransformers;

    // Keeps track of which state's property transformers have to be updated
    private Set<State> workSet;

    public <L, AP> SolveDD(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) {
        this.cfps = Converter.toCFPS(mcfps);
        if (formulaIsCtl) {
            FormulaNode ctlFormula = null;
            try {
                ctlFormula = ParserCTL.parse(formula);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Input formula " + formula + " is not a valid CTL formula");
            }
            this.ast = ctlToMuCalc(ctlFormula);
        } else {
            try {
                this.ast = ParserMuCalc.parse(formula);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Input formula " + formula + " is not a valid mu calculus formula");
            }
        }
        this.ast = this.ast.toNNF();
    }

    protected static FormulaNode ctlToMuCalc(FormulaNode ctlFormula) {
        CTLToMuCalc transformation = new CTLToMuCalc();
        return transformation.toMuCalc(ctlFormula);
    }

    public <L, AP> SolveDD(ModalContextFreeProcessSystem<L, AP> mcfps, FormulaNode formula, boolean formulaIsCtl) {
        this.cfps = Converter.toCFPS(mcfps);
        if (formulaIsCtl) {
            this.ast = ctlToMuCalc(formula);
        } else {
            this.ast = formula;
        }
        this.ast = this.ast.toNNF();
    }

    public abstract List<T> createCompositions(State state);

    public void solve() {
        checkCFPS();
        initialize();
        while (!workSet.isEmpty()) {
            State nextState = workSet.iterator().next();
            updateState(nextState);
        }
    }

    private void checkCFPS() {
        if (cfps.getMainGraph() == null) {
            throw new IllegalArgumentException("The cfps must have an assigned main graph");
        }
        for (ProceduralProcessGraph ppg : cfps.getProcessList()) {
            String ppgName = ppg.getProcessName();
            if (ppg.getStartState() == null) {
                throw new IllegalArgumentException("PPG " + ppgName + " has no start state");
            }
            if (ppg.getEndState() == null) {
                throw new IllegalArgumentException("PPG " + ppgName + " has no end state");
            }
        }
    }

    public void initialize() {
        this.workSet = new LinkedHashSet<>();
        this.dependGraph = new DependencyGraph(ast);
        this.propTransformers = new PropertyTransformer[cfps.getStateList().size()];
        this.currentBlockIndex = dependGraph.getBlocks().size() - 1;
        this.mustTransformers = new HashMap<>();
        this.mayTransformers = new HashMap<>();
        initDDManager();
        fillWorkList();
        initTransformers();
    }

    public abstract void updateState(State state);

    protected abstract void initDDManager();

    protected void fillWorkList() {
        for (ProceduralProcessGraph ppg : cfps.getProcessList()) {
            for (State state : ppg.getStates()) {
                if (!state.isEndState()) {
                    workSet.add(state);
                }
            }
        }
    }

    protected void initTransformers() {
        for (int stateNumber = 0; stateNumber < cfps.getStateList().size(); stateNumber++) {
            State state = cfps.getState(stateNumber);
            if (state.isEndState()) {
                propTransformers[stateNumber] = createInitTransformerEnd();
            } else {
                propTransformers[stateNumber] = createInitState();
            }
        }
    }

    protected abstract PropertyTransformer createInitTransformerEnd();

    protected abstract PropertyTransformer createInitState();

    public boolean isSat() {
        ProceduralProcessGraph mainPPG = cfps.getMainGraph();
        State startState = mainPPG.getStartState();
        List<FormulaNode> satisfiedFormulas = getSatisfiedSubformulas(startState);
        for (FormulaNode node : satisfiedFormulas) {
            if (node.getVarNumber() == 0) {
                return true;
            }
        }
        return false;
    }

    public List<FormulaNode> getSatisfiedSubformulas(State state) {
        Set<Integer> output = propTransformers[state.getStateNumber()].evaluate(toBoolArray(getAllAPDeadlockedState()));
        List<FormulaNode> satisfiedSubFormulas = new ArrayList<>();
        for (FormulaNode node : dependGraph.getFormulaNodes()) {
            if (output.contains(node.getVarNumber())) {
                satisfiedSubFormulas.add(node);
            }
        }
        return satisfiedSubFormulas;
    }

    private boolean[] toBoolArray(Set<Integer> satisfiedVars) {
        boolean[] arr = new boolean[dependGraph.getNumVariables()];
        for (Integer satisfiedVar : satisfiedVars) {
            arr[satisfiedVar] = true;
        }
        return arr;
    }

    public Set<Integer> getAllAPDeadlockedState() {
        State endState = cfps.getMainGraph().getEndState();
        Set<Integer> satisfiedVariables = new HashSet<>();

        for (int blockIdx = dependGraph.getBlocks().size() - 1; blockIdx >= 0; blockIdx--) {
            EquationalBlock block = dependGraph.getBlock(blockIdx);
            for (FormulaNode node : block.getNodes()) {
                if (node instanceof TrueNode) {
                    satisfiedVariables.add(node.getVarNumber());
                } else if (node instanceof AtomicNode) {
                    String atomicProposition = ((AtomicNode) node).getProposition();
                    if (endState.satisfiesAtomicProposition(atomicProposition)) {
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

    protected void updateTransformerAndWorkSet(State state,
                                               int stateNumber,
                                               PropertyTransformer stateTransformer,
                                               PropertyTransformer updatedTransformer) {
        if (!stateTransformer.equals(updatedTransformer)) {
            propTransformers[stateNumber] = updatedTransformer;
            updateWorkSet(state);
        }
        if (workSet.isEmpty() && (currentBlockIndex > 0)) {
            currentBlockIndex--;
            fillWorkList();
        }
    }

    protected void updateWorkSet(State updatedState) {
        if (updatedState.isStartState()) {
            String label = updatedState.getProceduralProcessGraph().getProcessName();
            workSet.addAll(cfps.getStatesOutgoingEdgeLabeledBy(label));
        }
        workSet.addAll(updatedState.getPredecessors());
    }

    protected void initUpdate(State state) {
        if (state.isEndState()) {
            throw new IllegalArgumentException("End State must not be updated!");
        }
        workSet.remove(state);
    }

    public PropertyTransformer getEdgeTransformer(Edge edge) {
        PropertyTransformer edgeTransformer;
        if (edge.isProcessCall()) {
            int stateNumberOfProcess = cfps.getStateNumberOfProcess(edge.getLabel());
            edgeTransformer = propTransformers[stateNumberOfProcess];
        } else {
            if (edge.isMust()) {
                if (mustTransformers.containsKey(edge.getLabel())) {
                    edgeTransformer = mustTransformers.get(edge.getLabel());
                } else {
                    edgeTransformer = createInitTransformerEdge(edge);
                    mustTransformers.put(edge.getLabel(), edgeTransformer);
                }
            } else {
                if (mayTransformers.containsKey(edge.getLabel())) {
                    edgeTransformer = mayTransformers.get(edge.getLabel());
                } else {
                    edgeTransformer = createInitTransformerEdge(edge);
                    mayTransformers.put(edge.getLabel(), edgeTransformer);
                }
            }
        }
        return edgeTransformer;
    }

    protected abstract PropertyTransformer createInitTransformerEdge(Edge edge);

    public Set<State> getWorkSet() {
        return workSet;
    }

    public DependencyGraph getDependGraph() {
        return dependGraph;
    }

    public CFPS getCfps() {
        return cfps;
    }

    public PropertyTransformer[] getPropertyTransformers() {
        return propTransformers;
    }

    public Map<String, PropertyTransformer> getMustTransformers() {
        return mustTransformers;
    }

    public Map<String, PropertyTransformer> getMayTransformers() {
        return mayTransformers;
    }

}
