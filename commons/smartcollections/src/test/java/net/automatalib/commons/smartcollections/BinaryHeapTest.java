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
package net.automatalib.commons.smartcollections;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class BinaryHeapTest {
	
	private final BinaryHeap<Integer> heap = BinaryHeap.create(Arrays.asList(42, 37));
	
	@Test
	public void testHeapOps() {
		Assert.assertEquals(heap.size(), 2);
		Assert.assertFalse(heap.isEmpty());
		Assert.assertFalse(heap.contains(13));
		
		Assert.assertEquals(heap.peekMin().intValue(), 37);
		
		Assert.assertTrue(heap.add(13));
		Assert.assertEquals(heap.size(), 3);
		
		Assert.assertEquals(heap.peekMin().intValue(), 13);
		Assert.assertEquals(heap.extractMin().intValue(), 13);
		Assert.assertEquals(heap.size(), 2);
		
		Assert.assertEquals(heap.peekMin().intValue(), 37);
		heap.add(40);
		Assert.assertEquals(heap.size(), 3);
		Assert.assertEquals(heap.peekMin().intValue(), 37);
		Assert.assertEquals(heap.extractMin().intValue(), 37);
		Assert.assertEquals(heap.size(), 2);
		
		Assert.assertEquals(heap.peekMin().intValue(), 40);
		Assert.assertEquals(heap.extractMin().intValue(), 40);
		Assert.assertEquals(heap.size(), 1);
		
		Assert.assertEquals(heap.peekMin().intValue(), 42);
		Assert.assertEquals(heap.extractMin().intValue(), 42);
		
		Assert.assertEquals(heap.size(), 0);
		Assert.assertTrue(heap.isEmpty());
	}
	
	
}
