package net.automatalib.modelcheckers.m3c.solver;

import java.util.Collections;
import java.util.Map;

import net.automatalib.graphs.ModalContextFreeProcessSystem;
import net.automatalib.graphs.ModalProcessGraph;
import net.automatalib.graphs.MutableModalProcessGraph;
import net.automatalib.graphs.base.compact.CompactMPG;
import net.automatalib.modelcheckers.m3c.transformer.PropertyTransformer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

abstract class SolverTest<T extends PropertyTransformer> {

    protected static ModalContextFreeProcessSystem<Character, String> mcfps;

    @BeforeClass
    public static void setup() {
        final CompactMPG<Character, String> mpg = buildMPG(new CompactMPG<>());

        mcfps = new ModalContextFreeProcessSystem<Character, String>() {

            @Override
            public Alphabet<Character> getTerminalAlphabet() {
                return Alphabets.fromArray('a', 'b', 'e');
            }

            @Override
            public Alphabet<Character> getProcessAlphabet() {
                return Alphabets.singleton('P');
            }

            @Override
            public Map<Character, ModalProcessGraph<?, Character, ?, String, ?>> getMPGs() {
                return Collections.singletonMap('P', mpg);
            }

            @Override
            public Character getMainProcess() {
                return 'P';
            }
        };
    }

    private static <N, E, AP, MMPG extends MutableModalProcessGraph<N, Character, E, AP, ?>> MMPG buildMPG(MMPG mpg) {

        final N start = mpg.addNode();
        final N end = mpg.addNode();
        final N s1 = mpg.addNode();
        final N s2 = mpg.addNode();

        mpg.setInitialNode(start);
        mpg.setFinalNode(end);

        final E e1 = mpg.connect(start, s1);
        final E e2 = mpg.connect(start, end);
        final E e3 = mpg.connect(s1, s2);
        final E e4 = mpg.connect(s2, end);

        mpg.getEdgeProperty(e1).setMust();
        mpg.setEdgeLabel(e1, 'a');

        mpg.getEdgeProperty(e2).setMust();
        mpg.setEdgeLabel(e2, 'e');

        mpg.getEdgeProperty(e3).setMust();
        mpg.setEdgeLabel(e3, 'P');

        mpg.getEdgeProperty(e4).setMust();
        mpg.setEdgeLabel(e4, 'b');

        return mpg;
    }

    @Test
    void testSolve() {
        String formula = "mu X.(<b><b>true || <>X)";
        SolveDD<T> solver = getSolver(mcfps, formula, false);
        assertSolve(solver, true);

        String negatedFormula = "!(" + formula + ")";
        solver = getSolver(mcfps, negatedFormula, false);
        assertSolve(solver, false);
    }

    public abstract <L, AP> SolveDD<T> getSolver(ModalContextFreeProcessSystem<L, AP> mcfps,
                                                 String formula,
                                                 boolean formulaIsCtl);

    protected void assertSolve(SolveDD<T> solver, boolean expectedIsSat) {
        solver.solve();
        Assert.assertEquals(expectedIsSat, solver.isSat());
    }

}
