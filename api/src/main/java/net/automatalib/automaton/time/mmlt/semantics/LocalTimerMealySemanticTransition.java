package net.automatalib.automaton.time.mmlt.semantics;

import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;

/**
 * Represents a transition in the semantics automaton ("expanded form") of an MMLT.
 *
 * @param output Transition output
 * @param target Transition target
 * @param <S>    Location type
 * @param <I>    Input type for non-delaying inputs
 * @param <O>    Output symbol type
 */
public record LocalTimerMealySemanticTransition<S, I, O>(LocalTimerMealyOutputSymbol<O> output,
                                                         LocalTimerMealyConfiguration<S, I, O> target) {

}
