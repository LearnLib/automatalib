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
package net.automatalib.commons.smartcollections;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DefaultLinkedListTest {
	private DefaultLinkedList<Object> linkedList;
	private Object first = new Object(), second = new Object(), third = new Object();
	private ElementReference firstRef, secondRef, thirdRef;
	
	@BeforeClass
	public void setup() {
		linkedList = new DefaultLinkedList<>();
		firstRef = linkedList.referencedAdd(first);
		secondRef = linkedList.referencedAdd(second);
		thirdRef = linkedList.referencedAdd(third);
	}
	
	
	@Test
	public void testInsertRemove() {
		Assert.assertEquals(linkedList.size(), 3);
		Assert.assertEquals(linkedList.get(firstRef), first);
		Assert.assertEquals(linkedList.get(secondRef), second);
		Assert.assertEquals(linkedList.get(thirdRef), third);
		
		Assert.assertEquals(linkedList.getFront(), first);
		Assert.assertEquals(linkedList.getBack(), third);
		
		Object fourth = new Object();
		ElementReference fourthRef = linkedList.pushFront(fourth);
		Assert.assertEquals(linkedList.size(), 4);
		Assert.assertEquals(linkedList.get(fourthRef), fourth);
		Assert.assertEquals(linkedList.getFront(), fourth);
		Assert.assertEquals(linkedList.get(linkedList.succ(fourthRef)), first);
	}
}
