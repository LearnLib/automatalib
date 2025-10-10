package net.automatalib.util.automaton.mmlt;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.impl.mmlt.CompactLocalTimerMealy;
import net.automatalib.automaton.time.impl.mmlt.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;


import java.util.ArrayList;
import java.util.List;

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

        model.removeLocalReset(s1, new NonDelayingInput<>("abort"));
        model.addLocalReset(s1, new NonDelayingInput<>("abort"));

        // Still needs to be equivalent to original:
        var originalModel = buildBaseModel();
        Assert.assertNull(LocalTimerMealyUtil.findSeparatingWord(model, originalModel, originalModel.getSemantics().getInputAlphabet()));
    }

    @Test
    public void testSeparatedByResetsSimple() {
        GrowingMapAlphabet<NonDelayingInput<String>> alphabet = new GrowingMapAlphabet<>();
        alphabet.addSymbol(new NonDelayingInput<>("x"));

        // Same model, but with reset in A and no reset in B:
        var modelA = new CompactLocalTimerMealy<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0 = modelA.addState();
        modelA.setInitialState(s0);
        modelA.addPeriodicTimer(s0, "a", 3, "test");
        modelA.addTransition(s0, new NonDelayingInput<>("x"), "ok", s0);
        modelA.addLocalReset(s0, new NonDelayingInput<>("x"));

        var modelB = new CompactLocalTimerMealy<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0B = modelB.addState();
        modelB.setInitialState(s0B);
        modelB.addPeriodicTimer(s0B, "a", 3, "test");
        modelB.addTransition(s0B, new NonDelayingInput<>("x"), "ok", s0B);

        Assert.assertNotNull(LocalTimerMealyUtil.findSeparatingWord(modelA, modelB, modelA.getSemantics().getInputAlphabet()));

        // If we remove the timestep, should not find a counterexample:
        List<LocalTimerMealySemanticInputSymbol<String>> reducedInputs = new ArrayList<>(modelA.getUntimedAlphabet());
        reducedInputs.add(new TimeoutSymbol<>());
        Assert.assertNull(LocalTimerMealyUtil.findSeparatingWord(modelA, modelB, reducedInputs));
    }

    @Test
    public void testSeparatedByResetsComplex() {
        GrowingMapAlphabet<NonDelayingInput<String>> alphabet = new GrowingMapAlphabet<>();
        alphabet.addSymbol(new NonDelayingInput<>("x"));

        // Same model, but with reset in A and no reset in B:
        var modelA = new CompactLocalTimerMealy<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0 = modelA.addState();
        var s1 = modelA.addState();
        modelA.setInitialState(s0);
        modelA.addPeriodicTimer(s0, "a", 3, "test");
        modelA.addOneShotTimer(s0, "b", 5, "test2", s1);

        var modelB = new CompactLocalTimerMealy<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0B = modelB.addState();
        var s1B = modelB.addState();
        var s2B = modelB.addState();
        modelB.setInitialState(s0B);
        modelB.addOneShotTimer(s0B, "a", 3, "test", s1B);
        modelB.addOneShotTimer(s1B, "b", 2, "test2", s2B);
        modelB.addTransition(s1B, new NonDelayingInput<>("x"), "void", s1B);
        modelB.addLocalReset(s1B, new NonDelayingInput<>("x"));

        Assert.assertNotNull(LocalTimerMealyUtil.findSeparatingWord(modelA, modelB, modelA.getSemantics().getInputAlphabet()));

        // If we remove the timestep, should not find a counterexample:
        List<LocalTimerMealySemanticInputSymbol<String>> reducedInputs = new ArrayList<>(modelA.getUntimedAlphabet());
        reducedInputs.add(new TimeoutSymbol<>());
        Assert.assertNull(LocalTimerMealyUtil.findSeparatingWord(modelA, modelB, reducedInputs));
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
