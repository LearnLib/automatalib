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
package net.automatalib.automaton.mmlt.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MMLTTests {

    public CompactMMLT<String, String> buildBaseModel() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addIntInitialState();
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

    private static List<String> generateRandomWords(int count, int wordLength) {
        Random r = new Random(100);
        List<String> words = new ArrayList<>(count);
        for (int j = 0; j < count; j++) {
            StringBuilder sb = new StringBuilder(wordLength);
            for (int i = 0; i < wordLength; i++) {
                char tmp = (char) ('a' + r.nextInt('z' - 'a'));
                sb.append(tmp);
            }
            words.add(sb.toString());
        }
        return words;
    }

    @Test
    public void testStringCombiner() {
        var combiner = StringSymbolCombiner.getInstance();
        var words = generateRandomWords(100, 10);

        var combined = combiner.combineSymbols(words);
        Assert.assertTrue(combiner.isCombinedSymbol(combined));
        var separated = combiner.separateSymbols(combined);

        Assert.assertEquals(words, separated);

        // Tests words with an absent output:
        Assert.assertThrows(IllegalArgumentException.class, () -> combiner.combineSymbols(List.of("|", "d|c")));

        var combinedAbsent = combiner.combineSymbols(List.of("a|", "d|c"));
        Assert.assertEquals(combinedAbsent, "a|d|c");

        // Now test words that contain a pipe:
        var combinedPipe = combiner.combineSymbols(List.of("b|a", "d|c"));
        Assert.assertEquals(combinedPipe, "b|a|d|c");

        // Test words with duplicate characters:
        var combinedDupes = combiner.combineSymbols(List.of("b|a", "c|a"));
        var separateDupes = combiner.separateSymbols(combinedDupes);
        Assert.assertEquals(combinedDupes, "b|a|c");
        Assert.assertEquals(List.of("b", "a", "c"), separateDupes);
    }

    @Test
    public void testConfigurationProperties() {
        var model = buildBaseModel();

        var nonStableConfig =
                model.getSemantics().getState(Word.fromSymbols(TimedInput.input("p1"), TimedInput.step(12)));
        Assert.assertNotNull(nonStableConfig);
        Assert.assertFalse(nonStableConfig.isStableConfig());
        Assert.assertFalse(nonStableConfig.isEntryConfig());
        Assert.assertEquals(nonStableConfig.getEntryDistance(), 12);
        Assert.assertEquals(nonStableConfig.getLocation().intValue(), 1);

        var stableConfig = model.getSemantics().getState(Word.fromSymbols(TimedInput.input("p1"), TimedInput.step(2)));
        Assert.assertNotNull(stableConfig);
        Assert.assertTrue(stableConfig.isStableConfig());
        Assert.assertFalse(stableConfig.isEntryConfig());
        Assert.assertEquals(stableConfig.getEntryDistance(), 2);
        Assert.assertEquals(stableConfig.getLocation().intValue(), 1);
    }

    @Test
    public void testReducedSemanticsIncludedConfiguration() {
        var automaton = buildBaseModel();
        var reducedSemanticsModel = ReducedMMLTSemantics.forMMLT(automaton);
        Assert.assertEquals(reducedSemanticsModel.size(), 31);

        // Reachable in both automata:
        Word<TimedInput<String>> includedConfigPrefix =
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(6), TimedInput.steps(1));
        var includedConfig = automaton.getSemantics().getState(includedConfigPrefix);

        // Verify that the reached states are identical:
        var expectedState = reducedSemanticsModel.getStateForConfiguration(includedConfig, false);
        var reachedState = reducedSemanticsModel.getState(includedConfigPrefix);

        // Verify that the output is identical:
        var fullOutput = automaton.getSemantics().computeSuffixOutput(Word.epsilon(), includedConfigPrefix);
        var reducedOutput = reducedSemanticsModel.computeOutput(includedConfigPrefix);
        Assert.assertEquals(fullOutput, reducedOutput);

        Assert.assertEquals(expectedState, reachedState);
    }

    @Test
    public void testReducedSemanticsOmittedConfiguration() {
        var automaton = buildBaseModel();
        var reducedSemanticsModel = ReducedMMLTSemantics.forMMLT(automaton);
        Assert.assertEquals(reducedSemanticsModel.size(), 31);

        // Only reachable via at least two following time steps:
        Word<TimedInput<String>> omittedConfigPrefix =
                Word.fromWords(TimedInput.inputs("p1"), TimedInput.timeouts(2), TimedInput.steps(2));
        var omittedConfig = automaton.getSemantics().getState(omittedConfigPrefix);

        // Verify that we cannot reach this state in the reduced semantics:
        Assert.assertNull(reducedSemanticsModel.getState(omittedConfigPrefix));
        Assert.assertThrows(IllegalStateException.class,
                            () -> reducedSemanticsModel.getStateForConfiguration(omittedConfig, false));

        // Verify that the output is incomplete:
        var fullOutput = automaton.getSemantics().computeSuffixOutput(Word.epsilon(), omittedConfigPrefix);
        var reducedOutput = reducedSemanticsModel.computeOutput(omittedConfigPrefix);
        Assert.assertNotEquals(fullOutput, reducedOutput);

        // Check the approximated state:
        Assert.assertNotNull(omittedConfig);
        var approxStateId = reducedSemanticsModel.getStateForConfiguration(omittedConfig, true);
        var approxConfig = reducedSemanticsModel.getConfigurationForState(approxStateId);

        Assert.assertEquals(approxConfig.getLocation(), omittedConfig.getLocation());
        Assert.assertEquals(approxConfig.getEntryDistance(), 7);
    }

    @Test
    public void testInvalidTimerChecks() {
        var automaton = buildBaseModel();

        int s1 = 1;

        // Duplicate timer name:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addPeriodicTimer(s1, "a", 3, "test"));

        // Timer with silent output:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addPeriodicTimer(s1, "e", 3, "void"));

        // Timer never expires:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addPeriodicTimer(s1, "e", 41, "test"));

        // One-shot timer that times out at same time as periodic:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addOneShotTimer(s1, "e", 12, "test", 3));

        // Periodic timer that times out at same time as one-shot:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addPeriodicTimer(s1, "e", 20, "test"));

        // Duplicate one-shot timer:
        Assert.assertThrows(IllegalArgumentException.class, () -> automaton.addOneShotTimer(s1, "e", 12, "test", 3));
    }
}
