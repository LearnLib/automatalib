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
package net.automatalib.util.ts;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.ts.traversal.BFSOrderIterator;


public abstract class TS {
	
	
	public static <S,I,T> Iterable<T> allTransitions(final TransitionSystem<S,I,T> ts,
			final S state,
			final Collection<I> inputs) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new AllTransitionsIterator<S,I,T>(ts, state, inputs);
			}
		};
	}
	
	public static <S,I> Iterable<S> bfsOrder(final TransitionSystem<S,I,?> ts, final Collection<? extends I> inputs) {
		return new Iterable<S>() {
			@Override
			public Iterator<S> iterator() {
				return new BFSOrderIterator<S, I>(ts, inputs);
			}
			
		};
	}
	
	public static <S,SP> Mapping<S,SP> stateProperties(final UniversalTransitionSystem<S, ?, ?, SP, ?> uts) {
		return new Mapping<S,SP>() {
			@Override
			public SP get(S elem) {
				return uts.getStateProperty(elem);
			}
		};
	}
	
	public static <T,TP> Mapping<T,TP> transitionProperties(final UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
		return new Mapping<T,TP>() {
			@Override
			public TP get(T elem) {
				return uts.getTransitionProperty(elem);
			}
		};
	}
	
}
