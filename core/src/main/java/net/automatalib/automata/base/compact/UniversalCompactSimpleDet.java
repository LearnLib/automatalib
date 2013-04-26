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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package net.automatalib.automata.base.compact;

import net.automatalib.words.Alphabet;

public class UniversalCompactSimpleDet<I, SP> extends
		AbstractCompactSimpleDet<I, SP> {
	
	private Object[] stateProperties;
	
	

	public UniversalCompactSimpleDet(Alphabet<I> alphabet, float resizeFactor) {
		super(alphabet, resizeFactor);
	}

	public UniversalCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity,
			float resizeFactor) {
		super(alphabet, stateCapacity, resizeFactor);
	}

	public UniversalCompactSimpleDet(Alphabet<I> alphabet, int stateCapacity) {
		super(alphabet, stateCapacity);
	}

	public UniversalCompactSimpleDet(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	@SuppressWarnings("unchecked")
	public SP getStateProperty(int stateId) {
		return (SP)stateProperties[stateId];
	}

	@Override
	public void initState(int stateId, SP property) {
		stateProperties[stateId] = property;
	}

	@Override
	public void setStateProperty(int stateId, SP property) {
		stateProperties[stateId] = property;
	}

	@Override
	protected void ensureCapacity(int oldCap, int newCap) {
		super.ensureCapacity(oldCap, newCap);
		Object[] newProps = new Object[newCap];
		System.arraycopy(stateProperties, 0, newProps, 0, stateProperties.length);
		stateProperties = newProps;
	}

	@Override
	public void clear() {
		super.clear();
		for(int i = 0; i < stateProperties.length; i++)
			stateProperties[i] = null;
	}
	
	

}
