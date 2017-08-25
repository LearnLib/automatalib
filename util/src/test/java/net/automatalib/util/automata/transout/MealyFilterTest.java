package net.automatalib.util.automata.transout;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class MealyFilterTest {

    private final Alphabet<Integer> testAlphabet;
    private CompactMealy<Integer, String> testMealy;

    public MealyFilterTest() {
        this.testAlphabet = Alphabets.integers(0, 1);
    }

    @BeforeClass
    public void setUp() {
        this.testMealy = fromSequence("a", "b", "c");
    }

    private CompactMealy<Integer, String> fromSequence(String... outputs) {
        CompactMealy<Integer, String> mealy = new CompactMealy<>(testAlphabet);

        int prev = -1; //mealy.addInitialState();

        int first = -1;

        for (int i = 0; i < outputs.length; i++) {
            String out = outputs[i];
            int next = mealy.addState();
            if (prev < 0) {
                first = next;
            } else {
                mealy.addTransition(prev, 1, next, out);
            }
            prev = next;
        }

        int init = mealy.addInitialState();
        mealy.addTransition(init, 1, first, outputs[0]);

        return mealy;
    }

    @Test
    public void testPruneTransitionWithOutput() {
        Word<Integer> testWord = Word.fromSymbols(1, 1, 1);
        Assert.assertEquals(testMealy.computeOutput(testWord), Word.fromSymbols("a", "b", "c"));

        MealyMachine<?, Integer, ?, String> mealy1 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "c");
        Assert.assertEquals(mealy1.size(), 3);
        Assert.assertEquals(mealy1.computeOutput(testWord), Word.fromSymbols("a", "b"));

        MealyMachine<?, Integer, ?, String> mealy2 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "b", "c");
        Assert.assertEquals(mealy2.size(), 2);
        Assert.assertEquals(mealy2.computeOutput(testWord), Word.fromSymbols("a"));

        MealyMachine<?, Integer, ?, String> mealy3 =
                MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "a");
        Assert.assertEquals(mealy3.size(), 1);
        Assert.assertEquals(mealy3.computeOutput(testWord), Word.epsilon());
    }
}
