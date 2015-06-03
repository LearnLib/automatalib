/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
