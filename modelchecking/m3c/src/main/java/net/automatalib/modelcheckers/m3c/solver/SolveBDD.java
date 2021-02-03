package net.automatalib.modelcheckers.m3c.solver;

import java.util.ArrayList;
import java.util.List;

import info.scce.addlib.dd.bdd.BDDManager;
import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.formula.EquationalBlock;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;

public class SolveBDD extends SolveDD<BDDTransformer> {

    private BDDManager bddManager;

    public <L, AP> SolveBDD(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) {
        super(mcfps, formula, formulaIsCtl);
    }

    public <L, AP> SolveBDD(ModalContextFreeProcessSystem<L, AP> mcfps, FormulaNode formula, boolean formulaIsCtl) {
        super(mcfps, formula, formulaIsCtl);
    }

    public void updateState(State state) {
        initUpdate(state);
        int stateNumber = state.getStateNumber();
        BDDTransformer stateTransformer = (BDDTransformer) propTransformers[stateNumber];
        PropertyTransformer updatedTransformer = getUpdatedPropertyTransformer(state, stateTransformer);
        updateTransformerAndWorkSet(state, stateNumber, stateTransformer, updatedTransformer);
    }

    private PropertyTransformer getUpdatedPropertyTransformer(State state, BDDTransformer stateTransformer) {
        List<BDDTransformer> compositions = createCompositions(state);
        EquationalBlock currentBlock = dependGraph.getBlock(currentBlockIndex);
        return stateTransformer.createUpdate(state, compositions, currentBlock);
    }

    public List<BDDTransformer> createCompositions(State state) {
        List<BDDTransformer> compositions = new ArrayList<>();
        for (Edge edge : state.getOutgoingEdges()) {
            State targetState = cfps.getState(edge.getTarget().getStateNumber());
            PropertyTransformer edgeTransformer = getEdgeTransformer(edge);
            PropertyTransformer succTransformer = propTransformers[targetState.getStateNumber()];
            BDDTransformer composition = (BDDTransformer) edgeTransformer.compose(succTransformer);
            composition.setIsMust(edge.isMust());
            compositions.add(composition);
        }
        return compositions;
    }

    @Override
    protected void initDDManager() {
        this.bddManager = new BDDManager();
    }

    @Override
    protected PropertyTransformer createInitTransformerEnd() {
        int numVariables = dependGraph.getNumVariables();
        return new BDDTransformer(bddManager, numVariables);
    }

    @Override
    protected PropertyTransformer createInitState() {
        return new BDDTransformer(bddManager, dependGraph);
    }

    @Override
    protected PropertyTransformer createInitTransformerEdge(Edge edge) {
        return new BDDTransformer(bddManager, edge, dependGraph);
    }

}
