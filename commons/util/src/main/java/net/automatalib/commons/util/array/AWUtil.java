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
package net.automatalib.commons.util.array;

import java.util.Arrays;

/**
 * Utility class for writing containers to arrays.
 * 
 * It is generally preferable to use the static methods this class offers than
 * using {@link ArrayWritable#writeToArray(int, Object[], int, int)} directly.
 * 
 * @author Malte Isberner
 *
 */
public abstract class AWUtil {
	
	/*
	 * Prevent inheritance.
	 */
	private AWUtil() {
	}
	
	
	/**
	 * Writes the complete container data to an array. This method ensures that the array's capacity
	 * is not exceeded.
	 * @param aw the container.
	 * @param array the array
	 * @return the number of elements copied
	 */
	public static <T,U extends T> int safeWrite(ArrayWritable<U> aw, T[] array) {
		int num = aw.size();
		if(num <= 0)
			return 0;
		if(num > array.length)
			num = array.length;
		aw.writeToArray(0, array, 0, num);
		return num;
	}
	
	/**
	 * Writes a given maximum amount of data items from a container to an array. This
	 * method ensures that the array's capacity is not exceeded.
	 * @param num the number of elements to copy
	 * @param aw the container.
	 * @param array the array
	 * @return the number of elements copied
	 */
	public static <T,U extends T> int safeWrite(int num, ArrayWritable<U> aw, T[] array) {
		int tmp = aw.size();
		if(tmp < num)
			num = tmp;
		tmp = array.length;
		if(tmp < num)
			num = tmp;
		if(num <= 0)
			return 0;
		aw.writeToArray(0, array, 0, num);
		return num;
	}
	
	public static <T, U extends T> int safeWrite(int num, ArrayWritable<U> aw, int ofs, T[] array, int tgtOfs) {
		int tmp = aw.size() - ofs;
		if(tmp < num)
			num = tmp;
		tmp = array.length - tgtOfs;
		if(tmp < num)
			num = tmp;
		if(num <= 0)
			return 0;
		aw.writeToArray(ofs, array, tgtOfs, num);
		return num;
	}
	
	public static <T, U extends T> int safeWrite(ArrayWritable<U> aw, T[] array, int tgtOfs) {
		int num = array.length - tgtOfs;
		int s = aw.size();
		if(s < num)
			num = s;
		if(num <= 0)
			return 0;
		aw.writeToArray(0, array, tgtOfs, num);
		return num;
	}
	
	
	public static Object[] toArray(ArrayWritable<?> aw) {
		int num = aw.size();
		Object[] arr = new Object[num];
		aw.writeToArray(0, arr, 0, num);
		return arr;
	}
	
	public static <T> T[] toArray(ArrayWritable<?> aw, T[] arr) {
		int num = aw.size();
		if(arr.length < num)
			arr = Arrays.copyOf(arr, num);
		aw.writeToArray(0, arr, 0, num);
		return arr;
	}
}
