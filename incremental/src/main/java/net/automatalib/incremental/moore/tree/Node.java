/* Copyright (C) 2013-2024 TU Dortmund University
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
package net.automatalib.incremental.moore.tree;

import net.automatalib.common.smartcollection.ResizingArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A node in the tree internally used by {@link IncrementalMooreTreeBuilder}.
 *
 * @param <O>
 *         output symbol type
 */
final class Node<O> {

    private final O output;
    private @Nullable ResizingArrayStorage<Node<O>> children;

    /**
     * Constructor. Constructs a new node with no children and the specified acceptance value.
     *
     * @param output
     *         the output value for the node
     */
    Node(O output) {
        this.output = output;
    }

    /**
     * Retrieves the output value of this node.
     *
     * @return the output value of this node
     */
    O getOutput() {
        return output;
    }

    /**
     * Retrieves, for a given index, the respective child of this node.
     *
     * @param idx
     *         the alphabet symbol index
     *
     * @return the child for the given index, or {@code null} if there is no such child
     */
    @Nullable Node<O> getChild(int idx) {
        if (children == null) {
            return null;
        }
        return children.array[idx];
    }

    /**
     * Sets the child for a given index.
     *
     * @param idx
     *         the alphabet symbol index
     * @param alphabetSize
     *         the overall alphabet size; this is needed if a new children array needs to be created
     * @param child
     *         the new child
     */
    void setChild(int idx, int alphabetSize, Node<O> child) {
        if (children == null) {
            children = new ResizingArrayStorage<>(Node.class, alphabetSize);
        }
        children.array[idx] = child;
    }

    /**
     * See {@link ResizingArrayStorage#ensureCapacity(int)}.
     */
    void ensureInputCapacity(int capacity) {
        if (this.children != null) {
            this.children.ensureCapacity(capacity);
        }
    }
}
