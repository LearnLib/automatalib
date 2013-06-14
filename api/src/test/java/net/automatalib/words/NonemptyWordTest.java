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
package net.automatalib.words;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class NonemptyWordTest extends AbstractWordTest {
	
	protected List<Word<Object>> realPrefixes;
	protected List<Word<Object>> realSuffixes;
	
	protected abstract List<Word<Object>> realPrefixes();
	protected abstract List<Word<Object>> realSuffixes();
	
	@BeforeClass
	@Override
	public void setup() {
		super.setup();
		this.realPrefixes = realPrefixes();
		this.realSuffixes = realSuffixes();
	}
	
	@Override
	@Test
	public void testIsEmpty() {
		Assert.assertFalse(testWord.isEmpty());
	}
	
	@Override
	@Test
	public void testLongestCommonPrefix() {
		super.testLongestCommonPrefix();
		
		for(Word<Object> rp : realPrefixes) {
			Word<Object> lcp = testWord.longestCommonPrefix(rp);
			Assert.assertEquals(rp, lcp);
		}
	}
	@Override
	@Test
	public void testLongestCommonSuffix() {
		super.testLongestCommonSuffix();
		
		for(Word<Object> rs : realSuffixes) {
			Word<Object> lcs = testWord.longestCommonSuffix(rs);
			Assert.assertEquals(rs, lcs);
		}
	}
	
	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void testSubword3() {
		testWord.subWord(testWord.length(), 0);
	}
	
	
	
}
