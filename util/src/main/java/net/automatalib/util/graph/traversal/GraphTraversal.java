/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.util.graph.traversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import net.automatalib.common.util.Holder;
import net.automatalib.graph.IndefiniteGraph;
import net.automatalib.util.graph.traversal.DFRecord.LastEdge;
import net.automatalib.util.traversal.TraversalOrder;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GraphTraversal {

    public static final int NO_LIMIT = -1;

    private GraphTraversal() {
        // prevent inheritance
    }

    /**
     * Traverses the given graph in a breadth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void breadthFirst(IndefiniteGraph<N, E> graph,
                                              N initialNode,
                                              GraphTraversalVisitor<N, E, D> visitor) {
        breadthFirst(graph, NO_LIMIT, initialNode, visitor);
    }

    /**
     * Traverses the given graph in a breadth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void breadthFirst(IndefiniteGraph<N, E> graph,
                                              Collection<? extends N> initialNodes,
                                              GraphTraversalVisitor<N, E, D> visitor) {
        breadthFirst(graph, NO_LIMIT, initialNodes, visitor);
    }

    /**
     * Traverses the given graph in a breadth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 int limit,
                                                 N initialNode,
                                                 GraphTraversalVisitor<N, E, D> visitor) {
        return breadthFirst(graph, limit, Collections.singleton(initialNode), visitor);
    }

    /**
     * Traverses the given graph in a breadth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean breadthFirst(IndefiniteGraph<N, E> graph,
                                                 int limit,
                                                 Collection<? extends N> initialNodes,
                                                 GraphTraversalVisitor<N, E, D> visitor) {

        Queue<BFRecord<N, D>> bfsQueue = new ArrayDeque<>();

        // setting the following to false means that the traversal had to be aborted due to reaching the limit
        boolean complete = true;
        int nodeCount = 0;

        Holder<D> dataHolder = new Holder<>();

        for (N init : initialNodes) {
            dataHolder.value = null;
            GraphTraversalAction act = visitor.processInitial(init, dataHolder);

            switch (act) {
                case IGNORE:
                case ABORT_NODE:
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) { // not equals will always be true for negative limit values
                        bfsQueue.add(new BFRecord<>(init, dataHolder.value));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        bfs_loop:
        while (!bfsQueue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull BFRecord<N, D> current = bfsQueue.poll();

            N currNode = current.node;
            D currData = current.data;

            if (!visitor.startExploration(currNode, currData)) {
                continue;
            }

            Iterator<E> edges = graph.getOutgoingEdgesIterator(currNode);

            while (edges.hasNext()) {
                E edge = edges.next();
                N tgtNode = graph.getTarget(edge);

                dataHolder.value = null;
                GraphTraversalAction act = visitor.processEdge(currNode, currData, edge, tgtNode, dataHolder);

                switch (act) {
                    case IGNORE:
                        continue;
                    case ABORT_NODE:
                        continue bfs_loop;
                    case ABORT_TRAVERSAL:
                        return complete;
                    case EXPLORE:
                        if (nodeCount != limit) { // not equals will always be true for negative limit values
                            bfsQueue.offer(new BFRecord<>(tgtNode, dataHolder.value));
                            nodeCount++;
                        } else {
                            complete = false;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown action " + act);
                }
            }

            visitor.finishExploration(currNode, currData);
        }

        return complete;
    }

    /**
     * Returns an {@link Iterable} for the (reachable) nodes of the given graph in breadth-first order.
     *
     * @param graph
     *         the graph
     * @param start
     *         the nodes from which the traversal should start
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return an {@link Iterable} for the (reachable) nodes of the given graph in breadth-first order
     */
    public static <N, E> Iterable<N> breadthFirstOrder(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return () -> breadthFirstIterator(graph, start);
    }

    /**
     * Returns an {@link Iterator} for the (reachable) nodes of the given graph in breadth-first order.
     *
     * @param graph
     *         the graph
     * @param start
     *         the nodes from which the traversal should start
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return an {@link Iterator} for the (reachable) nodes of the given graph in breadth-first order
     */
    public static <N, E> Iterator<N> breadthFirstIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return new BreadthFirstIterator<>(graph, start);
    }

    /**
     * Traverses the given graph in a depth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void depthFirst(IndefiniteGraph<N, E> graph,
                                            N initialNode,
                                            GraphTraversalVisitor<N, E, D> visitor) {
        depthFirst(graph, NO_LIMIT, initialNode, visitor);
    }

    /**
     * Traverses the given graph in a depth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void depthFirst(IndefiniteGraph<N, E> graph,
                                            Collection<? extends N> initialNodes,
                                            GraphTraversalVisitor<N, E, D> visitor) {
        depthFirst(graph, NO_LIMIT, initialNodes, visitor);
    }

    /**
     * Traverses the given graph in a depth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               int limit,
                                               N initialNode,
                                               GraphTraversalVisitor<N, E, D> visitor) {
        return depthFirst(graph, limit, Collections.singleton(initialNode), visitor);
    }

    /**
     * Traverses the given graph in a breadth-first fashion. The traversal is steered by the specified visitor.
     *
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean depthFirst(IndefiniteGraph<N, E> graph,
                                               int limit,
                                               Collection<? extends N> initialNodes,
                                               GraphTraversalVisitor<N, E, D> visitor) {

        Deque<DFRecord<N, E, D>> dfsStack = new ArrayDeque<>();
        Holder<D> dataHolder = new Holder<>();

        // setting the following to false means that the traversal had to be aborted due to reaching the limit
        boolean complete = true;
        int nodeCount = 0;

        for (N init : initialNodes) {
            dataHolder.value = null;
            GraphTraversalAction act = visitor.processInitial(init, dataHolder);

            switch (act) {
                case IGNORE:
                case ABORT_NODE:
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) {
                        dfsStack.push(new DFRecord<>(init, dataHolder.value));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        while (!dfsStack.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull DFRecord<N, E, D> current = dfsStack.peek();

            N currNode = current.node;
            D currData = current.data;

            if (current.start(graph) && !visitor.startExploration(currNode, currData)) {
                dfsStack.pop();
                continue;
            }

            LastEdge<E, N, D> lastEdge = current.getLastEdge();
            if (lastEdge != null) {
                visitor.backtrackEdge(currNode, currData, lastEdge.edge, lastEdge.node, lastEdge.data);
            }

            if (!current.hasNextEdge()) {
                dfsStack.pop();
                visitor.finishExploration(currNode, currData);
                continue;
            }

            E edge = current.nextEdge();
            N tgt = graph.getTarget(edge);
            GraphTraversalAction act = visitor.processEdge(currNode, currData, edge, tgt, dataHolder);

            switch (act) {
                case IGNORE:
                    continue;
                case ABORT_NODE:
                    dfsStack.pop();
                    continue;
                case ABORT_TRAVERSAL:
                    return complete;
                case EXPLORE:
                    if (nodeCount != limit) {
                        D data = dataHolder.value;
                        current.setLastEdge(edge, tgt, data);
                        dfsStack.push(new DFRecord<>(tgt, data));
                        nodeCount++;
                    } else {
                        complete = false;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action " + act);
            }
        }

        return complete;
    }

    /**
     * Returns an {@link Iterable} for the (reachable) nodes of the given graph in depth-first order.
     *
     * @param graph
     *         the graph
     * @param start
     *         the nodes from which the traversal should start
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return an {@link Iterable} for the (reachable) nodes of the given graph in depth-first order
     */
    public static <N, E> Iterable<N> depthFirstOrder(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return () -> depthFirstIterator(graph, start);
    }

    /**
     * Returns an {@link Iterator} for the (reachable) nodes of the given graph in depth-first order.
     *
     * @param graph
     *         the graph
     * @param start
     *         the nodes from which the traversal should start
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     *
     * @return an {@link Iterator} for the (reachable) nodes of the given graph in depth-first order
     */
    public static <N, E> Iterator<N> depthFirstIterator(IndefiniteGraph<N, E> graph, Collection<? extends N> start) {
        return new DepthFirstIterator<>(graph, start);
    }

    /**
     * Traverses the given graph in a given order. The traversal is steered by the specified visitor.
     *
     * @param order
     *         the order in which the states should be traversed
     * @param graph
     *         the graph
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void traverse(TraversalOrder order,
                                          IndefiniteGraph<N, E> graph,
                                          N initialNode,
                                          GraphTraversalVisitor<N, E, D> visitor) {
        traverse(order, graph, NO_LIMIT, initialNode, visitor);
    }

    /**
     * Traverses the given graph in a given order. The traversal is steered by the specified visitor.
     *
     * @param order
     *         the order in which the states should be traversed
     * @param graph
     *         the graph
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     */
    public static <N, E, D> void traverse(TraversalOrder order,
                                          IndefiniteGraph<N, E> graph,
                                          Collection<? extends N> initialNodes,
                                          GraphTraversalVisitor<N, E, D> visitor) {
        traverse(order, graph, NO_LIMIT, initialNodes, visitor);
    }

    /**
     * Traverses the given graph in a given order. The traversal is steered by the specified visitor.
     *
     * @param order
     *         the order in which the states should be traversed
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNode
     *         the node from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             int limit,
                                             N initialNode,
                                             GraphTraversalVisitor<N, E, D> visitor) {
        return traverse(order, graph, limit, Collections.singleton(initialNode), visitor);
    }

    /**
     * Traverses the given graph in a given order. The traversal is steered by the specified visitor.
     *
     * @param order
     *         the order in which the states should be traversed
     * @param graph
     *         the graph
     * @param limit
     *         the upper bound on the number of nodes to be visited
     * @param initialNodes
     *         the nodes from which the traversal should start
     * @param visitor
     *         the visitor
     * @param <N>
     *         node type
     * @param <E>
     *         edge type
     * @param <D>
     *         (user) data type
     *
     * @return {@code false} if the number of explored nodes reached {@code limit}, {@code true} otherwise
     */
    public static <N, E, D> boolean traverse(TraversalOrder order,
                                             IndefiniteGraph<N, E> graph,
                                             int limit,
                                             Collection<? extends N> initialNodes,
                                             GraphTraversalVisitor<N, E, D> visitor) {
        switch (order) {
            case BREADTH_FIRST:
                return breadthFirst(graph, limit, initialNodes, visitor);
            case DEPTH_FIRST:
                return depthFirst(graph, limit, initialNodes, visitor);
            default:
                throw new IllegalArgumentException("Unknown traversal order " + order);
        }
    }

}
