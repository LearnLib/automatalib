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

import java.util.Arrays;


public class EnumAlphabet<E extends Enum<E>> extends ArrayAlphabet<E> {
	
	private static <E> E[] extractEnumValues(Class<E> enumClazz, boolean withNull) {
		E[] enumValues = enumClazz.getEnumConstants();
		if(enumValues == null)
			throw new IllegalArgumentException("Class " + enumClazz.getName() + " is not an enumeration class!");
		if(!withNull)
			return enumValues;
		return Arrays.copyOf(enumValues, enumValues.length + 1);
	}

	public EnumAlphabet(Class<E> enumClazz, boolean withNull) {
		super(extractEnumValues(enumClazz, withNull));
	}

	@Override
	public int getSymbolIndex(E symbol) throws IllegalArgumentException {
		if(symbol == null) {
			int lastIdx = symbols.length - 1;
			if(symbols[lastIdx] == null)
				return lastIdx;
			throw new IllegalArgumentException("No such symbol: null");
		}
		return symbol.ordinal();
	}

	/* (non-Javadoc)
	 * @see net.automatalib.words.abstractimpl.AbstractAlphabet#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(E o1, E o2) {
		if(o1 == o2)
			return 0;
		if(o1 == null)
			return 1;
		if(o2 == null)
			return -1;
		return o1.compareTo(o2);
	}

}
