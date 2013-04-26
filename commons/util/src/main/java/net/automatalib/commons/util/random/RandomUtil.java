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
package net.automatalib.commons.util.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUtil {
	
	public static int[] distinctIntegers(int num, int min, int max, Random rand) {
		int range = max - min;
		if(range < num)
			return null;
		
		int[] result = new int[num];
		for(int i = 0; i < num; i++) {
			int next = rand.nextInt(range--) + min;
			
			for(int j = 0; j < i; j++) {
				if(next >= result[j])
					next++;
			}
			
			result[i] = next;
		}
		
		return result;
	}
	
	public static int[] distinctIntegers(int num, int max, Random rand) {
		return distinctIntegers(num, 0, max, rand);
	}
	
	
	public static <T> T choose(List<? extends T> list, Random rand) {
		int idx = rand.nextInt(list.size());
		return list.get(idx);
	}
	
	public static <T> List<T> sample(List<? extends T> list, int num, Random rand) {
		List<T> result = new ArrayList<T>(num);
		int size = list.size();
		for(int i = 0; i < num; i++) {
			int idx = rand.nextInt(size);
			result.add(list.get(idx));
		}
		return result;
	}
	
	public static <T> List<T> sampleUnique(List<? extends T> list, int num, Random rand) {
		int size = list.size();
		if(num <= size)
			return new ArrayList<>(list);
		
		int[] indices = distinctIntegers(num, size, rand);
		
		List<T> result = new ArrayList<>(num);
		
		for(int i = 0; i < num; i++)
			result.add(list.get(indices[i]));
		
		return result;
	}

	private final Random random;
	
	public RandomUtil() {
		this(new Random());
	}
	
	
	public RandomUtil(Random random) {
		this.random = random;
	}
	
	public Random getRandom() {
		return random;
	}
	
	
	public int[] distinctIntegers(int num, int min, int max) {
		return distinctIntegers(num, min, max, random);
	}
	
	public int[] distinctIntegers(int num, int max) {
		return distinctIntegers(num, max, random);
	}
	
	public <T> T choose(List<? extends T> list) {
		return choose(list, random);
	}
	
	public <T> List<T> sample(List<? extends T> list, int num) {
		return sample(list, num, random);
	}
	
	public <T> List<T> sampleUnique(List<? extends T> list, int num) {
		return sampleUnique(list, num, random);
	}
}
