package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;

public class SolverBDDTest extends SolverTest<BDDTransformer> {

    public <L, AP> SolveDD<BDDTransformer> getSolver(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) {
        return new SolveBDD(mcfps, formula, formulaIsCtl);
    }

}
