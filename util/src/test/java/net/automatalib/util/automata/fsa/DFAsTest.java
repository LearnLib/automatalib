/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.util.automata.fsa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DFAsTest {
	
	private static final boolean[] vector1 = { true, true, false, false };
	private static final boolean[] vector1Neg = { false, false, true, true };
	private static final boolean[] vector2 = { true, false, true, false };
	
	// The precomputed results of applying the operations on vector1 and vector2
	private static final boolean[] andResult = { true, false, false, false };
	private static final boolean[] orResult = { true, true, true, false };
	private static final boolean[] xorResult = { false, true, true, false };
	private static final boolean[] equivResult = { true, false, false, true };
	private static final boolean[] implResult = { true, false, true, true };
	
	private final Alphabet<Integer> testAlphabet;
	private CompactDFA<Integer> testDfa1, testDfa2;

	public DFAsTest() {
		this.testAlphabet = Alphabets.integers(0, 0);
	}
	
	@BeforeClass
	public void setUp() {
		this.testDfa1 = forVector(vector1);
		this.testDfa2 = forVector(vector2);
	}
	
	
	@Test
	public void testAnd() {
		DFA<?,Integer> expected = forVector(andResult);
		DFA<?,Integer> actual = DFAs.and(testDfa1, testDfa2, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}
	
	@Test
	public void testOr() {
		DFA<?,Integer> expected = forVector(orResult);
		DFA<?,Integer> actual = DFAs.or(testDfa1, testDfa2, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}
	
	@Test
	public void testXor() {
		DFA<?,Integer> expected = forVector(xorResult);
		DFA<?,Integer> actual = DFAs.xor(testDfa1, testDfa2, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}
	
	@Test
	public void testEquiv() {
		DFA<?,Integer> expected = forVector(equivResult);
		DFA<?,Integer> actual = DFAs.equiv(testDfa1, testDfa2, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}
	
	@Test
	public void testImpl() {
		DFA<?,Integer> expected = forVector(implResult);
		DFA<?,Integer> actual = DFAs.impl(testDfa1, testDfa2, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}
	
	@Test
	public void testComplement() {
		DFA<?,Integer> expected = forVector(vector1Neg);
		DFA<?,Integer> actual = DFAs.complement(testDfa1, testAlphabet);
		
		Assert.assertTrue(Automata.testEquivalence(actual, expected, testAlphabet));
	}

	private CompactDFA<Integer> forVector(boolean... boolVec) {
		if(boolVec.length == 0) {
			throw new IllegalArgumentException("Length of vector must be non-zero");
		}
		
		CompactDFA<Integer> result = new CompactDFA<>(testAlphabet, boolVec.length);
		
		int first = result.addInitialState(boolVec[0]);
		int prev = first;
		
		for(int i = 1; i < boolVec.length; i++) {
			int next = result.addState(boolVec[i]);
			result.addTransition(prev, 0, next);
			prev = next;
		}
		
		result.addTransition(prev, 0, first);
		
		return result;
	}
	
}
