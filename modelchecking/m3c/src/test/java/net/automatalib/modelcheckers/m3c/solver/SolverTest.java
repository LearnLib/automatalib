package net.automatalib.modelcheckers.m3c.solver;

import net.automatalib.modelcheckers.m3c.cfps.CFPS;
import net.automatalib.modelcheckers.m3c.cfps.Edge;
import net.automatalib.modelcheckers.m3c.cfps.EdgeType;
import net.automatalib.modelcheckers.m3c.cfps.ProceduralProcessGraph;
import net.automatalib.modelcheckers.m3c.cfps.State;
import net.automatalib.modelcheckers.m3c.cfps.StateClass;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

abstract class SolverTest<T extends PropertyTransformer> {

    protected static CFPS cfps;

    @BeforeClass
    public static void setup() {
        cfps = new CFPS();
        ProceduralProcessGraph ppg = new ProceduralProcessGraph();
        ppg.setProcessName("P");
        cfps.addPPG(ppg);
        cfps.setMainGraph(ppg);

        State startState = cfps.createAndAddState(ppg, StateClass.START).withName("start");
        State endState = cfps.createAndAddState(ppg, StateClass.END).withName("end");
        State s1 = cfps.createAndAddState(ppg, StateClass.NORMAL).withName("s1");
        State s2 = cfps.createAndAddState(ppg, StateClass.NORMAL).withName("s2");

        Edge startToS1 = new Edge(startState, s1, "a", EdgeType.MUST);
        Edge startToEnd = new Edge(startState, endState, "e", EdgeType.MUST);
        Edge s1ToS2 = new Edge(s1, s2, "P", EdgeType.MUST_PROCESS);
        Edge s2TOEnd = new Edge(s2, endState, "b", EdgeType.MUST);

        startState.addEdge(startToS1);
        startState.addEdge(startToEnd);
        s1.addEdge(s1ToS2);
        s2.addEdge(s2TOEnd);
    }

    @Test
    void testSolve() {
        String formula = "mu X.(<b><b>true || <>X)";
        SolveDD<T> solver = getSolver(cfps, formula, false);
        assertSolve(solver, true);

        String negatedFormula = "!(" + formula + ")";
        solver = getSolver(cfps, negatedFormula, false);
        assertSolve(solver, false);
    }

    public abstract SolveDD<T> getSolver(CFPS cfps, String formula, boolean formulaIsCtl);

    protected void assertSolve(SolveDD<T> solver, boolean expectedIsSat) {
        solver.solve();
        Assert.assertEquals(expectedIsSat, solver.isSat());
    }

}
