package net.automatalib.util.automata.transout;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class MealyFilterTest {

	private final Alphabet<Integer> testAlphabet;
	private CompactMealy<Integer, String> testMealy;
	
	public MealyFilterTest() {
		this.testAlphabet = Alphabets.integers(0, 0);
	}
	
	@BeforeClass
	public void setUp() {
		this.testMealy = fromSequence("a", "b", "c");
	}
	
	@Test
	public void testPruneTransitionWithOutput() {
		MealyMachine<?,Integer,?,String> mealy1
			= MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "c");
		Assert.assertEquals(mealy1.size(), 3);

		MealyMachine<?,Integer,?,String> mealy2
			= MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "b", "c");
		Assert.assertEquals(mealy2.size(), 2);
		
		MealyMachine<?,Integer,?,String> mealy3
			= MealyFilter.pruneTransitionsWithOutput(testMealy, testAlphabet, "a");
		Assert.assertEquals(mealy3.size(), 1);
	}
	
	
	private CompactMealy<Integer,String> fromSequence(String... outputs) {
		CompactMealy<Integer,String> mealy = new CompactMealy<Integer,String>(testAlphabet);
		
		int prev = mealy.addInitialState();
		
		for(int i = 0; i < outputs.length; i++) {
			String out = outputs[i];
			int next = mealy.addState();
			mealy.addTransition(prev, 0, next, out);
			prev = next;
		}
		
		return mealy;
	}
}
