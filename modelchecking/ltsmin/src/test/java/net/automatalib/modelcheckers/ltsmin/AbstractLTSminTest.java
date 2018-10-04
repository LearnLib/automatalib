/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.modelcheckers.ltsmin;

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for AbstractLTSmin with arbitrary LTSs.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractLTSminTest<A, R extends Output<String, ?>> {

    private final Alphabet<String> alphabet = Alphabets.closedCharStringRange('a', 'b');

    private DetOutputAutomaton<?, String, ?, ?> counterExample;

    private A automaton;

    private String falseProperty;

    private Word<String> input;

    public Alphabet<String> getAlphabet() {
        return alphabet;
    }

    protected abstract DetOutputAutomaton<?, String, ?, ?> createCounterExample();

    protected abstract A createAutomaton();

    protected abstract String createFalseProperty();

    protected abstract Word<String> getInput();

    protected abstract int getMinimumMajorVersion();

    protected abstract int getMinimumMinorVersion();

    protected abstract int getMinimumPatchVersion();

    protected abstract AbstractLTSmin<String, A, R> getModelChecker();

    protected abstract void newModelChecker();

    @BeforeClass
    public void setupBeforeClass() {
        if (!LTSminUtil.checkUsable(getMinimumMajorVersion(), getMinimumMinorVersion(), getMinimumPatchVersion())) {
            throw new SkipException("LTSmin not installed");
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {
        newModelChecker();
        counterExample = createCounterExample();
        automaton = createAutomaton();
        falseProperty = createFalseProperty();
        input = getInput();
    }

    /**
     * First test for the absence of a counterexample, then test for the presence.
     */
    @Test
    public void testFindCounterExample() {
        R noCE = getModelChecker().findCounterExample(automaton, alphabet, "true");
        Assert.assertNull(noCE);

        R ce = getModelChecker().findCounterExample(automaton, alphabet, falseProperty);
        Assert.assertEquals(counterExample.computeOutput(input), ce.computeOutput(input));
    }
}