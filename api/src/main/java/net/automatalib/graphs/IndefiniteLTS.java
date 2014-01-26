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
package net.automatalib.graphs;

import net.automatalib.graphs.concepts.EdgeLabels;

/**
 * A labeled transition system is a graph with labels attached to its edges.
 * 
 * @author Malte Isberner
 *
 * @param <N> node class
 * @param <E> edge class
 * @param <L> label class
 */
public interface IndefiniteLTS<N, E, L> extends IndefiniteGraph<N, E>, EdgeLabels<E, L> {

}
