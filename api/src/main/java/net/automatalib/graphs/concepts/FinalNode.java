package net.automatalib.graphs.concepts;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface FinalNode<N> {

    /**
     * Retrieves the final node, or {@code null} if this graph does not have a final node.
     *
     * @return the final node.
     */
    @Nullable N getFinalNode();
}
