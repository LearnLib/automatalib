/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.modelchecker.m3c.formula;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.exception.FormatException;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DependencyGraphTest {

    @Test
    void testDependencyGraph() throws FormatException {
        String formula = "mu X.(<b><b>true || <>X)";
        FormulaNode<String, String> ast = M3CParser.parse(formula);
        DependencyGraph<String, String> dg = new DependencyGraph<>(ast);

        /* Assert that number of variables are correct */
        Assert.assertEquals(dg.getFormulaNodes().size(), 5);
        Assert.assertEquals(dg.getNumVariables(), 5);
        Assert.assertTrue(checkVarNumbering(dg.getFormulaNodes()));

        /* Assert that blocks are created correctly*/
        Assert.assertEquals(dg.getBlocks().size(), 1);
        Assert.assertEquals(dg.getBlocks().get(0).getNodes().size(), 5);
        Assert.assertTrue(isMonotonicallyDecreasing(dg.getBlocks().get(0).getNodes()));

    }

    private boolean checkVarNumbering(List<FormulaNode<String, String>> nodes) {
        int numVars = nodes.size();
        Set<Integer> vars = new HashSet<>();
        for (int i = 0; i < numVars; i++) {
            vars.add(i);
        }
        for (FormulaNode<String, String> node : nodes) {
            vars.remove(node.getVarNumber());
        }
        return vars.isEmpty();
    }

    private boolean isMonotonicallyDecreasing(List<FormulaNode<String, String>> nodes) {
        /* Checks if nodes are sorted such that dependencies between nodes are respected */
        for (int i = 0; i < nodes.size() - 1; i++) {
            if (nodes.get(i).getVarNumber() < nodes.get(i + 1).getVarNumber()) {
                return false;
            }
        }
        return true;
    }
}
