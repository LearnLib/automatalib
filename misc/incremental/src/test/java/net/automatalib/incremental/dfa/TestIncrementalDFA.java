/* Copyright (C) 2013 TU Dortmund
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

import net.automatalib.incremental.ConflictException;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class TestIncrementalDFA {

	private Alphabet<Character> alphabet;
	private IncrementalDFABuilder<Character> incDfa;

	@BeforeClass
	public void setUp() {
		this.alphabet = new SimpleAlphabet<>();
		this.alphabet.add('a');
		this.alphabet.add('b');
		this.alphabet.add('c');
		
		this.incDfa = new IncrementalDFABuilder<>(alphabet);
	}

	@Test
	public void testLookup() {
		Word<Character> w1 = Word.fromString("abc");
		Word<Character> w2 = Word.fromString("ac");
		Word<Character> w3 = Word.fromString("acb");
		
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w1));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w2));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w3));
		
		incDfa.insert(w1, true);
		Assert.assertEquals(Acceptance.TRUE, incDfa.lookup(w1));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w2));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w3));
		
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w1.prefix(2)));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w2.prefix(1)));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w3.prefix(2)));
		
		incDfa.insert(w2, false);
		Assert.assertEquals(Acceptance.TRUE, incDfa.lookup(w1));
		Assert.assertEquals(Acceptance.FALSE, incDfa.lookup(w2));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w3));
		
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w1.prefix(2)));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w2.prefix(1)));
		Assert.assertEquals(Acceptance.FALSE, incDfa.lookup(w3.prefix(2)));
		
		incDfa.insert(w3, true);
		Assert.assertEquals(Acceptance.TRUE, incDfa.lookup(w1));
		Assert.assertEquals(Acceptance.FALSE, incDfa.lookup(w2));
		Assert.assertEquals(Acceptance.TRUE, incDfa.lookup(w3));
		
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w1.prefix(2)));
		Assert.assertEquals(Acceptance.DONT_KNOW, incDfa.lookup(w2.prefix(1)));
		Assert.assertEquals(Acceptance.FALSE, incDfa.lookup(w3.prefix(2)));
	}
	
	@Test(dependsOnMethods = "testLookup")
	public void testInsertSame() {
		Word<Character> w1 = Word.fromString("abc");
		incDfa.insert(w1, true);
	}
	
	@Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
	public void testConflict() {
		Word<Character> w1 = Word.fromString("abc");
		incDfa.insert(w1, false);
	}

}
