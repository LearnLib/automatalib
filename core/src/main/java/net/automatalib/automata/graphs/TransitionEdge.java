package net.automatalib.automata.graphs;

import net.automatalib.ts.UniversalTransitionSystem;

public final class TransitionEdge<I, T> {
	
	public static final class Property<I,TP> {
		private final I input;
		private final TP property;
		
		
		public Property(I input, TP property) {
			this.input = input;
			this.property = property;
		}
		
		public I getInput() {
			return input;
		}
		
		public TP getProperty() {
			return property;
		}
	}
	
	private final I input;
	private final T transition;

	public TransitionEdge(I input, T transition) {
		this.input = input;
		this.transition = transition;
	}

	
	public I getInput() {
		return input;
	}
	
	public T getTransition() {
		return transition;
	}
	
	
	public <TP> Property<I,TP> property(UniversalTransitionSystem<?, ?, T, ?, TP> uts) {
		return new Property<>(input, uts.getTransitionProperty(transition));
	}

}
