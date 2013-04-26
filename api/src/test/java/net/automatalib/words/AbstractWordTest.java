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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.words;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public abstract class AbstractWordTest {
	
	protected Word<Object> testWord;
	protected List<Word<Object>> equalWords;
	protected List<Word<Object>> unequalWords;
	protected List<Word<Object>> all;
	
	
	protected abstract Word<Object> testWord();
	protected abstract List<Word<Object>> equalWords();
	protected abstract List<Word<Object>> unequalWords();
	
	@BeforeClass
	public void setup() {
		this.testWord = testWord();
		this.equalWords = equalWords();
		this.unequalWords = unequalWords();
		all = new ArrayList<>();
		all.add(testWord);
		all.addAll(equalWords);
		all.addAll(unequalWords);
	}
	
	@Test
	public void testAppend() {
		Object appSymbol = new Object();
		int oldLen = testWord.length();
		Word<Object> appended = testWord.append(appSymbol);
		Assert.assertEquals(oldLen, testWord.length());
		
		Assert.assertEquals(testWord.length() + 1, appended.length());
		
		Assert.assertEquals(appSymbol, appended.lastSymbol());
		
		Assert.assertEquals(testWord, appended.subWord(0, testWord.length()));
	}
	
	@Test
	public void testPrepend() {
		Object prepSymbol = new Object();
		int oldLen = testWord.length();
		Word<Object> prepended = testWord.prepend(prepSymbol);
		Assert.assertEquals(oldLen, testWord.length());
		
		Assert.assertEquals(testWord.length() + 1, prepended.length());
		
		Assert.assertEquals(prepSymbol, prepended.getSymbol(0));
		Assert.assertEquals(testWord, prepended.subWord(1, testWord.length() + 1));
	}
	
	@Test
	public void testWriteToArray() {
		Object[] array = new Object[testWord.length()];
		testWord.writeToArray(0, array, 0, array.length);
		
		for(int i = 0; i < array.length; i++)
			Assert.assertEquals(testWord.getSymbol(i), array[i]);
		
		
		Object[] fingerprint = new Object[5];
		for(int i = 0; i < fingerprint.length; i++)
			fingerprint[i] = new Object();
		
		array = new Object[2*fingerprint.length + testWord.length()];
		
		System.arraycopy(fingerprint, 0, array, 0, fingerprint.length);
		System.arraycopy(fingerprint, 0, array, testWord.length() + fingerprint.length, fingerprint.length);
		
		testWord.writeToArray(0, array, fingerprint.length, testWord.length());
		
		for(int i = 0; i < fingerprint.length; i++)
			Assert.assertEquals(fingerprint[i], array[i]);
		
		for(int i = 0; i < testWord.length(); i++)
			Assert.assertEquals(testWord.getSymbol(i), array[fingerprint.length + i]);
		
		for(int i = 0; i < fingerprint.length; i++)
			Assert.assertEquals(fingerprint[i], array[fingerprint.length + testWord.length() + i]);
	}
	
	@Test
	public void testConcat() {
		Word<Object> unchanged = testWord.concat();
		Assert.assertEquals(testWord.length(), unchanged.length());
		Assert.assertEquals(testWord, unchanged);
		
		for(Word<Object> other : all) {
			Word<Object> concated = testWord.concat(other, testWord);
			Assert.assertEquals(2*testWord.length() + other.length(), concated.length());
			Assert.assertEquals(testWord, concated.subWord(0, testWord.length()));
			Assert.assertEquals(other, concated.subWord(testWord.length(), testWord.length() + other.length()));
			Assert.assertEquals(testWord, concated.subWord(testWord.length() + other.length(), concated.length()));
			
			Assert.assertTrue(testWord.isPrefixOf(concated));
			Assert.assertTrue(testWord.isSuffixOf(concated));
		}
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(testWord.length(), testWord.size());
	}
	
	
	@Test
	public void testAsList() {
		List<Object> list = testWord.asList();
		
		Assert.assertEquals(testWord.length(), list.size());
		
		for(int i = 0; i < testWord.length(); i++)
			Assert.assertEquals(testWord.getSymbol(i), list.get(i));
	}
	
	@Test
	public void testIsEmpty() {
		Assert.assertEquals((testWord.length() == 0), testWord.isEmpty());
	}
	
	@Test
	public void testIsPrefixOf() {
		Assert.assertTrue(testWord.isPrefixOf(testWord));
		
		for(Word<Object> eq : equalWords)
			Assert.assertTrue(testWord.isPrefixOf(eq));
	}
	
	@Test
	public void testIsSuffixOf() {
		Assert.assertTrue(testWord.isSuffixOf(testWord));
		
		for(Word<Object> eq : equalWords)
			Assert.assertTrue(testWord.isSuffixOf(eq));
	}
	
	@Test
	public void testLongestCommonPrefix() {
		Assert.assertEquals(testWord, testWord.longestCommonPrefix(testWord));
	}
	
	@Test
	public void testLongestCommonSuffix() {
		Assert.assertEquals(testWord, testWord.longestCommonSuffix(testWord));
	}
	
	@Test
	public void testEquals() {
		Assert.assertTrue(testWord.equals(testWord));
		Assert.assertFalse(testWord.equals(null));
		
		for(Word<Object> eq : equalWords) {
			Assert.assertTrue(testWord.equals(eq));
			Assert.assertTrue(eq.equals(testWord));
		}
		
		for(Word<Object> neq : unequalWords) {
			Assert.assertFalse(testWord.equals(neq));
			Assert.assertFalse(neq.equals(testWord));
		}
	}
	
	@Test
	public void testIterator() {
		Iterator<Object> it = testWord.iterator();
		Assert.assertNotNull(it);
		
		for(int i = 0; i < testWord.length(); i++) {
			Assert.assertTrue(it.hasNext());
			Assert.assertEquals(testWord.getSymbol(i), it.next());
		}
		
		Assert.assertFalse(it.hasNext());
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testSubword1() {
		testWord.subWord(-1, testWord.length());
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testSubword2() {
		testWord.subWord(0, testWord.length()+1);
	}
	
	@Test
	public void testPrefix() {
		for(int i = 0; i <= testWord.length(); i++) {
			Word<Object> pref = testWord.prefix(i);
			Assert.assertEquals(i, pref.length());
			Assert.assertEquals(testWord.subWord(0, i), pref);
			Assert.assertTrue(pref.isPrefixOf(testWord));
			
			if(i > 0) {
				pref = testWord.prefix(-i);
				Assert.assertEquals(testWord.length() - i, pref.length());
				Assert.assertEquals(testWord.subWord(0, testWord.length() - i), pref);
				Assert.assertTrue(pref.isPrefixOf(testWord));
			}
		}
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testPrefix1() {
		testWord.prefix(testWord.length() + 1);
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testPrefix2() {
		testWord.prefix(-testWord.length() - 1);
	}
	
	@Test
	public void testSuffix() {
		for(int i = 0; i <= testWord.length(); i++) {
			Word<Object> suff = testWord.suffix(i);
			Assert.assertEquals(i, suff.length());
			Assert.assertEquals(testWord.subWord(testWord.length() - i, testWord.length()), suff);
			Assert.assertTrue(suff.isSuffixOf(testWord));
			
			if(i > 0) {
				suff = testWord.suffix(-i);
				Assert.assertEquals(testWord.length() - i, suff.length());
				Assert.assertEquals(testWord.subWord(i, testWord.length()), suff);
				Assert.assertTrue(suff.isSuffixOf(testWord));
			}
		}
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testSuffix1() {
		testWord.suffix(testWord.length() + 1);
	}
	
	@Test(expectedExceptions={IndexOutOfBoundsException.class})
	public void testSuffix2() {
		testWord.suffix(-testWord.length() - 1);
	}
	
}
