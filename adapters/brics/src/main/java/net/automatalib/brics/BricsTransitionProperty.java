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
package net.automatalib.brics;

import dk.brics.automaton.Transition;

/**
 * The properties of an edge in a Brics automaton.
 * 
 * @author Malte Isberner 
 */
public class BricsTransitionProperty {
	
	public static String toString(char min, char max) {
		StringBuilder sb = new StringBuilder();
		sb.append('\'').append(min).append('\'');
		if(max > min)
			sb.append("..'").append(max).append('\'');
		return sb.toString();
	}
	
	private final char min;
	private final char max;

	/**
	 * Constructor.
	 * @param min lower bound of the character range.
	 * @param max upper bound of the character range.
	 */
	public BricsTransitionProperty(char min, char max) {
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Constructor. Constructs the property from a Brics {@link Transition}.
	 * @param trans the Brics transition object
	 */
	public BricsTransitionProperty(Transition trans) {
		this(trans.getMin(), trans.getMax());
	}
	
	/**
	 * Retrieves the lower bound of the character range.
	 * @return the lower bound of the character range
	 * @see Transition#getMin()
	 */
	public char getMin() {
		return min;
	}
	
	/**
	 * Retrieves the upper bound of the character range.
	 * @return the upper bound of the character range
	 * @see Transition#getMax()
	 */
	public char getMax() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max;
		result = prime * result + min;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BricsTransitionProperty other = (BricsTransitionProperty) obj;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(min, max);
	}
	
	

}
