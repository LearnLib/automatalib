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
package net.automatalib.util.ts;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.util.ts.TSIterators.DefinedInputsIterator;
import net.automatalib.util.ts.TSIterators.UndefinedInputsIterator;
import net.automatalib.util.ts.traversal.BFSOrderIterator;

import com.google.common.base.Function;


public abstract class TS {
	
	public static final class TransRef<S,I,T> {
		public final S state;
		public final I input;
		public final T transition;
		public TransRef(S state, I input, T transition) {
			this.state = state;
			this.input = input;
			this.transition = transition;
		}
		public TransRef(S state, I input) {
			this(state, input, null);
		}
	}
	
	public static <S,I> Iterable<S> bfsOrder(final TransitionSystem<S,I,?> ts, final Collection<? extends I> inputs) {
		return new Iterable<S>() {
			@Override
			public Iterator<S> iterator() {
				return new BFSOrderIterator<S, I>(ts, inputs);
			}
			
		};
	}
	
	public static <S,SP> Function<S,SP> stateProperties(final UniversalTransitionSystem<S, ?, ?, SP, ?> uts) {
		return new Function<S,SP>() {
			@Override
			public SP apply(S elem) {
				return uts.getStateProperty(elem);
			}
		};
	}
	
	public static <T,TP> Function<T,TP> transitionProperties(final UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
		return new Function<T,TP>() {
			@Override
			public TP apply(T elem) {
				return uts.getTransitionProperty(elem);
			}
		};
	}
	
	public static <S,I> Iterator<I> definedInputsIterator(TransitionSystem<S, I, ?> ts,
			S state,
			Iterator<? extends I> inputsIt) {
		return new DefinedInputsIterator<S, I>(ts, state, inputsIt);
	}
	
	public static <S,I> Iterable<I> definedInputs(
			final DeterministicTransitionSystem<S, I, ?> dts,
			final S state,
			final Iterable<? extends I> inputs) {
		return new Iterable<I>() {
			@Override
			public Iterator<I> iterator() {
				return definedInputsIterator(dts, state, inputs.iterator());
			}
		};
	}
	
	public static <S,I> Iterator<TransRef<S,I,?>> allDefinedInputsIterator(
			TransitionSystem<S, I, ?> ts,
			Iterator<? extends S> stateIt,
			Iterable<? extends I> inputs) {
		return new TSIterators.AllDefinedInputsIterator<>(stateIt, ts, inputs);
	}
	
	public static <S,I> Iterable<TransRef<S,I,?>> allDefinedInputs(
			final TransitionSystem<S, I, ?> ts,
			final Iterable<? extends S> states,
			final Iterable<? extends I> inputs) {
		return new Iterable<TransRef<S,I,?>>() {
			@Override
			public Iterator<TransRef<S,I,?>> iterator() {
				return allDefinedInputsIterator(ts, states.iterator(), inputs);
			}
		};
	}
	
	
	
	public static <S,I> Iterator<I> undefinedInputsIterator(TransitionSystem<S, I, ?> ts,
			S state,
			Iterator<? extends I> inputsIt) {
		return new UndefinedInputsIterator<S, I>(ts, state, inputsIt);
	}
	
	public static <S,I> Iterable<I> undefinedInputs(
			final TransitionSystem<S, I, ?> ts,
			final S state,
			final Iterable<? extends I> inputs) {
		return new Iterable<I>() {
			@Override
			public Iterator<I> iterator() {
				return undefinedInputsIterator(ts, state, inputs.iterator());
			}
		};
	}
	
	public static <S,I> Iterator<TransRef<S,I,?>> allUndefinedTransitionsIterator(
			TransitionSystem<S, I, ?> ts,
			Iterator<? extends S> stateIt,
			Iterable<? extends I> inputs) {
		return new TSIterators.AllUndefinedInputsIterator<>(stateIt, ts, inputs);
	}
	
	public static <S,I> Iterable<TransRef<S,I,?>> allUndefinedTransitions(
			final TransitionSystem<S, I, ?> ts,
			final Iterable<? extends S> states,
			final Iterable<? extends I> inputs) {
		return new Iterable<TransRef<S,I,?>>() {
			@Override
			public Iterator<TransRef<S, I, ?>> iterator() {
				return allUndefinedTransitionsIterator(ts, states.iterator(), inputs);
			}
		};
	}
	
}
