package net.automatalib.util.automata.ads;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class used by the algorithm of {@link LeeYannakakis}.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 *
 * @author frohme
 */
class SplitTreeResult<S, I, O> {

	private final Optional<SplitTree<S, I, O>> delegate;
	private final Set<S> indistinguishableStates;

	public SplitTreeResult(final SplitTree<S, I, O> result) {
		this.delegate = Optional.of(result);
		this.indistinguishableStates = Collections.emptySet();
	}

	public SplitTreeResult(final Set<S> indistinguishableStates) {
		this.delegate = Optional.empty();
		this.indistinguishableStates = indistinguishableStates;
	}

	public SplitTreeResult() {
		this.delegate = Optional.empty();
		this.indistinguishableStates = Collections.emptySet();
	}

	public boolean isPresent() {
		return this.delegate.isPresent();
	}

	public SplitTree<S, I, O> get() {
		return this.delegate.get();
	}

	public Set<S> getIndistinguishableStates() {
		return this.indistinguishableStates;
	}

}
