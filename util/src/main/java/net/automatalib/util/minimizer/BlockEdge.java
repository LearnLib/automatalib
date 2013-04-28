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
package net.automatalib.util.minimizer;

/**
 * An edge in a {@link BlockAutomaton}.
 * 
 * @author Malte Isberner
 *
 * @param <S> state class.
 * @param <L> transition label class.
 */
public class BlockEdge<S, L> {
	private final Block<S,L> source;
	private final Block<S,L> target;
	private final L label;
	
	
	/**
	 * Constructor.
	 * 
	 * @param source source block.
	 * @param target target block.
	 * @param label the transition label.
	 */
	BlockEdge(Block<S,L> source, Block<S,L> target, L label) {
		this.source = source;
		this.target = target;
		this.label = label;
	}

	/**
	 * Retrieves the source block.
	 * @return the source block.
	 */
	public Block<S, L> getSource() {
		return source;
	}

	/**
	 * Retrieves the target block.
	 * @return the target block.
	 */
	public Block<S, L> getTarget() {
		return target;
	}

	/**
	 * Retrieves the transition label.
	 * @return the transition label.
	 */
	public L getLabel() {
		return label;
	}
}
