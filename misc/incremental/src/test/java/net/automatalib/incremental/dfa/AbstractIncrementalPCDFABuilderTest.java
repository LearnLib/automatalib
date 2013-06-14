/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.incremental.dfa;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalPCDFABuilderTest {

	private static final Alphabet<Character> testAlphabet = Alphabets.characters('a', 'c');
	
	private IncrementalDFABuilder<Character> incPcDfa;
	
	protected abstract <I>
	IncrementalDFABuilder<I> createIncrementalPCDFABuilder(Alphabet<I> alphabet);
	
	@BeforeClass
	public void setUp() {
		this.incPcDfa = createIncrementalPCDFABuilder(testAlphabet);
	}

	@Test
	public void testLookup() {
		Word<Character> w1 = Word.fromString("abc");
		Word<Character> w2 = Word.fromString("acb");
		Word<Character> w3 = Word.fromString("ac");
		
		Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.DONT_KNOW);
		Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.DONT_KNOW);
		Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);
		
		incPcDfa.insert(w1, true);
		Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w1.prefix(2)), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w1.prefix(1)), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w1.prefix(0)), Acceptance.TRUE);
		
		Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.DONT_KNOW);
		Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);
		
		incPcDfa.insert(w2, false);
		Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.FALSE);
		Assert.assertEquals(incPcDfa.lookup(w2.append('a')), Acceptance.FALSE);
		Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.DONT_KNOW);
		
		Assert.assertEquals(incPcDfa.lookup(w1.prefix(1)), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w2.prefix(1)), Acceptance.TRUE);
		
		incPcDfa.insert(w3, true);
		Assert.assertEquals(incPcDfa.lookup(w1), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w2), Acceptance.FALSE);
		Assert.assertEquals(incPcDfa.lookup(w3), Acceptance.TRUE);
		
		Assert.assertEquals(incPcDfa.lookup(w1.prefix(2)), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w2.prefix(1)), Acceptance.TRUE);
		Assert.assertEquals(incPcDfa.lookup(w3.append('a')), Acceptance.DONT_KNOW);
	}
	
	@Test(dependsOnMethods = "testLookup")
	public void testInsertSame() {
		Word<Character> w1 = Word.fromString("abc");
		int oldSize = incPcDfa.asGraph().size();
		incPcDfa.insert(w1, true);
		Assert.assertEquals(incPcDfa.asGraph().size(), oldSize);
	}
	
	@Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
	public void testConflict() {
		Word<Character> w1 = Word.fromString("abc");
		incPcDfa.insert(w1, false);
	}
	
	@Test(dependsOnMethods = "testLookup")
	public void testFindSeparatingWord() {
		CompactDFA<Character> testDfa = new CompactDFA<>(testAlphabet);
		int s0 = testDfa.addInitialState(true);
		int s1 = testDfa.addState(true);
		int s2 = testDfa.addState(true);
		int s3 = testDfa.addState(true);
		
		testDfa.addTransition(s0, 'a', s1);
		testDfa.addTransition(s1, 'b', s2);
		testDfa.addTransition(s2, 'c', s3);
		
		Word<Character> sepWord;
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, true);
		Assert.assertNull(sepWord);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, false);
		Assert.assertEquals(sepWord, Word.fromString("ac"));
		
		int s4 = testDfa.addState(true);
		testDfa.addTransition(s1, 'c', s4);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, true);
		Assert.assertNull(sepWord);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, false);
		Assert.assertNull(sepWord);
		
		int s5 = testDfa.addState(false);
		testDfa.addTransition(s4, 'b', s5);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, true);
		Assert.assertNull(sepWord);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, false);
		Assert.assertNull(sepWord);
		
		int s6 = testDfa.addState(false);
		testDfa.addTransition(s3, 'a', s6);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, true);
		Assert.assertNull(sepWord);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, false);
		Assert.assertNull(sepWord);
		
		int s7 = testDfa.addState(true);
		testDfa.addTransition(s5, 'a', s7);
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, true);
		Assert.assertEquals(sepWord, Word.fromString("acba"));
		sepWord = incPcDfa.findSeparatingWord(testDfa, testAlphabet, false);
		Assert.assertEquals(sepWord, Word.fromString("acba"));
	}

}