/* Copyright (C) 2013-2026 TU Dortmund University
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
package net.automatalib.modelchecker.m3c.checker;

import java.io.IOException;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import net.automatalib.exception.FormatException;
import net.automatalib.exception.ModelCheckingException;
import net.automatalib.graph.ContextFreeModalProcessSystem;
import net.automatalib.modelchecker.m3c.formula.FormulaNode;
import net.automatalib.modelchecker.m3c.formula.NotNode;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.solver.WitnessExtractorTest;
import net.automatalib.modelchecker.m3c.solver.WitnessTree;
import net.automatalib.modelchecker.m3c.util.ExternalSystemDeserializer;
import net.automatalib.modelchecking.ModelChecker;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class M3CCheckersTest {

    private final ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>>
            stringChecker;
    private final ModelChecker<String, ContextFreeModalProcessSystem<String, Void>, FormulaNode<String, Void>, WitnessTree<String, Void>>
            typedChecker;
    private final ContextFreeModalProcessSystem<String, String> stringSystem;
    private final ContextFreeModalProcessSystem<String, Void> typedSystem;

    @Factory(dataProvider = "checkers")
    public M3CCheckersTest(ModelChecker<String, ContextFreeModalProcessSystem<String, String>, String, WitnessTree<String, String>> stringChecker,
                           ModelChecker<String, ContextFreeModalProcessSystem<String, Void>, FormulaNode<String, Void>, WitnessTree<String, Void>> typedChecker)
            throws IOException, ParserConfigurationException, SAXException {
        this.stringChecker = stringChecker;
        this.typedChecker = typedChecker;
        this.stringSystem = ExternalSystemDeserializer.parse("/cfmps/witness/an_c_bn.xml");
        this.typedSystem = ExternalSystemDeserializer.parse("/cfmps/witness/an_c_bn.xml");
    }

    @DataProvider
    public static Object[][] checkers() {
        var typedBDD = M3CCheckers.<String, Void>typedChecker();
        var typedADD = M3CCheckers.<String, Void>typedADDChecker();
        var stringBDD = M3CCheckers.checker();
        var stringADD = M3CCheckers.addChecker();

        return new Object[][] {{stringADD, typedADD}, {stringBDD, typedBDD}};
    }

    @Test(dataProvider = "formulasOnAnCBn", dataProviderClass = WitnessExtractorTest.class)
    public void checkTypedFormulasOnAnCBn(String formula, Word<String> expectedWitness) throws FormatException {

        var f = M3CParser.<String, Void>parse(formula, l -> l, ap -> null);
        var ce = typedChecker.findCounterExample(typedSystem, Collections.emptyList(), new NotNode<>(f));

        Assert.assertNotNull(ce);
        Assert.assertEquals(ce.getWitness(), expectedWitness);
    }

    @Test(dataProvider = "formulasOnAnCBn", dataProviderClass = WitnessExtractorTest.class)
    public void checkStringFormulasOnAnCBn(String formula, Word<String> expectedWitness) {

        var ce = stringChecker.findCounterExample(stringSystem, Collections.emptyList(), "!(" + formula + ')');

        Assert.assertNotNull(ce);
        Assert.assertEquals(ce.getWitness(), expectedWitness);
    }

    @Test
    public void testModelCheckingException() {
        Assert.assertThrows(ModelCheckingException.class,
                            () -> stringChecker.findCounterExample(stringSystem,
                                                                   Collections.emptyList(),
                                                                   "true &&& false"));
    }
}
