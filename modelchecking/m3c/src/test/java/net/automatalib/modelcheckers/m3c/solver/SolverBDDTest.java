package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.transformer.BDDTransformer;

public class SolverBDDTest extends SolverTest<BDDTransformer> {

    public SolveDD<BDDTransformer> getSolver(CFPS cfps, String formula, boolean formulaIsCtl) {
        return new SolveBDD(cfps, formula, formulaIsCtl);
    }

}
