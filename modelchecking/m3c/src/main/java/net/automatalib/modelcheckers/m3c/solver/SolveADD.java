package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.List;

import info.scce.addlib.dd.xdd.latticedd.example.BooleanVectorLogicDDManager;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformer;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;

public class SolveADD extends SolveDD<ADDTransformer> {

    private BooleanVectorLogicDDManager ddManager;

    public SolveADD(CFPS cfps, String formula, boolean formulaIsCtl) {
        super(cfps, formula, formulaIsCtl);
    }

    public SolveADD(CFPS cfps, FormulaNode formula, boolean formulaIsCtl) {
        super(cfps, formula, formulaIsCtl);
    }

    public void updateState(State state) {
        initUpdate(state);
        int stateNumber = state.getStateNumber();
        ADDTransformer stateTransformer = (ADDTransformer) propTransformers[stateNumber];
        PropertyTransformer updatedTransformer = getUpdatedPropertyTransformer(state, stateTransformer);
        updateTransformerAndWorkSet(state, stateNumber, stateTransformer, updatedTransformer);
    }

    private PropertyTransformer getUpdatedPropertyTransformer(State state, ADDTransformer stateTransformer) {
        List<ADDTransformer> compositions = createCompositions(state);
        EquationalBlock currentBlock = dependGraph.getBlock(currentBlockIndex);
        return stateTransformer.createUpdate(state, compositions, currentBlock);
    }

    public List<ADDTransformer> createCompositions(State state) {
        List<ADDTransformer> compositions = new ArrayList<>();
        for (Edge edge : state.getOutgoingEdges()) {
            State targetState = cfps.getState(edge.getTarget().getStateNumber());
            PropertyTransformer edgeTransformer = getEdgeTransformer(edge);
            PropertyTransformer succTransformer = propTransformers[targetState.getStateNumber()];
            ADDTransformer composition = (ADDTransformer) edgeTransformer.compose(succTransformer);
            composition.setIsMust(edge.isMust());
            compositions.add(composition);
        }
        return compositions;
    }

    @Override
    protected void initDDManager() {
        this.ddManager = new BooleanVectorLogicDDManager(dependGraph.getNumVariables());
    }

    @Override
    protected PropertyTransformer createInitTransformerEdge(Edge edge) {
        return new ADDTransformer(ddManager, edge, dependGraph);
    }

    @Override
    protected PropertyTransformer createInitTransformerEnd() {
        return new ADDTransformer(ddManager, dependGraph.getNumVariables());
    }

    @Override
    protected PropertyTransformer createInitState() {
        return new ADDTransformer(ddManager, dependGraph);
    }

}
