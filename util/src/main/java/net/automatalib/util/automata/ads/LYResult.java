package net.automatalib.util.automata.ads;

import net.automatalib.automata.ads.ADSNode;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class that holds some information aggregated during the ADS computation of {@link LeeYannakakis}.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 *
 * @author frohme
 */
public class LYResult<S, I, O> {

	private final Optional<ADSNode<S, I, O>> delegate;

	private final Set<S> indistinguishableStates;

	public LYResult(final ADSNode<S, I, O> result) {
		this.delegate = Optional.of(result);
		this.indistinguishableStates = Collections.emptySet();
	}

	public LYResult(final Set<S> indistinguishableStates) {
		this.delegate = Optional.empty();
		this.indistinguishableStates = indistinguishableStates;
	}

	public LYResult() {
		this.delegate = Optional.empty();
		this.indistinguishableStates = Collections.emptySet();
	}

	public boolean isPresent() {
		return this.delegate.isPresent();
	}

	public ADSNode<S, I, O> get() {
		return this.delegate.get();
	}

	public Set<S> getIndistinguishableStates() {
		return this.indistinguishableStates;
	}
}
