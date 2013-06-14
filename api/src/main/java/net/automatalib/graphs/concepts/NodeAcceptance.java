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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.graphs.Graph;

/**
 * Node acceptance concept, for {@link Graph}s that represent a structure for
 * deciding acceptance or rejection.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 */
@ParametersAreNonnullByDefault
public interface NodeAcceptance<N> {
	
	/**
	 * Checks whether a node is an accepting node.
	 * @param node the node
	 * @return <tt>true</tt> if the given node is an accepting node, <tt>false</tt>
	 * otherwise.
	 */
	public boolean isAcceptingNode(N node);
}
