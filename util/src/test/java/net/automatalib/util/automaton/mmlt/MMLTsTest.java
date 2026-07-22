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
package net.automatalib.util.automaton.mmlt;

import java.util.HashSet;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.symbol.time.TimedInput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MMLTsTest {

    public CompactMMLT<String, String> buildBaseModel() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addInitialState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.addTransition(s0, "p1", s1, "go");
        model.addTransition(s1, "abort", s1, "ok");
        model.addLocalReset(s1, "abort");

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, "p2", s2, "go");
        model.addTransition(s2, "abort", s3, "void");
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, "collect", s0, "void");

        return model;
    }

    @Test
    public void testTimerAndResetRemovals() {
        var model = buildBaseModel();

        int s1 = 1;
        int s3 = 3;

        // Remove and add some timers:
        model.addPeriodicTimer(s1, "e", 12, "test");
        model.removeTimer(s1, "b");
        model.removeTimer(s1, "a");
        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.removeTimer(s1, "c");
        model.addOneShotTimer(s1, "c", 40, "done", s3);
        model.removeTimer(s1, "e");

        model.removeLocalReset(s1, "abort");
        model.addLocalReset(s1, "abort");

        // Still needs to be equivalent to original:
        var originalModel = buildBaseModel();
        Assert.assertTrue(MMLTs.testEquivalence(model, originalModel, originalModel.getSemantics().getInputAlphabet()));
    }

    @Test
    public void testSeparatedByResetsSimple() {
        Alphabet<String> alphabet = Alphabets.singleton("x");

        // Same model, but with reset in A and no reset in B:
        var modelA = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0 = modelA.addState();
        modelA.setInitialState(s0);
        modelA.addPeriodicTimer(s0, "a", 3, "test");
        modelA.addTransition(s0, "x", s0, "ok");
        modelA.addLocalReset(s0, "x");

        var modelB = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0B = modelB.addState();
        modelB.setInitialState(s0B);
        modelB.addPeriodicTimer(s0B, "a", 3, "test");
        modelB.addTransition(s0B, "x", s0B, "ok");

        Assert.assertFalse(MMLTs.testEquivalence(modelA, modelB, modelA.getSemantics().getInputAlphabet()));

        var sepWord = MMLTs.findSeparatingWord(modelA, modelB, modelA.getSemantics().getInputAlphabet());
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(modelA.computeOutput(sepWord), modelB.computeOutput(sepWord));

        // If we remove the timestep, should not find a counterexample:
        Set<TimedInput<String>> reducedInputs = new HashSet<>(modelA.getSemantics().getInputAlphabet());
        reducedInputs.remove(TimedInput.step());
        Assert.assertTrue(MMLTs.testEquivalence(modelA, modelB, reducedInputs));
    }

    @Test
    public void testSeparatedByResetsComplex() {
        Alphabet<String> alphabet = Alphabets.singleton("x");

        // Same model, but with reset in A and no reset in B:
        var modelA = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0 = modelA.addState();
        var s1 = modelA.addState();
        modelA.setInitialState(s0);
        modelA.addPeriodicTimer(s0, "a", 3, "test");
        modelA.addOneShotTimer(s0, "b", 5, "test2", s1);

        var modelB = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0B = modelB.addState();
        var s1B = modelB.addState();
        var s2B = modelB.addState();
        modelB.setInitialState(s0B);
        modelB.addOneShotTimer(s0B, "a", 3, "test", s1B);
        modelB.addOneShotTimer(s1B, "b", 2, "test2", s2B);
        modelB.addTransition(s1B, "x", s1B, "void");
        modelB.addLocalReset(s1B, "x");

        Assert.assertFalse(MMLTs.testEquivalence(modelA, modelB, modelA.getSemantics().getInputAlphabet()));

        var sepWord = MMLTs.findSeparatingWord(modelA, modelB, modelA.getSemantics().getInputAlphabet());
        Assert.assertNotNull(sepWord);
        Assert.assertNotEquals(modelA.computeOutput(sepWord), modelB.computeOutput(sepWord));

        // If we remove the timestep, should not find a counterexample:
        Set<TimedInput<String>> reducedInputs = new HashSet<>(modelA.getSemantics().getInputAlphabet());
        reducedInputs.remove(TimedInput.step());
        Assert.assertTrue(MMLTs.testEquivalence(modelA, modelB, reducedInputs));
    }

    @Test
    public void testMaximumInitialTimer() {
        var automaton = buildBaseModel();

        Assert.assertEquals(MMLTs.getMaximumInitialTimerValue(automaton), 40);

        automaton.removeTimer(1, "c");
        Assert.assertEquals(MMLTs.getMaximumInitialTimerValue(automaton), 6);
    }

    @Test
    public void testConfigurationCounter() {
        var automaton = buildBaseModel();

        var s1 = 1;
        var s2 = 2;

        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s1), 40);
        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s2), 4);

        // remove one-shots
        automaton.removeTimer(s1, "c");
        automaton.removeTimer(s2, "d");
        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s1), 6);
        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s2), 1);

        // use primes for lcm product
        automaton.removeTimer(s1, "b");
        automaton.addPeriodicTimer(s1, "b", 7, "noise");
        automaton.addPeriodicTimer(s1, "c", 11, "done");
        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s1), 231);

        // check for overflow
        automaton.removeTimer(s1, "b");
        automaton.addPeriodicTimer(s1, "b", Long.MAX_VALUE, "noise");
        Assert.assertEquals(MMLTs.getConfigurationCount(automaton, s1), Long.MAX_VALUE);
    }

    @Test
    public void testMaximumTimeoutDelay() {
        var automaton = buildBaseModel();

        Assert.assertEquals(MMLTs.getMaximumTimeoutDelay(automaton), 4);

        var s1 = 1;
        var s2 = 2;
        // remove one-shots
        automaton.removeTimer(s1, "c");
        automaton.removeTimer(s2, "d");

        automaton.removeTimer(s1, "b");
        automaton.addPeriodicTimer(s1, "b", 7, "noise");
        automaton.addPeriodicTimer(s1, "c", 19, "done");

        Assert.assertEquals(MMLTs.getMaximumTimeoutDelay(automaton), 3);
    }
}
