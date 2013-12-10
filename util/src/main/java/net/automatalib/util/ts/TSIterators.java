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

import net.automatalib.commons.util.collections.TwoLevelIterator;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.TS.TransRef;

import com.google.common.collect.AbstractIterator;

abstract class TSIterators {
	
	
	public static final class DefinedInputsIterator<S,I> extends AbstractIterator<I> {
		private final TransitionSystem<S, I, ?> ts;
		private final Iterator<? extends I> inputsIt;
		private final S state;
		public DefinedInputsIterator(
				TransitionSystem<S, I, ?> ts,
				S state,
				Iterator<? extends I> inputsIt) {
			this.ts = ts;
			this.inputsIt = inputsIt;
			this.state = state;
		}
		@Override
		protected I computeNext() {
			while(inputsIt.hasNext()) {
				I input = inputsIt.next();
				Collection<?> transitions = ts.getTransitions(state, input);
				if(transitions != null && !transitions.isEmpty()) {
					return input;
				}
			}
			return endOfData();
		}
	}
	
	public static final class AllDefinedInputsIterator<S,I>
			extends TwoLevelIterator<S,I,TransRef<S, I, ?>> {
		private final TransitionSystem<S, I, ?> ts;
		private final Iterable<? extends I> inputs;
		public AllDefinedInputsIterator(
				Iterator<? extends S> stateIt,
				TransitionSystem<S, I, ?> ts,
				Iterable<? extends I> inputs) {
			super(stateIt);
			this.ts = ts;
			this.inputs = inputs;
		}
		@Override
		protected Iterator<I> l2Iterator(S state) {
			return TS.definedInputsIterator(ts, state, inputs.iterator());
		}
		@Override
		protected TransRef<S, I, ?> combine(S state, I input) {
			return new TransRef<>(state, input);
		}
	}

	public static final class UndefinedInputsIterator<S,I> extends AbstractIterator<I> {
		private final TransitionSystem<S, I, ?> ts;
		private final Iterator<? extends I> inputsIt;
		private final S state;
		public UndefinedInputsIterator(TransitionSystem<S, I, ?> ts, S state, Iterator<? extends I> inputsIt) {
			this.ts = ts;
			this.inputsIt = inputsIt;
			this.state = state;
		}
		@Override
		protected I computeNext() {
			while(inputsIt.hasNext()) {
				I input = inputsIt.next();
				Collection<?> transitions = ts.getTransitions(state, input);
				if(transitions == null || !transitions.isEmpty()) {
					return input;
				}
			}
			return endOfData();
		}
	}
	
	
	public static final class AllUndefinedInputsIterator<S,I>
			extends TwoLevelIterator<S, I, TransRef<S,I,?>> {
		private final TransitionSystem<S, I, ?> ts;
		private final Iterable<? extends I> inputs;
		public AllUndefinedInputsIterator(
				Iterator<? extends S> stateIt,
				TransitionSystem<S, I, ?> ts,
				Iterable<? extends I> inputs) {
			super(stateIt);
			this.ts = ts;
			this.inputs = inputs;
		}
		@Override
		protected Iterator<I> l2Iterator(S state) {
			return TS.undefinedInputsIterator(ts, state, inputs.iterator());
		}
		@Override
		protected TransRef<S,I,?> combine(S l1Object, I l2Object) {
			return new TransRef<>(l1Object, l2Object);
		}
	}


}
