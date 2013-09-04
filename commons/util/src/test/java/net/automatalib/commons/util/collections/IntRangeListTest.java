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
package net.automatalib.commons.util.collections;

import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class IntRangeListTest {
	
	private IntRange ir0, ir1;
	
	@BeforeClass
	public void setup() {
		ir0 = new IntRange(10, 20);
		ir1 = new IntRange(20, 30, 3);
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(ir0.size(), 10);
		Assert.assertEquals(ir1.size(), 4);
	}
	
	@Test
	public void testGet() {
		Assert.assertEquals(ir0.get(0).intValue(), 10);
		Assert.assertEquals(ir0.get(4).intValue(), 14);
		
		Assert.assertEquals(ir1.get(2).intValue(), 26);
		Assert.assertEquals(ir1.get(3).intValue(), 29);
	}
	
	@Test
	public void testIterator() {
		testIterator(ir0);
		testIterator(ir1);
	}
	
	private static <T> void testIterator(List<T> lst) {
		Iterator<T> it = lst.iterator();
		
		int idx = 0;
		
		while(it.hasNext()) {
			T itObj = it.next();
			T lstObj = lst.get(idx++);
			Assert.assertEquals(itObj, lstObj);
		}
		
		Assert.assertEquals(idx, lst.size());
	}
}
