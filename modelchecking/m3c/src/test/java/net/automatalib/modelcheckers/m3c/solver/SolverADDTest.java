package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformer;

public class SolverADDTest extends SolverTest<ADDTransformer> {

    public SolveDD<ADDTransformer> getSolver(CFPS cfps, String formula, boolean formulaIsCtl) {
        return new SolveADD(cfps, formula, formulaIsCtl);
    }

}
