package net.automatalib.util.automata.ads;

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests for the automata of the paper "State-Identification Experiments in Finite Automata" by Arthur Gill.
 *
 * @author frohme
 */
public class ADSGillTest extends AbstractADSTest {

	private static final CompactMealy<Character, Integer> A;

	static {
		A = AutomatonBuilders
				.forMealy(new CompactMealy<Character, Integer>(Alphabets.characters('a', 'b')))
				.withInitial("s1")
				.from("s1")
				.on('a').withOutput(0).loop()
				.on('b').withOutput(1).to("s4")
				.from("s2")
				.on('a').withOutput(0).to("s1")
				.on('b').withOutput(1).to("s5")
				.from("s3")
				.on('a').withOutput(0).to("s5")
				.on('b').withOutput(1).to("s1")
				.from("s4")
				.on('a').withOutput(1).to("s3")
				.on('b').withOutput(1).loop()
				.from("s5")
				.on('a').withOutput(1).to("s2")
				.on('b').withOutput(1).loop()
				.create();
	}

	@Test
	public void testGillExampleA_Complete() {
		super.verifyFailure(A);
	}

	@Test
	public void testGillExampleA_S2S3S4S5() {
		super.verifySuccess(A, Arrays.asList(1, 4));
	}
}
