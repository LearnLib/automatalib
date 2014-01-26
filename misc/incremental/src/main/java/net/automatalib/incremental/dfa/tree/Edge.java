/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.incremental.dfa.tree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.incremental.dfa.Acceptance;

@ParametersAreNonnullByDefault
public final class Edge<I> {
	private final Node<I> node;
	private final I input;
	public Edge(Node<I> node, @Nullable I input) {
		this.node = node;
		this.input = input;
	}
	
	@Nonnull
	public Node<I> getNode() {
		return node;
	}
	
	@Nullable
	public I getInput() {
		return input;
	}
	
	@Nonnull
	public Acceptance getAcceptance() {
		return node.getAcceptance();
	}
}
