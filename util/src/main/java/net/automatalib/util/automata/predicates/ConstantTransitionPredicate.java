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
package net.automatalib.util.automata.predicates;

import net.automatalib.ts.TransitionPredicate;

final class ConstantTransitionPredicate implements TransitionPredicate<Object,Object,Object> {
	
	public static ConstantTransitionPredicate TRUE = new ConstantTransitionPredicate(true);
	public static ConstantTransitionPredicate FALSE = new ConstantTransitionPredicate(false);
	
	public static ConstantTransitionPredicate forValue(boolean value) {
		return value ? TRUE : FALSE;
	}

	private final boolean value;
	
	private ConstantTransitionPredicate(boolean value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.automata.filters.TransitionFilter#apply(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean apply(Object source, Object input,
			Object transition) {
		return value;
	}

	
}
