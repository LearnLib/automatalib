/* Copyright (C) 2013-2014 TU Dortmund
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
package net.automatalib.graphs.concepts;

import javax.annotation.Nullable;

/**
 * Initial node concept. Graphs implementing this interface expose a designated
 * initial node.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class.
 */
public interface InitialNode<N> {
	/**
	 * Retrieves the initial node, or {@code null} if this graph does not have an initial
	 * node.
	 * @return the initial node.
	 */
	@Nullable
	public N getInitialNode();
}
