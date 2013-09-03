package net.automatalib.automata.transout.probabilistic;

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

}
