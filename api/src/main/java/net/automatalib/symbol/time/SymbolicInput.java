package net.automatalib.symbol.time;

import net.automatalib.automaton.mmlt.MMLT;

/**
 * Markup-interface for structural inputs currently used in {@link MMLT}s.
 *
 * @param <I>
 *         input symbol type
 */
public sealed interface SymbolicInput<I> permits InputSymbol, TimerTimeoutSymbol {}
