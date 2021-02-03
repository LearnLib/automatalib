package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.transformer.ADDTransformer;

public class SolverADDTest extends SolverTest<ADDTransformer> {

    public <L, AP> SolveDD<ADDTransformer> getSolver(ModalContextFreeProcessSystem<L, AP> mcfps, String formula, boolean formulaIsCtl) {
        return new SolveADD(mcfps, formula, formulaIsCtl);
    }

}
