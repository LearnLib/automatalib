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
package net.automatalib.incremental.mealy;

import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractIncrementalMealyBuilderTest {
	
	private static final Alphabet<Character> testAlphabet = Alphabets.characters('a', 'c');
	private static final Word<Character> w1 = Word.fromString("abc");
	private static final Word<Character> w1o = Word.fromString("xyz");
	private static final Word<Character> w2 = Word.fromString("ac");
	private static final Word<Character> w2o = Word.fromString("xw");
	private static final Word<Character> w3 = Word.fromString("acb");
	private static final Word<Character> w3o = Word.fromString("xwu");

	private IncrementalMealyBuilder<Character,Character> incMealy;
	
	
	protected abstract <I,O> IncrementalMealyBuilder<I,O> createIncrementalMealyBuilder(Alphabet<I> alphabet);
	
	@BeforeClass
	public void setUp() {
		this.incMealy = createIncrementalMealyBuilder(testAlphabet);
	}

	@Test
	public void testLookup() {
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w1));
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w2));
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w3));
		

		
		
		incMealy.insert(w1, w1o);
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w1));
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w1.prefix(2)));
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w1.append('a')));
		
		WordBuilder<Character> wb = new WordBuilder<>();
		
		Assert.assertTrue(incMealy.lookup(w1, wb));
		Assert.assertEquals(wb.toWord(), w1o);
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w1.prefix(2), wb));
		Assert.assertEquals(wb.toWord(), w1o.prefix(2));
		wb.clear();
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w2));
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w3));
		
		
		incMealy.insert(w2, w2o);
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w1));
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w2));
		Assert.assertFalse(incMealy.hasDefinitiveInformation(w3));
		
		Assert.assertTrue(incMealy.lookup(w2, wb));
		Assert.assertEquals(wb.toWord(), w2o);
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w2.prefix(1), wb));
		Assert.assertEquals(wb.toWord(), w2o.prefix(1));
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w1, wb));
		Assert.assertEquals(wb.toWord(), w1o);
		wb.clear();
		
		
			
		incMealy.insert(w3, w3o);
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w1));
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w2));
		Assert.assertTrue(incMealy.hasDefinitiveInformation(w3));
		
		Assert.assertTrue(incMealy.lookup(w3, wb));
		Assert.assertEquals(wb.toWord(), w3o);
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w3.prefix(2), wb));
		Assert.assertEquals(wb.toWord(), w3o.prefix(2));
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w1, wb));
		Assert.assertEquals(wb.toWord(), w1o);
		wb.clear();
		Assert.assertTrue(incMealy.lookup(w2, wb));
		Assert.assertEquals(wb.toWord(), w2o);
		wb.clear();
	}
	
	@Test(dependsOnMethods = "testLookup")
	public void testInsertSame() {
		incMealy.insert(w1, w1o);
	}
	
	@Test(expectedExceptions = ConflictException.class, dependsOnMethods = "testLookup")
	public void testConflict() {
		incMealy.insert(w1, w3o);
	}

}
