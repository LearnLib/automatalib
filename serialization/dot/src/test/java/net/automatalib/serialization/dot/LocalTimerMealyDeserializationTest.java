package net.automatalib.serialization.dot;

import net.automatalib.alphabet.impl.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.StringSymbolCombiner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class LocalTimerMealyDeserializationTest {

    @Test
    public void parseSensorModel() {
        // Load a model from file:
        var resource = LocalTimerMealyDeserializationTest.class.getResource("/sensor_mmlt.dot");

        var dotAutomaton = LocalTimerMealyGraphvizParser.parseLocalTimerMealy(new File(resource.getFile()), "void", StringSymbolCombiner.getInstance());

        // Compare to reference:
        var ndiP1 = new NonDelayingInput<>("p1");
        var ndiP2 = new NonDelayingInput<>("p2");
        var ndiAbort = new NonDelayingInput<>("abort");
        var ndiCollect = new NonDelayingInput<>("collect");

        int s0 = 0;
        int s1 = 1;
        int s2 = 2;
        int s3 = 3;

        // Check non-delaying transitions:
        assertSilentLoop(dotAutomaton, s0, ndiAbort);
        assertSilentLoop(dotAutomaton, s0, ndiCollect);
        assertTransition(dotAutomaton, s0, s1, ndiP1, "go");
        assertTransition(dotAutomaton, s0, s2, ndiP2, "go");

        assertTransition(dotAutomaton, s1, s1, ndiAbort, "ok");
        Assert.assertTrue(dotAutomaton.isLocalReset(s1, ndiAbort));
        assertSilentLoop(dotAutomaton, s1, ndiP1);
        assertSilentLoop(dotAutomaton, s1, ndiP2);
        assertSilentLoop(dotAutomaton, s1, ndiCollect);

        assertTransition(dotAutomaton, s2, s3, ndiAbort, "void");
        assertSilentLoop(dotAutomaton, s2, ndiP1);
        assertSilentLoop(dotAutomaton, s2, ndiP2);
        assertSilentLoop(dotAutomaton, s2, ndiCollect);

        assertSilentLoop(dotAutomaton, s3, ndiAbort);
        assertSilentLoop(dotAutomaton, s3, ndiP1);
        assertSilentLoop(dotAutomaton, s3, ndiP2);
        assertTransition(dotAutomaton, s3, s0, ndiCollect, "void");

        // Check timers:
        Assert.assertTrue(dotAutomaton.getSortedTimers(s0).isEmpty());
        Assert.assertTrue(dotAutomaton.getSortedTimers(s3).isEmpty());
        Assert.assertEquals(dotAutomaton.getSortedTimers(s1).size(), 3);
        Assert.assertEquals(dotAutomaton.getSortedTimers(s2).size(), 1);

        var firstTimerS1 = dotAutomaton.getSortedTimers(s1).get(0);
        Assert.assertEquals(firstTimerS1.initial(), 3);
        Assert.assertEquals(firstTimerS1.output(), "part");
        Assert.assertTrue(firstTimerS1.periodic());

        var secondTimerS1 = dotAutomaton.getSortedTimers(s1).get(1);
        Assert.assertEquals(secondTimerS1.initial(), 6);
        Assert.assertEquals(secondTimerS1.output(), "noise");
        Assert.assertTrue(secondTimerS1.periodic());

        var thirdTimerS1 = dotAutomaton.getSortedTimers(s1).get(2);
        Assert.assertEquals(thirdTimerS1.initial(), 40);
        Assert.assertEquals(thirdTimerS1.output(), "done");
        Assert.assertFalse(thirdTimerS1.periodic());

        var firstTimerS2 = dotAutomaton.getSortedTimers(s2).get(0);
        Assert.assertEquals(firstTimerS2.initial(), 4);
        Assert.assertEquals(firstTimerS2.output(), "done");
        Assert.assertFalse(firstTimerS2.periodic());
    }

    private void assertTransition(LocalTimerMealy<Integer, String, String> model, int state, int target, NonDelayingInput<String> input, String output) {
        var trans = model.getTransition(state, input);
        if (trans == null || (trans.successor() != target) || !trans.output().equals(output)) {
            throw new AssertionError();
        }
    }

    private void assertSilentLoop(LocalTimerMealy<Integer, String, String> model, int state, NonDelayingInput<String> input) {
        var trans = model.getTransition(state, input);
        if (trans != null && (trans.successor() != state || !trans.output().equals("void"))) {
            throw new AssertionError();
        }
        Assert.assertFalse(model.isLocalReset(state, input));
    }


}
