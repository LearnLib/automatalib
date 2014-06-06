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
package net.automatalib.util.traversal;

/**
 * Enum to use for coloring nodes/states during traversal.
 * <p>
 * Note that this enum only declares two values. The value {@link #WHITE} for unvisited nodes/states
 * is identified with <tt>null</tt>.
 *  
 * @author Malte Isberner 
 *
 */
public enum Color {
	/**
	 * Color for nodes/states that have been discovered, but not yet fully explored.
	 */
	GRAY,
	/**
	 * Color for nodes/states that have been fully explored.
	 */
	BLACK;
	
	/**
	 * Color for nodes/states that have not yet been discovered.
	 */
	public static final Color WHITE = null;
}
