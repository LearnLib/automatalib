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
package net.automatalib.util.ts.traversal;

public final class TraversalAction<D> {
	public static enum Type {
		EXPLORE,
		IGNORE,
		ABORT_INPUT,
		ABORT_STATE,
		ABORT_TRAVERSAL
	}
	
	public final Type type;
	public final D data;
	
	public TraversalAction(Type type) {
		this(type, null);
	}
	
	public TraversalAction(Type type, D data) {
		this.type = type;
		this.data = data;
	}
}
