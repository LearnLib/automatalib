package net.automatalib.automaton.impl;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.automaton.time.impl.mmlt.CompactLocalTimerMealy;
import net.automatalib.automaton.time.impl.mmlt.ReducedLocalTimerMealySemantics;
import net.automatalib.automaton.time.impl.mmlt.StringSymbolCombiner;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocalTimerMealyTests {

    public CompactLocalTimerMealy<String, String> buildBaseModel() {
        var symbols = List.of("p1", "p2", "abort", "collect");
        GrowingMapAlphabet<NonDelayingInput<String>> alphabet = new GrowingMapAlphabet<>();
        symbols.forEach(s -> alphabet.add(new NonDelayingInput<>(s)));

        var model = new CompactLocalTimerMealy<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.setInitialState(s0);

        model.addTransition(s0, new NonDelayingInput<>("p1"), "go", s1);
        model.addTransition(s1, new NonDelayingInput<>("abort"), "ok", s1);
        model.addLocalReset(s1, new NonDelayingInput<>("abort"));

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, new NonDelayingInput<>("p2"), "go", s2);
        model.addTransition(s2, new NonDelayingInput<>("abort"), "void", s3);
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, new NonDelayingInput<>("collect"), "void", s0);

        return model;
    }

    private static List<String> generateRandomWords(int count, int wordLength) {
        Random r = new Random(100);
        List<String> words = new ArrayList<>();
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

        Assert.assertEquals(words.stream().sorted().toList(), separated.stream().sorted().toList());

        // Tests words with an absent output:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> combiner.combineSymbols(List.of("|", "d|c"))
        );

        var combinedAbsent = combiner.combineSymbols(List.of("a|", "d|c"));
        Assert.assertEquals(combinedAbsent, "a|c|d");

        // Now test words that contain a pipe:
        var combinedPipe = combiner.combineSymbols(List.of("b|a", "d|c"));
        Assert.assertEquals(combinedPipe, "a|b|c|d");

        // Test words with duplicate characters:
        var combinedDupes = combiner.combineSymbols(List.of("b|a", "c|a"));
        var separateDupes = combiner.separateSymbols(combinedDupes);
        Assert.assertEquals(combinedDupes, "a|b|c");
        Assert.assertEquals(List.of("a", "b", "c"), separateDupes);
    }

    @Test
    public void testConfigurationProperties() {
        var model = buildBaseModel();

        var nonStableConfig = model.getSemantics().traceInputs(Word.fromSymbols(
                new NonDelayingInput<>("p1"), new TimeStepSequence<>(12)
        ));
        Assert.assertFalse(nonStableConfig.isStableConfig());
        Assert.assertFalse(nonStableConfig.isEntryConfig());
        Assert.assertEquals(nonStableConfig.getEntryDistance(), 12);
        Assert.assertEquals(nonStableConfig.getLocation().intValue(), 1);


        var stableConfig = model.getSemantics().traceInputs(Word.fromSymbols(
                new NonDelayingInput<>("p1"), new TimeStepSequence<>(2)
        ));
        Assert.assertTrue(stableConfig.isStableConfig());
        Assert.assertFalse(stableConfig.isEntryConfig());
        Assert.assertEquals(stableConfig.getEntryDistance(), 2);
        Assert.assertEquals(stableConfig.getLocation().intValue(), 1);
    }

    @Test
    public void testReducedSemanticsIncludedConfiguration() {
        var automaton = buildBaseModel();
        var reducedSemanticsModel = ReducedLocalTimerMealySemantics.forLocalTimerMealy(automaton);
        Assert.assertEquals(reducedSemanticsModel.size(), 31);

        // Reachable in both automata:
        Word<LocalTimerMealySemanticInputSymbol<String>> includedConfigPrefix = Word.fromSymbols(
                new NonDelayingInput<>("p1"), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeoutSymbol<>(), new TimeStepSymbol<>()
        );
        var includedConfig = automaton.getSemantics().traceInputs(includedConfigPrefix);

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
        var reducedSemanticsModel = ReducedLocalTimerMealySemantics.forLocalTimerMealy(automaton);
        Assert.assertEquals(reducedSemanticsModel.size(), 31);

        // Only reachable via at least two following time steps:
        Word<LocalTimerMealySemanticInputSymbol<String>> omittedConfigPrefix = Word.fromSymbols(
                new NonDelayingInput<>("p1"), new TimeoutSymbol<>(), new TimeoutSymbol<>(),
                new TimeStepSymbol<>(), new TimeStepSymbol<>()
        );
        var omittedConfig = automaton.getSemantics().traceInputs(omittedConfigPrefix);

        // Verify that we cannot reach this state in the reduced semantics:
        Assert.assertNull(reducedSemanticsModel.getState(omittedConfigPrefix));
        Assert.assertThrows(IllegalStateException.class, () -> reducedSemanticsModel.getStateForConfiguration(omittedConfig, false));

        // Verify that the output is incomplete:
        var fullOutput = automaton.getSemantics().computeSuffixOutput(Word.epsilon(), omittedConfigPrefix);
        var reducedOutput = reducedSemanticsModel.computeOutput(omittedConfigPrefix);
        Assert.assertNotEquals(fullOutput, reducedOutput);

        // Check the approximated state:
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
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addPeriodicTimer(s1, "a", 3, "test")
        );

        // Timer with silent output:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addPeriodicTimer(s1, "e", 3, "void")
        );

        // Timer never expires:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addPeriodicTimer(s1, "e", 41, "test")
        );

        // One-shot timer that times out at same time as periodic:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addOneShotTimer(s1, "e", 12, "test", 3)
        );

        // Periodic timer that times out at same time as one-shot:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addPeriodicTimer(s1, "e", 20, "test")
        );

        // Duplicate one-shot timer:
        Assert.assertThrows(IllegalArgumentException.class,
                () -> automaton.addOneShotTimer(s1, "e", 12, "test", 3)
        );
    }
}
