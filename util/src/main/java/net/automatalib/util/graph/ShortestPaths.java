/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import net.automatalib.graph.IndefiniteGraph;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Unweighted shortest path search in graphs.
 * <p>
 * This class offers an iterator-style approach to the shortest path search: the methods in this class generally return
 * either an {@link Iterator} or an {@link Iterable} wrapped around an iterator which allows for enumerating all
 * shortest paths to the given set of target nodes. The iterators implement this lazily, i.e., a call to the
 * {@link Iterator#next() next()} method of an iterator will continue the shortest path search on an as-needed basis.
 */
public final class ShortestPaths {

    private ShortestPaths() {
        // prevent instantiation
    }

    /**
     * Returns a shortest path from the start node to the target node, if available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start node
     * @param limit
     *         a limit on the maximum path length
     * @param target
     *         the target node
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a shortest path from the start node to the target node, {@code null} if such a path does not exist or
     * exceeds the given {@code limit}.
     */
    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph, N start, int limit, N target) {
        return shortestPath(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    /**
     * Returns a shortest path from the start node to the first node that satisfies the given predicate, if available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start node
     * @param limit
     *         a limit on the maximum path length
     * @param pred
     *         the predicate that should be satisfied by the target node
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a shortest path from the start node to the first node that satisfies the given predicate, {@code null} if
     * such a path does not exist or exceeds the given {@code limit}.
     */
    public static <N, E> @Nullable Path<N, E> shortestPath(IndefiniteGraph<N, E> graph,
                                                           N start,
                                                           int limit,
                                                           Predicate<? super N> pred) {
        Iterator<Path<N, E>> spIt = shortestPathsIterator(graph, Collections.singleton(start), limit, pred);
        return spIt.hasNext() ? spIt.next() : null;
    }

    /**
     * Returns a collection of shortest paths from the start node to the target nodes, if available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start node
     * @param limit
     *         a limit on the maximum path length
     * @param targets
     *         the target nodes
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a collection of shortest paths from the start node to the target nodes. May be empty if such paths do not
     * exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            N start,
                                                            int limit,
                                                            Collection<?> targets) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    /**
     * Returns a collection of shortest paths from the start node to all nodes that satisfy the given predicate, if
     * available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start node
     * @param limit
     *         a limit on the maximum path length
     * @param pred
     *         the predicate that should be satisfied by the target nodes
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a collection of shortest paths from the start node to all nodes that satisfy the given predicate. May be
     * empty if such paths do not exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            N start,
                                                            int limit,
                                                            Predicate<? super N> pred) {
        return shortestPaths(graph, Collections.singleton(start), limit, pred);
    }

    /**
     * Returns a collection of shortest paths from the start nodes to the target node, if available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start nodes
     * @param limit
     *         a limit on the maximum path length
     * @param target
     *         the target node
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a collection of shortest paths from the start nodes to the target node. May be empty if such paths do not
     * exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            Collection<? extends N> start,
                                                            int limit,
                                                            N target) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
    }

    /**
     * Returns a collection of shortest paths from the start nodes to the target nodes, if available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start nodes
     * @param limit
     *         a limit on the maximum path length
     * @param targets
     *         the target nodes
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a collection of shortest paths from the start nodes to the target nodes. May be empty if such paths do
     * not exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            Collection<? extends N> start,
                                                            int limit,
                                                            Collection<?> targets) {
        return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
    }

    /**
     * Returns a collection of shortest paths from the start nodes to all nodes that satisfy the given predicate, if
     * available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start nodes
     * @param limit
     *         a limit on the maximum path length
     * @param pred
     *         the predicate that should be satisfied by the target nodes
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return a collection of shortest paths from the start nodes to all nodes that satisfy the given predicate. May be
     * empty if such paths do not exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterable<Path<N, E>> shortestPaths(IndefiniteGraph<N, E> graph,
                                                            Collection<? extends N> start,
                                                            int limit,
                                                            Predicate<? super N> pred) {
        return () -> shortestPathsIterator(graph, start, limit, pred);
    }

    /**
     * Returns an iterator of shortest paths from the start nodes to all nodes that satisfy the given predicate, if
     * available.
     *
     * @param graph
     *         the graph
     * @param start
     *         the start nodes
     * @param limit
     *         a limit on the maximum path length
     * @param pred
     *         the predicate that should be satisfied by the target nodes
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return an iterator of shortest paths from the start nodes to all nodes that satisfy the given predicate. May be
     * {@link Iterator#hasNext() empty} if such paths do not exist or exceed the given {@code limit}.
     */
    public static <N, E> Iterator<Path<N, E>> shortestPathsIterator(IndefiniteGraph<N, E> graph,
                                                                    Collection<? extends N> start,
                                                                    int limit,
                                                                    Predicate<? super N> pred) {
        return new FindShortestPathsIterator<>(graph, start, limit, pred);
    }

}
