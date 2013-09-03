package net.automatalib.automata.transout.probabilistic;

import java.util.Objects;

public final class ProbabilisticOutput<O> {
	
	private final float probability;
	private final O output;

	public ProbabilisticOutput(float probability, O output) {
		this.probability = probability;
		this.output = output;
	}
	
	public float getProbability() {
		return probability;
	}
	
	public O getOutput() {
		return output;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(output);
		result = prime * result + Float.floatToIntBits(probability);
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
		if (obj.getClass() != ProbabilisticOutput.class)
			return false;
		ProbabilisticOutput<?> other = (ProbabilisticOutput<?>)obj;
		if(!Objects.equals(output, other.output))
			return false;
		return (probability == other.probability);
	}
	
	

}
