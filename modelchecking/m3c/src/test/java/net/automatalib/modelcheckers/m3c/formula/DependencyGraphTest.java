package net.automatalib.modelcheckers.m3c.formula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.modelcheckers.m3c.formula.parser.ParseException;
import net.automatalib.modelcheckers.m3c.formula.parser.ParserMuCalc;
import org.testng.Assert;
import org.testng.annotations.Test;

class DependencyGraphTest {

    @Test
    void testDependencyGraph() throws ParseException {
        String formula = "mu X.(<b><b>true || <>X)";
        FormulaNode ast = ParserMuCalc.parse(formula);
        DependencyGraph dg = new DependencyGraph(ast);

        /* Assert that number of variables are correct */
        Assert.assertEquals(5, dg.getFormulaNodes().size());
        Assert.assertEquals(dg.getFormulaNodes().size(), dg.getNumVariables());
        Assert.assertTrue(checkVarNumbering(dg.getFormulaNodes()));

        /* Assert that blocks are created correctly*/
        Assert.assertEquals(1, dg.getBlocks().size());
        Assert.assertEquals(5, dg.getBlocks().get(0).getNodes().size());
        Assert.assertTrue(isMonotonicallyDecreasing(dg.getBlocks().get(0).getNodes()));

    }

    private boolean checkVarNumbering(List<FormulaNode> nodes) {
        int numVars = nodes.size();
        Set<Integer> vars = new HashSet<>();
        for (int i = 0; i < numVars; i++) {
            vars.add(i);
        }
        for (FormulaNode node : nodes) {
            vars.remove(node.getVarNumber());
        }
        return vars.isEmpty();
    }

    private boolean isMonotonicallyDecreasing(List<FormulaNode> nodes) {
        /* Checks if nodes are sorted such that dependencies between nodes are respected */
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (nodes.get(i).getVarNumber() < nodes.get(i + 1).getVarNumber()) {
                return false;
            }
        }
        return true;
    }
}
