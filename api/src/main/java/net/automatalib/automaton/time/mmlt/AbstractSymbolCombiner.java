package net.automatalib.automaton.time.mmlt;

import java.util.List;

/**
 * In an MMLT, multiple timeouts may occur simultaneously.
 * We use these symbol combiners to combine their outputs deterministically.
 *
 * @param <U> Symbol type
 */
public abstract class AbstractSymbolCombiner<U> {

    /**
     * Indicates if the provided suffix is a combined suffix.
     *
     * @param symbol Symbol for testing
     * @return True if combined suffix, false if not.
     */
    public abstract boolean isCombinedSymbol(U symbol);

    /**
     * Combines the provided symbols to a single suffix of same data type. Must be deterministic.
     *
     * @param symbols Provided symbols.
     * @return Combined suffix
     */
    public abstract U combineSymbols(List<U> symbols);

    /**
     * Attempts to separate the provided combined suffix into individual symbols.
     *
     * @param symbol Combined symbols
     * @return Individual symbols
     */
    public abstract List<U> separateSymbols(U symbol);
}
