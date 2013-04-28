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
package net.automatalib.commons.util.array;

/**
 * Utility class for writing containers to arrays.
 * 
 * It is generally preferable to use the static methods this class offers than
 * using {@link ArrayWritable#writeToArray(int, Object[], int, int)} directly.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
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
}
