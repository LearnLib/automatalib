/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.graphs;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.graphs.IndefiniteGraph;

/**
 * Unweighted shortest path search in graphs.
 * <p>
 * This class offers an iterator-style approach to shortest path search: the methods in this class generally
 * return either an {@link Iterator} or an {@link Iterable} wrapped around an iterator which allows for enumerating
 * all shortest paths to the given set of target nodes. The iterators implement this lazily, i.e., a call to
 * the {@link Iterator#next() next()} method of an iterator will continue the shortest path search
 * on an as-needed basis.
 * 
 * @author Malte Isberner
 *
 */
@ParametersAreNonnullByDefault
public abstract class ShortestPaths {
	
	
	public static <N,E>
	Iterator<Path<N,E>> shortestPathsIterator(IndefiniteGraph<N,E> graph, Collection<? extends N> start, int limit,
			Predicate<? super N> targetPred) {
		return new FindShortestPathsIterator<>(graph, start, limit, targetPred);
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(final IndefiniteGraph<N, E> graph, final Collection<? extends N> start,
			final int limit, final Predicate<? super N> targetPred) {
		Objects.requireNonNull(graph);
		Objects.requireNonNull(start);
		Objects.requireNonNull(targetPred);
		return () -> shortestPathsIterator(graph, start, limit, targetPred);
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(IndefiniteGraph<N, E> graph, @Nullable N start,
			int limit, Predicate<? super N> targetPred) {
		return shortestPaths(graph, Collections.singleton(start), limit, targetPred);
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(IndefiniteGraph<N, E> graph, @Nullable N start,
			int limit, N target) {
		return shortestPaths(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(IndefiniteGraph<N, E> graph, @Nullable N start,
			int limit, Collection<?> targets) {
		return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(IndefiniteGraph<N, E> graph, Collection<? extends N> start,
			int limit, @Nullable N target) {
		return shortestPaths(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
	}
	
	public static <N,E>
	Iterable<Path<N,E>> shortestPaths(IndefiniteGraph<N, E> graph, Collection<? extends N> start,
			int limit, Collection<?> targets) {
		return shortestPaths(graph, start, limit, (Predicate<? super N>) targets::contains);
	}
	
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, Collection<? extends N> start,
			int limit, Predicate<? super N> targetPred) {
		Iterator<Path<N,E>> spIt = shortestPathsIterator(graph, start, limit, targetPred);
		return spIt.hasNext() ? spIt.next() : null;
	}
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, @Nullable N start, int limit, Predicate<? super N> targetPred) {
		return shortestPath(graph, Collections.singleton(start), limit, targetPred);
	}
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, @Nullable N start, int limit, Collection<?> targets) {
		return shortestPath(graph, start, limit, (Predicate<? super N>) targets::contains);
	}
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, @Nullable N start, int limit, @Nullable N target) {
		return shortestPath(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
	}
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, Collection<? extends N> start, int limit, Collection<?> targets) {
		return shortestPath(graph, start, limit, (Predicate<? super N>) targets::contains);
	}
	
	public static <N,E>
	Path<N,E> shortestPath(IndefiniteGraph<N, E> graph, Collection<? extends N> start, int limit, @Nullable N target) {
		return shortestPath(graph, start, limit, (Predicate<? super N>) n -> Objects.equals(n, target));
	}

	private ShortestPaths() {
		throw new AssertionError("Constructor should not be invoked");
	}

}
