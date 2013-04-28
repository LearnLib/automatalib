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
/**
 * 
 */
package net.automatalib.util.minimizer;

/**
 * An edge in the internal automaton representation.
 * 
 * @author Malte Isberner
 *
 * @param <S> state class.
 * @param <T> transition label class.
 */
final class Edge<S,T> {
	// source state
	private final State<S,T> source;
	
	// target state
	private final State<S,T> target;
	
	// transition label
	private final TransitionLabel<S, T> transitionLabel;

	
	/**
	 * Constructor.
	 * @param source the source state.
	 * @param target the target state.
	 * @param transitionLabel the transition label.
	 */
	public Edge(State<S,T> source, State<S,T> target, TransitionLabel<S,T> transitionLabel) {
		this.source = source;
		this.target = target;
		this.transitionLabel = transitionLabel;
	}
	
	/**
	 * Retrieves the source state.
	 * @return the source state.
	 */
	public State<S, T> getSource() {
		return source;
	}
	
	/**
	 * Retrieves the transition label.
	 * @return the transition label.
	 */
	public TransitionLabel<S,T> getTransitionLabel() {
		return transitionLabel;
	}
	
	/**
	 * Retrieves the target state.
	 * @return the target state.
	 */
	public State<S, T> getTarget() {
		return target;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + source + ", " + transitionLabel + ", " + target + ")";
	}
	
}