/* Copyright (C) 2013 TU Dortmund
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

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.automatalib.graphs.IndefiniteGraph;

public class Path<N, E> extends AbstractList<E> {
	
	public static final class PathData<N,E> {
		public final N start;
		public final List<? extends E> edgeList;
		public PathData(N start, List<? extends E> edgeList) {
			this.start = start;
			this.edgeList = edgeList;
		}
		public Path<N,E> toPath(IndefiniteGraph<N,E> graph) {
			return new Path<>(graph, start, edgeList);
		}
	}
	
	private final class NodeIterator implements Iterator<N> {
		private Iterator<? extends E> edgeIt;
		@Override
		public boolean hasNext() {
			if(edgeIt == null)
				return true;
			return edgeIt.hasNext();
		}
		@Override
		public N next() {
			if(edgeIt == null) {
				edgeIt = edgeList.iterator();
				return start;
			}
			E edge = edgeIt.next();
			return graph.getTarget(edge);
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private class NodeList extends AbstractList<N> {
		@Override
		public N get(int index) {
			if(index < 0)
				throw new IndexOutOfBoundsException();
			if(index == 0)
				return start;
			E edge = edgeList.get(index - 1);
			return graph.getTarget(edge);
		}
		@Override
		public int size() {
			return edgeList.size() + 1;
		}
		@Override
		public Iterator<N> iterator() {
			return nodeIterator();
		}
		@Override
		public boolean isEmpty() {
			return false;
		}
	}
	
	private final IndefiniteGraph<N,E> graph;
	private final N start;
	private final List<? extends E> edgeList;

	public Path(IndefiniteGraph<N,E> graph, N start, List<? extends E> edgeList) {
		this.graph = graph;
		this.start = start;
		this.edgeList = edgeList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<E> iterator() {
		return (Iterator<E>)edgeList.iterator();
	}
	
	public Iterator<N> nodeIterator() {
		return new NodeIterator();
	}
	
	public Iterable<N> nodes() {
		return new Iterable<N>() {
			@Override
			public Iterator<N> iterator() {
				return nodeIterator();
			}
		};
	}
	
	public List<? extends E> edgeList() {
		return Collections.unmodifiableList(edgeList);
	}
	
	public List<N> nodeList() {
		return new NodeList();
	}
	
	
	public N firstNode() {
		return start;
	}
	
	public E firstEdge() {
		if(edgeList.isEmpty())
			return null;
		return edgeList.get(0);
	}
	
	public E lastEdge() {
		int idx = edgeList.size() - 1;
		if(idx < 0)
			return null;
		return edgeList.get(idx);
	}
	
	public N endNode() {
		E edge = lastEdge();
		if(edge == null)
			return start;
		return graph.getTarget(edge);
	}

	@Override
	public E get(int index) {
		return edgeList.get(index);
	}
	@Override
	public int size() {
		return edgeList.size();
	}
	@Override
	public boolean isEmpty() {
		return edgeList.isEmpty();
	}
}
