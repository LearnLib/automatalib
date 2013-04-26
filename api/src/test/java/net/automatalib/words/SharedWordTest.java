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

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

@Test
public class SharedWordTest extends NonemptyWordTest {
	
	private static final Object[] DATA = new Object[]{2, 1, 3, 3, 7, 9};
	private static final int OFFSET = 1;
	private static final int LENGTH = 4;

	
	@Test
	public void testLength() {
		Assert.assertEquals(LENGTH, testWord.length());
	}


	@Override
	@Test
	public void testAsList() {
		super.testAsList();
		Assert.assertEquals(Arrays.asList(1, 3, 3, 7), testWord.asList());
	}


	@Override
	protected Word<Object> testWord() {
		return new SharedWord<>(DATA, OFFSET, LENGTH);
	}


	@Override
	protected List<Word<Object>> equalWords() {
		return Arrays.<Word<Object>>asList(new SharedWord<Object>(Arrays.asList(1, 3, 3, 7)),
				new SharedWord<>(new Object[]{1, 3, 3, 7}));
	}


	@Override
	protected List<Word<Object>> unequalWords() {
		return Arrays.<Word<Object>>asList(new SharedWord<>(DATA),
				new SharedWord<>(Arrays.asList(DATA)),
				new EmptyWord(),
				new SharedWord<Object>(Arrays.asList(2, 4)));
	}


	@Override
	protected List<Word<Object>> realPrefixes() {
		return Arrays.<Word<Object>>asList(new EmptyWord(), new SharedWord<Object>(Arrays.asList(1, 3)),
				new SharedWord<>(new Object[]{1, 3, 3}),
				new LetterWord<Object>(1));
	}


	@Override
	protected List<Word<Object>> realSuffixes() {
		return Arrays.<Word<Object>>asList(new EmptyWord(), new SharedWord<Object>(Arrays.asList(3, 7)),
				new SharedWord<>(new Object[]{3, 3, 7}),
				new LetterWord<Object>(7));
	}
	

}
