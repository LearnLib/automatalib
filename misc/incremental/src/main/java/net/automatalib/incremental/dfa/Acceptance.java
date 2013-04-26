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
package net.automatalib.incremental.dfa;

/**
 * Tri-state acceptance value.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public enum Acceptance {
	FALSE,
	TRUE,
	DONT_KNOW;
	
	/**
	 * Retrieves the corresponding acceptance value (either {@link #TRUE} or {@link #FALSE})
	 * for a given boolean value.
	 * @param val the boolean value
	 * @return the corresponding acceptance value
	 */
	public static Acceptance fromBoolean(boolean val) {
		return val ? TRUE : FALSE;
	}
	
	
	public boolean conflicts(boolean val) {
		if(this == DONT_KNOW)
			return false;
		return ((this == TRUE) != val);
	}
}
