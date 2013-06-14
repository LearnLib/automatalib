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

import java.util.Collection;

import net.automatalib.ts.TransitionSystem;

public interface TSTraversalMethod {
	
	public static TSTraversalMethod BREADTH_FIRST = new TSTraversalMethod() {
		@Override
		public <S, I, T, D> void traverse(TransitionSystem<S, ? super I, T> ts,
				int limit, Collection<? extends I> inputs,
				TSTraversalVisitor<S, I, T, D> visitor) {
			TSTraversal.breadthFirst(ts, limit, inputs, visitor);
		}
	};
	
	public static TSTraversalMethod DEPTH_FIRST = new TSTraversalMethod() {
		@Override
		public <S, I, T, D> void traverse(TransitionSystem<S, ? super I, T> ts,
				int limit, Collection<? extends I> inputs,
				TSTraversalVisitor<S, I, T, D> visitor) {
			TSTraversal.depthFirst(ts, limit, inputs, visitor);
		}
	};
	
	public <S,I,T,D>
	void traverse(TransitionSystem<S,? super I,T> ts, int limit, Collection<? extends I> inputs, TSTraversalVisitor<S, I, T, D> visitor);

}
