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
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class EmptyWordTest extends AbstractWordTest {

	

	@Override
	@Test
	public void testIsEmpty() {
		super.testIsEmpty();
		Assert.assertTrue(testWord.isEmpty());
	}
	
	@Test
	public void testLength() {
		Assert.assertEquals(0, testWord.length());
	}

	@Override
	protected Word<Object> testWord() {
		return new EmptyWord();
	}

	@Override
	protected List<Word<Object>> equalWords() {
		return Arrays.<Word<Object>>asList(new SharedWord<>(new Object[0]),
				new SharedWord<>(Collections.emptyList()),
				new SharedWord<>(new Object[3], 2, 0));
	}

	@Override
	protected List<Word<Object>> unequalWords() {
		return Arrays.<Word<Object>>asList(new LetterWord<>(new Object()),
				new SharedWord<>(new Object[3]),
				new SharedWord<>(Arrays.<Object>asList(1, 2, 3)));
	}
	
	

}
