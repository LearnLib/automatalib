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
package net.automatalib.commons.util;

import java.io.IOException;
import java.io.Serializable;

import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.commons.util.strings.StringUtil;

/**
 * Mutable pair class.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <T1> type of the pair's first component.
 * @param <T2> type of the pair's second component.
 */
public class Pair<T1, T2> extends AbstractPrintable implements Serializable {
	private static final long serialVersionUID = -1L;
	
	/*
	 * Components
	 */
	protected T1 first;
	protected T2 second;
	
	/**
	 * Constructs a pair with the given members.
	 * 
	 * @param first first member.
	 * @param second second member.
	 */
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Getter for the first member.
	 * @return the first member.
	 */
	public T1 getFirst() { return first; }
	
	/**
	 * Getter for the second member.
	 * @return the second member.
	 */
	public T2 getSecond() { return second; }
	
	/**
	 * Setter for the first member.
	 * @param first the new value for the first member.
	 */
	public void setFirst(T1 first) { this.first = first; }
	
	/**
	 * Setter for the second member.
	 * @param second the new value for the second member.
	 */
	public void setSecond(T2 second) { this.second = second; }
	
	/**
	 * Convenience function for creating a pair, allowing the user to omit
	 * the type parameters.
	 * 
	 * @see #Pair(Object, Object)
	 */
	public static <T1,T2> Pair<T1,T2> make(T1 first, T2 second) {
		return new Pair<T1,T2>(first, second);
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?,?> other = (Pair<?,?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}


	/*
	 * (non-Javadoc)
	 * @see de.ls5.util.Printable#print(java.lang.Appendable)
	 */
	@Override
	public void print(Appendable a) throws IOException {
		StringUtil.appendObject(a, first);
		a.append(", ");
		StringUtil.appendObject(a, second);		
	}
}
