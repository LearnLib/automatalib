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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AllTuplesTest {
	
	private static final List<Integer> domain = Arrays.asList(1, 2, 3, 7);
	
	@Test
	public static void testEmptyDomain() {
		int count = count(CollectionsUtil.allTuples(Collections.emptySet(), 1), null);
		Assert.assertEquals(count, 0);
		
		count = count(CollectionsUtil.allTuples(Collections.emptySet(), 0), null);
		Assert.assertEquals(count, 1);
		
		count = count(CollectionsUtil.allTuples(Collections.emptySet(), 0, 5), null);
		Assert.assertEquals(count, 1);
	}
	
	@Test
	public static void testAllTuples() {
		int count = count(CollectionsUtil.allTuples(domain, 0), null);
		Assert.assertEquals(count, 1);
		
		Set<Object> set = new HashSet<Object>();
		count = count(CollectionsUtil.allTuples(domain, 1), set);
		Assert.assertEquals(count, domain.size());
		Assert.assertEquals(set.size(), count);
		
		count = count(CollectionsUtil.allTuples(domain, 0, 1), set);
		Assert.assertEquals(count, domain.size() + 1);
		Assert.assertEquals(set.size(), count);
		
		count = count(CollectionsUtil.allTuples(domain, 3), set);
		Assert.assertEquals(count, (int)Math.pow(domain.size(), 3));
		Assert.assertEquals(set.size(), count);
		
		for(List<?> lst : CollectionsUtil.allTuples(domain, 3))
			Assert.assertEquals(lst.size(), 3);
	}
	
	
	private static int count(Iterable<? extends List<?>> iterable, Set<Object> distinct) {
		if(distinct != null)
			distinct.clear();
		Iterator<? extends List<?>> it = iterable.iterator();
		int count = 0;
		while(it.hasNext()) {
			count++;
			List<?> l = it.next();
			if(distinct != null)
				distinct.add(new ArrayList<Object>(l));
		}
		return count;
	}
}
