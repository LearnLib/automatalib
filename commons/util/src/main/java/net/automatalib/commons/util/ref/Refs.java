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
package net.automatalib.commons.util.ref;

/**
 * Utility functions for dealing with references.
 * 
 * @author Malte Isberner <malte.isberner@cs.uni-dortmund.de>
 *
 */
public abstract class Refs {

	/**
	 * Creates a strong reference to the given referent.
	 * @param referent the referent.
	 * @return a strong reference to the referent.
	 */
	public static <T> StrongRef<T> strong(T referent) {
		return new StrongRef<T>(referent);
	}
	
	/**
	 * Creates a weak reference to the given referent.
	 * @param referent the referent.
	 * @return a weak reference to the referent.
	 */
	public static <T> WeakRef<T> weak(T referent) {
		return new WeakRef<T>(referent);
	}
	

	private Refs() {}
}

