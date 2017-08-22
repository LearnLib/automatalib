/* Copyright (C) 2017 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.util.automata.ads;

import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests for the automata of the chapter "State Identification" (in "Model-Based Testing of Reactive Systems") by
 * Moez Krichen.
 *
 * @author frohme
 */
public class ADSKrichenTest extends AbstractADSTest {

	// Examples from the paper
	private static final CompactMealy<Character, Integer> M3, M4, M5, M6;

	static {
		M3 = AutomatonBuilders
				.forMealy(new CompactMealy<Character, Integer>(Alphabets.characters('a', 'b')))
				.withInitial("s1")
				.from("s1")
				.on('a').withOutput(0).loop()
				.on('b').withOutput(1).to("s3")
				.from("s2")
				.on('a').withOutput(0).to("s1")
				.on('b').withOutput(0).to("s3")
				.from("s3")
				.on('a').withOutput(0).to("s2")
				.on('b').withOutput(0).loop()
				.create();

		M4 = AutomatonBuilders
				.forMealy(new CompactMealy<Character, Integer>(Alphabets.characters('a', 'b')))
				.withInitial("s1")
				.from("s1")
				.on('a').withOutput(0).to("s3")
				.on('b').withOutput(0).loop()
				.from("s2")
				.on('a').withOutput(0).to("s4")
				.on('b').withOutput(0).to("s1")
				.from("s3")
				.on('a').withOutput(1).to("s1")
				.on('b').withOutput(0).loop()
				.from("s4")
				.on('a').withOutput(1).to("s2")
				.on('b').withOutput(1).loop()
				.create();

		M5 = AutomatonBuilders
				.forMealy(new CompactMealy<Character, Integer>(Alphabets.characters('a', 'b')))
				.withInitial("s1")
				.from("s1")
				.on('a').withOutput(0).loop()
				.on('b').withOutput(1).to("s3")
				.from("s2")
				.on('a').withOutput(0).loop()
				.on('b').withOutput(0).to("s1")
				.from("s3")
				.on('a').withOutput(1).loop()
				.on('b').withOutput(1).to("s2")
				.create();

		M6 = AutomatonBuilders
				.forMealy(new CompactMealy<Character, Integer>(Alphabets.characters('a', 'b')))
				.withInitial("s1")
				.from("s1")
				.on('a').withOutput(0).to("s2")
				.on('b').withOutput(0).loop()
				.from("s2")
				.on('a').withOutput(1).to("s3")
				.on('b').withOutput(0).to("s1")
				.from("s3")
				.on('a', 'b').withOutput(0).to("s4")
				.from("s4")
				.on('a').withOutput(1).to("s5")
				.on('b').withOutput(0).to("s5")
				.from("s5")
				.on('a', 'b').withOutput(0).to("s6")
				.from("s6")
				.on('a').withOutput(1).to("s1")
				.on('b').withOutput(0).to("s1")
				.create();
	}

	@Test
	public void testKrichenExampleM3_Complete() {
		super.verifyFailure(M3);
	}

	@Test
	public void testKrichenExampleM3_S0S1() {
		super.verifySuccess(M3, Arrays.asList(0, 1));
	}

	@Test
	public void testKrichenExampleM3_S0S2() {
		super.verifySuccess(M3, Arrays.asList(0, 2));
	}

	@Test
	public void testKrichenExampleM3_S1S2() {
		super.verifySuccess(M3, Arrays.asList(1, 2));
	}

	@Test
	public void testKrichenExampleM4_Complete() {
		super.verifySuccess(M4);
	}

	@Test
	public void testKrichenExampleM5_Complete() {
		super.verifySuccess(M5);
	}

	@Test
	public void testKrichenExampleM6_Complete() {
		super.verifySuccess(M6);
	}

	@Test
	public void testKrichenExampleM6_Complete_S2S4() {
		super.verifySuccess(M6, Arrays.asList(2,4));
	}


}
