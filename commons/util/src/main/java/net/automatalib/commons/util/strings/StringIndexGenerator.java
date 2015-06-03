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
package net.automatalib.commons.util.strings;

import java.io.IOException;


/**
 * Class for transforming integer index values into string values (using
 * latin characters, therefore effectively realizing a radix-26 representation
 * of numbers).
 * 
 * @author Malte Isberner
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
