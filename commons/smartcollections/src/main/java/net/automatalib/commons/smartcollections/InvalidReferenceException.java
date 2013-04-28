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
package net.automatalib.commons.smartcollections;

/**
 * Exception that is thrown if an invalid {@link ElementReference} is used.
 * This can be the case if it refers to a previously removed element, or to
 * an element stored in a different collection.
 * 
 * This exception does not need to be caught explicitly.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 */
public class InvalidReferenceException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1L;


	/**
	 * Default constructor.
	 */
	public InvalidReferenceException() {
		super();
	}
	
	/**
	 * Constructor.
	 * @see RuntimeException#RuntimeException(String)
	 */
	public InvalidReferenceException(String message) {
		super(message);
	}
}
