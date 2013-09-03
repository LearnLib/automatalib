package net.automatalib.automata.graphs;

import java.util.Objects;

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

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Objects.hashCode(input);
			result = prime * result + Objects.hashCode(property);
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != Property.class)
				return false;
			Property<?,?> other = (Property<?,?>) obj;
			if(!Objects.equals(input, other.input))
				return false;
			return Objects.equals(property, other.property);
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


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(input);
		result = prime * result + Objects.hashCode(transition);
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != TransitionEdge.class)
			return false;
		TransitionEdge<?,?> other = (TransitionEdge<?,?>) obj;
		if(!Objects.equals(input, other.input))
			return false;
		return Objects.equals(transition, other.transition);
	}
	
	

}
