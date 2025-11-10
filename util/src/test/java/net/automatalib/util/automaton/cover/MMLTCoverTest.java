package net.automatalib.util.automaton.cover;

import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class MMLTCoverTest {
    private CompactMMLT<String, String> buildBaseModel() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.setInitialState(s0);

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
    public void computeFullCover() {
        var automaton = buildBaseModel();
        var cover = MMLTCover.getLocalTimerMealyLocationCover(automaton, automaton.getSemantics().getInputAlphabet());

        // Verify that all states are covered:
        Assert.assertEquals(cover.size(), automaton.size());
        for (var state : automaton.getStates()) {
            Assert.assertTrue(cover.containsKey(state));
        }

        // Verify that the assigned prefixes are correct:
        for (var entry : cover.entrySet()) {
            var targetConfig = automaton.getSemantics().getState(entry.getValue());
            Assert.assertTrue(targetConfig.isEntryConfig());
            Assert.assertEquals(targetConfig.getLocation(), entry.getKey());
        }
    }

    @Test
    public void computeIncompleteCover() {
        var automaton = buildBaseModel();

        // Create alphabet where state 3 is unreachable:
        var symbols = List.of("p1", "p2", "collect");
        GrowingMapAlphabet<TimedInput<String>> partialAlphabet = new GrowingMapAlphabet<>();
        symbols.forEach(s -> partialAlphabet.add(new InputSymbol<>(s)));

        // Test if detecting incomplete cover:
        Assert.assertThrows(AssertionError.class, () -> MMLTCover.getLocalTimerMealyLocationCover(automaton, partialAlphabet, true, false));

        // Verify that state 3 is not covered:
        var cover = MMLTCover.getLocalTimerMealyLocationCover(automaton, partialAlphabet);
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
