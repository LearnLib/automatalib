package net.automatalib.util.automata.ads;

import net.automatalib.words.Word;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class originally used by the algorithm of {@link LeeYannakakis} but utilized by other ADS computations as
 * well.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 *
 * @author frohme
 */
class SplitTree<S, I, O> {

	private final Map<O, SplitTree<S, I, O>> successors;
	private final Map<S, S> mapping;
	private final Set<S> partition;

	private Word<I> sequence;

	public SplitTree(final Set<S> partition) {
		this.partition = partition;

		this.successors = new HashMap<>();
		this.mapping = new HashMap<>();
		this.sequence = Word.epsilon();
	}

	public Map<O, SplitTree<S, I, O>> getSuccessors() {
		return successors;
	}

	public Map<S, S> getMapping() {
		return mapping;
	}

	public Set<S> getPartition() {
		return partition;
	}

	public Word<I> getSequence() {
		return sequence;
	}

	public void setSequence(Word<I> sequence) {
		this.sequence = sequence;
	}

	public Optional<SplitTree<S, I, O>> findLowestSubsetNode(final Set<S> nodes) {

		for (final SplitTree<S, I, O> st : successors.values()) {
			final Optional<SplitTree<S, I, O>> candidate = st.findLowestSubsetNode(nodes);

			if (candidate.isPresent()) {
				return candidate;
			}
		}

		if (this.partition.containsAll(nodes)) {
			return Optional.of(this);
		}

		return Optional.empty();
	}

	@Override
	public String toString() {
		return String.format("[states: %s, seq=%s]", this.partition.toString(), this.sequence.toString());
	}
}
