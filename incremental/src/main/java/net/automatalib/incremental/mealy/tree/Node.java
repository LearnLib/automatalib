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
package net.automatalib.incremental.mealy.tree;

import java.util.Objects;

import net.automatalib.incremental.ConflictException;

final class Node<I, O> {

    private final Edge<I, O>[] outEdges;

    @SuppressWarnings("unchecked")
    Node(int alphabetSize) {
        this.outEdges = new Edge[alphabetSize];
    }

    public Edge<I, O> getEdge(int idx) {
        return outEdges[idx];
    }

    public void setEdge(int idx, Edge<I, O> edge) {
        outEdges[idx] = edge;
    }

    public void setSuccessor(int idx, O output, Node<I, O> succ) {
        outEdges[idx] = new Edge<>(output, succ);
    }

    public Node<I, O> getSuccessor(int idx) {
        Edge<I, O> edge = outEdges[idx];
        if (edge != null) {
            return edge.getTarget();
        }
        return null;
    }

    public Node<I, O> successor(int idx, O output) throws ConflictException {
        Edge<I, O> edge = outEdges[idx];
        if (edge != null) {
            if (!Objects.equals(output, edge.getOutput())) {
                throw new ConflictException("Output mismatch: '" + output + "' vs '" + edge.getOutput() + "'");
            }
            return edge.getTarget();
        }
        Node<I, O> succ = new Node<>(outEdges.length);
        edge = new Edge<>(output, succ);
        outEdges[idx] = edge;

        return succ;
    }
}
