/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.util.graphs.sssp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.commons.smartcollections.BinaryHeap;
import net.automatalib.commons.smartcollections.ElementReference;
import net.automatalib.commons.smartcollections.SmartDynamicPriorityQueue;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.EdgeWeights;
import net.automatalib.util.graphs.Graphs;

/**
 * Implementation of Dijkstras algorithm for the single-source shortest path problem.
 *
 * @param <N>
 *         node class
 * @param <E>
 *         edge class
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class DijkstraSSSP<N, E> implements SSSPResult<N, E> {

    private final Graph<N, E> graph;
    private final N init;
    private final EdgeWeights<E> edgeWeights;
    private final MutableMapping<N, Record<N, E>> records;

    /**
     * Constructor.
     *
     * @param graph
     *         the graph in which to search for shortest paths
     * @param init
     *         the initial node
     * @param edgeWeights
     *         the edge weights
     */
    public DijkstraSSSP(Graph<N, E> graph, N init, EdgeWeights<E> edgeWeights) {
        this.graph = graph;
        this.init = init;
        this.edgeWeights = edgeWeights;
        this.records = graph.createStaticNodeMapping();
    }

    /**
     * Search for the shortest paths from a single source node in a graph.
     *
     * @param graph
     *         the graph in which to perform the search
     * @param init
     *         the initial (source) node
     * @param edgeWeights
     *         the edge weights
     *
     * @return the single-source shortest path results
     */
    @Nonnull
    public static <N, E> SSSPResult<N, E> findSSSP(Graph<N, E> graph, N init, EdgeWeights<E> edgeWeights) {
        DijkstraSSSP<N, E> dijkstra = new DijkstraSSSP<>(graph, init, edgeWeights);
        dijkstra.findSSSP();
        return dijkstra;
    }

    /**
     * Start the search. This method may only be invoked once.
     */
    public void findSSSP() {
        Record<N, E> initRec = new Record<>(init, 0.0f);
        if (records.put(init, initRec) != null) {
            throw new IllegalStateException("Search has already been performed!");
        }

        SmartDynamicPriorityQueue<Record<N, E>> pq = BinaryHeap.create(graph.size());
        initRec.ref = pq.referencedAdd(initRec);

        while (!pq.isEmpty()) {
            // Remove node with minimum distance
            Record<N, E> rec = pq.extractMin();
            float dist = rec.dist;

            N node = rec.node;

            // edge scanning
            for (E edge : graph.getOutgoingEdges(node)) {
                float w = edgeWeights.getEdgeWeight(edge);
                float newDist = dist + w;

                N tgt = graph.getTarget(edge);
                Record<N, E> tgtRec = records.get(tgt);
                if (tgtRec == null) {
                    // node has not been visited before, add a record
                    // and add it to the queue
                    tgtRec = new Record<>(tgt, newDist, edge, rec);
                    tgtRec.ref = pq.referencedAdd(tgtRec);
                    records.put(tgt, tgtRec);
                } else if (newDist < tgtRec.dist) {
                    // using currently considered edge decreases current distance
                    tgtRec.dist = newDist;
                    tgtRec.reach = edge;
                    tgtRec.depth = rec.depth + 1;
                    tgtRec.parent = rec;
                    // update it's position in the queue
                    pq.keyChanged(tgtRec.ref);
                }
            }
        }
    }

    @Override
    public N getInitialNode() {
        return init;
    }

    @Override
    public float getShortestPathDistance(N target) {
        Record<N, E> rec = records.get(target);
        if (rec == null) {
            return Graphs.INVALID_DISTANCE;
        }
        return rec.dist;
    }

    @Override
    public List<E> getShortestPath(N target) {
        Record<N, E> rec = records.get(target);
        if (rec == null) {
            return null;
        }

        if (rec.depth == 0) {
            return Collections.emptyList();
        }

        List<E> result = new ArrayList<>(rec.depth);

        E edge;
        while ((edge = rec.reach) != null) {
            result.add(edge);
            rec = rec.parent;
        }

        Collections.reverse(result);
        return result;
    }

    @Override
    public E getShortestPathEdge(N target) {
        Record<N, E> rec = records.get(target);
        if (rec == null) {
            return null;
        }
        return rec.reach;
    }

    /**
     * Internal data record.
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    @ParametersAreNonnullByDefault
    private static final class Record<N, E> implements Comparable<Record<N, E>> {

        @Nonnull
        public final N node;
        public float dist;
        @Nullable
        public ElementReference ref;
        @Nullable
        public E reach;
        @Nullable
        public Record<N, E> parent;
        int depth;

        Record(N node, float dist) {
            this(node, dist, null, null);
        }

        Record(N node, float dist, @Nullable E reach, @Nullable Record<N, E> parent) {
            this.node = node;
            this.dist = dist;
            this.reach = reach;
            this.parent = parent;
            this.depth = (parent != null) ? parent.depth + 1 : 0;
        }

        @Override
        public int compareTo(Record<N, E> o) {
            return Float.compare(dist, o.dist);
        }
    }
}
