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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

@Test
public class IterablesTest {

	@Test
	public static void testConcat() {
		List<String> l1 = Arrays.asList("foo", "bar");
		List<String> l2 = Arrays.asList("baz", "qux");
		
		System.err.println("=================");
		for(String s : IterableUtil.concat(l1, l2))
			System.err.println(s);
		
		System.err.println("=================");
		for(String s : IterableUtil.concat(Collections.<String>emptyList(), l2))
			System.err.println(s);
		
		System.err.println("=================");
		for(String s : IterableUtil.concat(l1, Collections.<String>emptyList()))
			System.err.println(s);
	}

}
