/* Copyright (C) 2017 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.graphs.ads;

import net.automatalib.graphs.Graph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * An interface representing a node in an adaptive distinguishing sequence (which essentially forms a decision tree).
 * For convenience, this interface extends the {@link Graph} interface so that an ADS may be passed easily to e.g.
 * GraphDOT methods.
 *
 * @param <S> (hypothesis) state type
 * @param <I> input alphabet type
 * @param <O> output alphabet type
 * @author frohme
 */
public interface ADSNode<S, I, O> extends Graph<ADSNode<S, I, O>, ADSNode<S, I, O>> {

	/**
	 * A utility method indicating whether {@code this} node represents a leaf of an ADS (and therefore referencing a
	 * hypothesis state) or an inner node (and therefore referencing an input symbol).
	 *
	 * @return {@code true} if {@code this} is a leaf of an ADS, {@code false} otherwise.
	 */
	boolean isLeaf();

	/**
	 * Returns the hypothesis state associated with this ADS node.
	 *
	 * @return {@code null} if {@code this} is an inner node (see {@link #isLeaf()}), the associated hypothesis state
	 * otherwise.
	 */
	S getHypothesisState();

	/**
	 * See {@link #getHypothesisState()}.
	 *
	 * @param state the hypothesis state to be associated with this ADS node.
	 * @throws UnsupportedOperationException if trying to set a hypothesis state on an inner node (see {@link #isLeaf()}).
	 */
	void setHypothesisState(final S state) throws UnsupportedOperationException;

	/**
	 * Returns the input symbol associated with this ADS node.
	 *
	 * @return {@code null} if {@code this} is a leaf node (see {@link #isLeaf()}), the associated input symbol
	 * otherwise.
	 */
	I getSymbol();

	/**
	 * See {@link #getSymbol()}.
	 *
	 * @param symbol the input symbol to be associated with this ADS node.
	 * @throws UnsupportedOperationException if trying to set an input symbol on a leaf node (see {@link #isLeaf()}).
	 */
	void setSymbol(final I symbol) throws UnsupportedOperationException;

	/**
	 * Returns the parent node of {@code this} node.
	 *
	 * @return The parent node of {@code this} ADS node. May be {@code null}, if {@code this} is the root node of an ADS.
	 */
	ADSNode<S, I, O> getParent();

	void setParent(final ADSNode<S, I, O> parent);

	/**
	 * Returns a mapping to the child nodes of {@code this} ADS node.
	 *
	 * @return A mapping from hypothesis outputs to child ADS nodes. May be empty/unmodifiable (for leaf nodes), but
	 * never {@code null}.
	 */
	Map<O, ADSNode<S, I, O>> getChildren();

	// default methods for graph interface
	@Override
	default Collection<? extends ADSNode<S, I, O>> getNodes() {
		final List<ADSNode<S, I, O>> result = new LinkedList<>();
		final Queue<ADSNode<S, I, O>> queue = new LinkedList<>();

		queue.add(this);

		// level-order iteration of the tree nodes
		while (!queue.isEmpty()) {
			final ADSNode<S, I, O> node = queue.poll();
			result.add(node);
			queue.addAll(node.getChildren().values());
		}

		return Collections.unmodifiableList(result);
	}

	@Override
	default Collection<? extends ADSNode<S, I, O>> getOutgoingEdges(final ADSNode<S, I, O> node) {
		return Collections.unmodifiableCollection(node.getChildren().values());
	}

	@Override
	default ADSNode<S, I, O> getTarget(final ADSNode<S, I, O> edge) {
		return edge;
	}

	@Override
	default GraphDOTHelper<ADSNode<S, I, O>, ADSNode<S, I, O>> getGraphDOTHelper() {
		return new DefaultDOTHelper<ADSNode<S, I, O>, ADSNode<S, I, O>>() {

			@Override
			public boolean getNodeProperties(final ADSNode<S, I, O> node, final Map<String, String> properties) {
				if (node.isLeaf()) {
					properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
					properties.put(NodeAttrs.LABEL, String.valueOf(node.getHypothesisState()));
				}
				else {
					properties.put(NodeAttrs.LABEL, node.toString());
					properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
				}

				return true;
			}

			@Override
			public boolean getEdgeProperties(final ADSNode<S, I, O> src,
											 final ADSNode<S, I, O> edge,
											 final ADSNode<S, I, O> tgt,
											 final Map<String, String> properties) {

				for (final Map.Entry<O, ADSNode<S, I, O>> e : src.getChildren().entrySet()) {
					if (e.getValue().equals(tgt)) {
						properties.put(EdgeAttrs.LABEL, e.getKey().toString());
						return true;
					}
				}
				return true;
			}
		};
	}
}
