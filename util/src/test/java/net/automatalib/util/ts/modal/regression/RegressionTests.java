/* Copyright (C) 2013-2024 TU Dortmund University
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.util.ts.modal.regression;

import java.io.IOException;

import net.automatalib.exception.FormatException;
import net.automatalib.ts.modal.impl.CompactMTS;
import net.automatalib.util.ts.modal.MTSs;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RegressionTests {

    @DataProvider(name = "Comp")
    public Object[][] loadCompositionTests() {
        Object[][] params = new Object[RegressionTestBundles.COMPOSITION_TESTS.size()][1];
        int i = 0;
        for (CompositionTest testCase : RegressionTestBundles.COMPOSITION_TESTS) {
            params[i++] = new Object[] {testCase};
        }
        return params;
    }

    @Test(dataProvider = "Comp")
    public void testMerge(CompositionTest testCase) throws IOException, FormatException {
        final CompositionInstance instance = new CompositionInstance(testCase);

        CompactMTS<String> currentMerge = MTSs.compose(instance.input0, instance.input1);

        Assert.assertTrue(currentMerge.getInputAlphabet().containsAll(instance.merge.getInputAlphabet()));
        Assert.assertTrue(instance.merge.getInputAlphabet().containsAll(currentMerge.getInputAlphabet()));
        Assert.assertTrue(MTSs.isRefinementOf(currentMerge, instance.merge, currentMerge.getInputAlphabet()));
        Assert.assertTrue(MTSs.isRefinementOf(instance.merge, currentMerge, currentMerge.getInputAlphabet()));
    }

}
