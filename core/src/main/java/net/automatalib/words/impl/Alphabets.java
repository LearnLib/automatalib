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
package net.automatalib.words.impl;

import java.util.List;

import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;

public abstract class Alphabets {

	public static <T> Alphabet<T> fromList(List<? extends T> list) {
		return new ListAlphabet<>(list);
	}
	
	@SafeVarargs
	public static <T> Alphabet<T> fromArray(T ...symbols) {
		return new ArrayAlphabet<>(symbols);
	}
	
	public static <E extends Enum<E>> Alphabet<E> fromEnum(Class<E> enumClazz, boolean withNull) {
		return new EnumAlphabet<>(enumClazz, withNull);
	}
	
	public static <E extends Enum<E>> Alphabet<E> fromEnum(Class<E> enumClazz) {
		return fromEnum(enumClazz, false);
	}
	
	public static Alphabet<Integer> integers(int startInclusive, int endInclusive) {
		List<Integer> lst = CollectionsUtil.intRange(startInclusive, endInclusive + 1);
		return fromList(lst);
	}
	
	public static Alphabet<Character> characters(char startInclusive, char endInclusive) {
		List<Character> lst = CollectionsUtil.charRange(startInclusive, (char)(endInclusive + 1));
		return fromList(lst);
	}
	
	
	
	private Alphabets() {
		// prevent inheritance
	}

}
