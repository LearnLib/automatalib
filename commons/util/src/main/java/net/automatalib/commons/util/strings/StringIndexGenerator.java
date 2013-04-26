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
package net.automatalib.commons.util.strings;

import java.io.IOException;


/**
 * Class for transforming integer index values into string values (using
 * latin characters, therefore effectively realizing a radix-26 representation
 * of numbers).
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public class StringIndexGenerator {
	public static enum Case {
		LOWER,
		UPPER
	}
	
	private final char base;
	
	private char getChar(int idx) {
		return (char)(base + idx);
	}
	
	private static int getInteger(char c) {
		c = Character.toLowerCase(c);
		return c - 'a';
	}
	
	public StringIndexGenerator(Case charCase) {
		switch(charCase) {
		case LOWER:
			this.base = 'a';
			break;
		default: // case UPPER:
			this.base = 'A';
		}
	}
	
	public void appendStringIndex(Appendable a, int idx) throws IOException {
		do {
			a.append(getChar(idx % 26));
			idx /= 26;
		} while(idx > 0);
	}
	
	public void appendStringIndex(StringBuilder sb, int idx) {
		do {
			sb.append(getChar(idx % 26));
			idx /= 26;
		} while(idx > 0);
	}
	
	public String getStringIndex(int idx) {
		StringBuilder sb = new StringBuilder();
		appendStringIndex(sb, idx);
		
		return sb.toString();
	}
	
	public static int getIntegerIndex(String sidx) {
		int idx = 0;
		int value = 1;
		for(int i = 0; i < sidx.length(); i++) {
			char c = sidx.charAt(i);
			idx = idx + value*getInteger(c);
			value *= 26;
		}
		
		return idx;
	}

}
