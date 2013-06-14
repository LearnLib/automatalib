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
package net.automatalib.graphs.concepts;

import java.util.Set;

/**
 * A Kripke interpretation for a graph. A Kripke interpretation assigns
 * to each node a set of so-called <i>atomic propositions</i>.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <N> node class
 * @param <AP> atomic proposition class
 */
public interface KripkeInterpretation<N, AP> {
	/**
	 * Retrieves the atomic propositions holding at the given node.
	 * 
	 * @param node the node
	 * @return the set of atomic propositions that hold at the given node
	 */
	public Set<AP> getAtomicPropositions(N node);
}
