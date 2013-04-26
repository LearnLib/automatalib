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
package net.automatalib.incremental;

/**
 * Conflict exception. Thrown when a data point to be inserted conflicts with
 * definitive information already existing. 
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public class ConflictException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 * @see IllegalArgumentException#IllegalArgumentException()
	 */
	public ConflictException() {
		super();
	}

	/**
	 * Constructor.
	 * @see IllegalArgumentException#IllegalArgumentException(String, Throwable)
	 */
	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @see IllegalArgumentException#IllegalArgumentException(String)
	 */
	public ConflictException(String s) {
		super(s);
	}

	/**
	 * Constructor.
	 * @see IllegalArgumentException#IllegalArgumentException(Throwable)
	 */
	public ConflictException(Throwable cause) {
		super(cause);
	}
	
	

}
