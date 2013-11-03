package net.automatalib.util.ts;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.commons.util.collections.SimplifiedIterator;
import net.automatalib.commons.util.collections.TwoLevelIterator;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.util.ts.TS.TransRef;

abstract class TSIterators {
	static final class AllTransitionsIterator<S, I, T> implements Iterator<T> {
		private final TransitionSystem<S, I, T> ts;
		private final S state;
		private final Iterator<I> inputIt;
		private Iterator<T> transIt;
		
		public AllTransitionsIterator(TransitionSystem<S, I, T> ts, S state, Collection<I> inputs) {
			this.ts = ts;
			this.state = state;
			this.inputIt = inputs.iterator();
			findNext();
		}

		@Override
		public boolean hasNext() {
			return transIt.hasNext();
		}

		@Override
		public T next() {
			T t = transIt.next();
			if(!transIt.hasNext())
				findNext();
			return t;
		}

		@Override
		public void remove() {
			transIt.remove();
		}
		
		private void findNext() {
			while(inputIt.hasNext()) {
				I input = inputIt.next();
				Collection<T> trans = ts.getTransitions(state, input);
				if(trans == null || trans.isEmpty())
					continue;
				transIt = trans.iterator();
				break;
			}
		}
		
	}
	
	public static final class DefinedTransitionsIterator<S,I> extends SimplifiedIterator<I> {
		private final DeterministicTransitionSystem<S, I, ?> dts;
		private final Iterator<? extends I> inputsIt;
		private final S state;
		public DefinedTransitionsIterator(
				DeterministicTransitionSystem<S, I, ?> dts,
				S state,
				Iterator<? extends I> inputsIt) {
			this.dts = dts;
			this.inputsIt = inputsIt;
			this.state = state;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		@Override
		protected boolean calculateNext() {
			while(inputsIt.hasNext()) {
				I input = inputsIt.next();
				if(dts.getTransition(state, input) != null) {
					this.nextValue = input;
					return true;
				}
			}
			return false;
		}	
	}
	
	public static final class AllDefinedTransitionsIterator<S,I>
			extends TwoLevelIterator<S,I,TransRef<S, I>> {
		private final DeterministicTransitionSystem<S, I, ?> dts;
		private final Iterable<? extends I> inputs;
		public AllDefinedTransitionsIterator(
				Iterator<? extends S> stateIt,
				DeterministicTransitionSystem<S, I, ?> dts,
				Iterable<? extends I> inputs) {
			super(stateIt);
			this.dts = dts;
			this.inputs = inputs;
		}
		@Override
		protected Iterator<I> l2Iterator(S state) {
			return TS.definedTransitionsIterator(dts, state, inputs.iterator());
		}
		@Override
		protected TransRef<S, I> combine(S state, I input) {
			return new TransRef<>(state, input);
		}
	}

	public static final class UndefinedTransitionsIterator<S,I> extends SimplifiedIterator<I> {
		private final DeterministicTransitionSystem<S, I, ?> dts;
		private final Iterator<? extends I> inputsIt;
		private final S state;
		public UndefinedTransitionsIterator(DeterministicTransitionSystem<S, I, ?> dts, S state, Iterator<? extends I> inputsIt) {
			this.dts = dts;
			this.inputsIt = inputsIt;
			this.state = state;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		@Override
		protected boolean calculateNext() {
			while(inputsIt.hasNext()) {
				I input = inputsIt.next();
				if(dts.getTransition(state, input) == null) {
					this.nextValue = input;
					return true;
				}
			}
			return false;
		}
	}
	
	
	public static final class AllUndefinedTransitionsIterator<S,I>
			extends TwoLevelIterator<S, I, TransRef<S,I>> {
		private final DeterministicTransitionSystem<S, I, ?> dts;
		private final Iterable<? extends I> inputs;
		public AllUndefinedTransitionsIterator(
				Iterator<? extends S> stateIt,
				DeterministicTransitionSystem<S, I, ?> dts,
				Iterable<? extends I> inputs) {
			super(stateIt);
			this.dts = dts;
			this.inputs = inputs;
		}
		@Override
		protected Iterator<I> l2Iterator(S state) {
			return TS.undefinedTransitionsIterator(dts, state, inputs.iterator());
		}
		@Override
		protected TransRef<S, I> combine(S l1Object, I l2Object) {
			return new TransRef<>(l1Object, l2Object);
		}
	}


}
