package net.automatalib.util.automaton.cover;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.impl.mmlt.CompactLocalTimerMealy;
import net.automatalib.automaton.time.impl.mmlt.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class LocalTimerMealyCoverTest {
    private CompactLocalTimerMealy<String, String> buildBaseModel() {
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

    @Test
    public void computeFullCover() {
        var automaton = buildBaseModel();
        var cover = LocalTimerMealyCover.getLocalTimerMealyLocationCover(automaton, automaton.getSemantics().getInputAlphabet());

        // Verify that all states are covered:
        Assert.assertEquals(cover.size(), automaton.size());
        for (var state : automaton.getStates()) {
            Assert.assertTrue(cover.containsKey(state));
        }

        // Verify that the assigned prefixes are correct:
        for (var entry : cover.entrySet()) {
            var targetConfig = automaton.getSemantics().traceInputs(entry.getValue());
            Assert.assertTrue(targetConfig.isEntryConfig());
            Assert.assertEquals(targetConfig.getLocation(), entry.getKey());
        }
    }

    @Test
    public void computeIncompleteCover() {
        var automaton = buildBaseModel();

        // Create alphabet where state 3 is unreachable:
        var symbols = List.of("p1", "p2", "collect");
        GrowingMapAlphabet<LocalTimerMealySemanticInputSymbol<String>> partialAlphabet = new GrowingMapAlphabet<>();
        symbols.forEach(s -> partialAlphabet.add(new NonDelayingInput<>(s)));

        // Test if detecting incomplete cover:
        Assert.assertThrows(AssertionError.class, () -> LocalTimerMealyCover.getLocalTimerMealyLocationCover(automaton, partialAlphabet, true, false));

        // Verify that state 3 is not covered:
        var cover = LocalTimerMealyCover.getLocalTimerMealyLocationCover(automaton, partialAlphabet);
        Assert.assertTrue(cover.size() < automaton.size());
        for (var state : automaton.getStates()) {
            if (state != 3) {
                Assert.assertTrue(cover.containsKey(state));
            } else {
                Assert.assertFalse(cover.containsKey(state));
            }
        }

    }

}
