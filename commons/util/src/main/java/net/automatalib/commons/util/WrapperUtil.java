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
package net.automatalib.commons.util;

public abstract class WrapperUtil {
	
	public static boolean booleanValue(Boolean b, boolean def) {
		return (b != null) ? b.booleanValue() : def;
	}
	
	public static boolean booleanValue(Boolean b) {
		return booleanValue(b, false);
	}
	
	public static short shortValue(Short s, short def) {
		return (s != null) ? s.shortValue() : def;
	}
	
	public static int intValue(Integer i, int def) {
		return (i != null) ? i.intValue() : def;
	}
	
	public static int intValue(Integer i) {
		return intValue(i, 0);
	}
	
	public static long longValue(Long l, long def) {
		return (l != null) ? l.longValue() : def;
	}
	
	public static long longValue(Long l) {
		return longValue(l, 0L);
	}
	
	public static float floatValue(Float f, float def) {
		return (f != null) ? f.floatValue() : def;
	}
	
	public static float floatValue(Float f) {
		return floatValue(f, 0.0f);
	}
	
	public static double doubleValue(Double d, double def) {
		return (d != null) ? d.doubleValue() : def;
	}
	
	public static double doubleValue(Double d) {
		return doubleValue(d, 0.0);
	}

}
