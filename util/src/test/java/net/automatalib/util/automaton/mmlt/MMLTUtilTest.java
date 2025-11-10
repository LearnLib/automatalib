package net.automatalib.util.automaton.mmlt;

import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class MMLTUtilTest {

    public CompactMMLT<String, String> buildBaseModel() {
        var symbols = List.of("p1", "p2", "abort", "collect");
        GrowingMapAlphabet<InputSymbol<String>> alphabet = new GrowingMapAlphabet<>();
        symbols.forEach(s -> alphabet.add(new InputSymbol<>(s)));

        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.setInitialState(s0);

        model.addTransition(s0, new InputSymbol<>("p1"), s1, "go");
        model.addTransition(s1, new InputSymbol<>("abort"), s1, "ok");
        model.addLocalReset(s1, new InputSymbol<>("abort"));

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, new InputSymbol<>("p2"), s2, "go");
        model.addTransition(s2, new InputSymbol<>("abort"), s3, "void");
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, new InputSymbol<>("collect"), s0, "void");

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

        model.removeLocalReset(s1, new InputSymbol<>("abort"));
        model.addLocalReset(s1, new InputSymbol<>("abort"));

        // Still needs to be equivalent to original:
        var originalModel = buildBaseModel();
        Assert.assertNull(MMLTUtil.findSeparatingWord(model,
                                                      originalModel,
                                                      originalModel.getSemantics().getInputAlphabet()));
    }

    @Test
    public void testSeparatedByResetsSimple() {
        GrowingMapAlphabet<InputSymbol<String>> alphabet = new GrowingMapAlphabet<>();
        alphabet.addSymbol(new InputSymbol<>("x"));

        // Same model, but with reset in A and no reset in B:
        var modelA = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0 = modelA.addState();
        modelA.setInitialState(s0);
        modelA.addPeriodicTimer(s0, "a", 3, "test");
        modelA.addTransition(s0, new InputSymbol<>("x"), s0, "ok");
        modelA.addLocalReset(s0, new InputSymbol<>("x"));

        var modelB = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());
        var s0B = modelB.addState();
        modelB.setInitialState(s0B);
        modelB.addPeriodicTimer(s0B, "a", 3, "test");
        modelB.addTransition(s0B, new InputSymbol<>("x"), s0B, "ok");

        Assert.assertNotNull(MMLTUtil.findSeparatingWord(modelA,
                                                         modelB,
                                                         modelA.getSemantics().getInputAlphabet()));

        // If we remove the timestep, should not find a counterexample:
        List<TimedInput<String>> reducedInputs = new ArrayList<>(modelA.getUntimedAlphabet());
        reducedInputs.add(new TimeoutSymbol<>());
        Assert.assertNull(MMLTUtil.findSeparatingWord(modelA, modelB, reducedInputs));
    }

    @Test
    public void testSeparatedByResetsComplex() {
        GrowingMapAlphabet<InputSymbol<String>> alphabet = new GrowingMapAlphabet<>();
        alphabet.addSymbol(new InputSymbol<>("x"));

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
        modelB.addTransition(s1B, new InputSymbol<>("x"), s1B, "void");
        modelB.addLocalReset(s1B, new InputSymbol<>("x"));

        Assert.assertNotNull(MMLTUtil.findSeparatingWord(modelA,
                                                         modelB,
                                                         modelA.getSemantics().getInputAlphabet()));

        // If we remove the timestep, should not find a counterexample:
        List<TimedInput<String>> reducedInputs = new ArrayList<>(modelA.getUntimedAlphabet());
        reducedInputs.add(new TimeoutSymbol<>());
        Assert.assertNull(MMLTUtil.findSeparatingWord(modelA, modelB, reducedInputs));
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
