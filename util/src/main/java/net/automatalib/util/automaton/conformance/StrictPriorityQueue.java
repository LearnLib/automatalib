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
package net.automatalib.util.automaton.conformance;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;

import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A priority queue which enforces that no two elements that it contains are equal wrt. the specified comparator (i.e.,
 * {@link Comparator#compare(Object, Object)} does not return {@code 0} for two distinct elements).
 * <p>
 * If an element is inserted which, according to the {@link Comparator}, is already present, the specified {@link
 * MergeOperation}'s {@link MergeOperation#merge(Object, Object)} method is invoked to determine the replacement
 * element.
 * <p>
 * The name derives from the fact that subsequent calls to {@link #poll()} will yield a <i>strictly</i> growing sequence
 * of elements.
 * <p>
 * This class does not disallow {@code null} values, but the supplied {@link Comparator} has to support them.
 *
 * @param <E>
 *         element type
 */
class StrictPriorityQueue<E> extends AbstractQueue<E> {

    private final ArrayStorage<E> storage = new ArrayStorage<>();
    private final Comparator<? super E> comparator;
    private final MergeOperation<E> mergeOp;
    private int size;

    /**
     * Constructor.
     *
     * @param comparator
     *         the comparator used to compare elements
     * @param mergeOp
     *         the merge operation to perform for equally-ranked elements
     */
    StrictPriorityQueue(Comparator<? super E> comparator, MergeOperation<E> mergeOp) {
        this.comparator = comparator;
        this.mergeOp = mergeOp;
    }

    @Override
    public boolean offer(E e) {
        storage.ensureCapacity(size + 1);
        storage.set(size++, e);
        if (!upHeap()) {
            size--;
            return false;
        }
        return true;
    }

    /**
     * Moves the last element upwards in the heap until the heap condition is restored.
     *
     * @return {@code true} if the element has been inserted, {@code false} if it has been merged with an existing
     * element.
     */
    @SuppressWarnings("PMD.AvoidArrayLoops") // we move non-contiguous elements that can't be batched
    private boolean upHeap() {
        int currIdx = size - 1;
        E elem = storage.get(currIdx);

        int steps = 0;

        while (currIdx > 0) {
            int parentIdx = currIdx / 2;
            E parent = storage.get(parentIdx);
            int cmp = comparator.compare(elem, parent);
            if (cmp == 0) {
                storage.set(parentIdx, mergeOp.merge(parent, elem));
                return false;
            } else if (cmp > 0) {
                break;
            }

            currIdx = parentIdx;
            steps++;
        }

        currIdx = size - 1;
        for (int i = 0; i < steps; i++) {
            int parentIdx = currIdx / 2;
            storage.set(currIdx, storage.get(parentIdx));
            currIdx = parentIdx;
        }
        storage.set(currIdx, elem);

        return true;
    }

    @Override
    @SuppressWarnings("nullness") // setting 'null' is fine, because we also decrease the size
    public @Nullable E poll() {
        if (size == 0) {
            return null;
        }
        E result = storage.get(0);
        size--;
        if (size > 0) {
            storage.set(0, storage.get(size));
            downHeap();
        }
        storage.set(size, null);

        return result;
    }

    /**
     * Sifts the topmost element down into the heap until the heap condition is restored.
     */
    private void downHeap() {
        E elem = storage.get(0);
        int currIdx = 0;

        while (2 * currIdx + 1 < size) {
            int childIdx = 2 * currIdx + 1;
            E child = storage.get(childIdx);
            int rightChildIdx = childIdx + 1;

            if (rightChildIdx < size) {
                E rightChild = storage.get(rightChildIdx);
                if (comparator.compare(child, rightChild) > 0) {
                    child = rightChild;
                    childIdx = rightChildIdx;
                }
            }

            if (comparator.compare(elem, child) > 0) {
                storage.set(currIdx, child);
                storage.set(childIdx, elem);
                currIdx = childIdx;
            } else {
                return;
            }
        }
    }

    @Override
    public @Nullable E peek() {
        if (size == 0) {
            return null;
        }
        return storage.get(0);
    }

    @Override
    public Iterator<E> iterator() {
        return storage.iterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * The merge operation two perform on two equally-ranked elements.
     *
     * @param <E>
     *         element type
     */
    public interface MergeOperation<E> {

        /**
         * Merges the old element and the new element into a replacement element.
         * <p>
         * Implementations can assume that {@code cmp.compare(oldObject, newObject) == 0} holds for the comparator
         * {@code cmp} specified in {@link StrictPriorityQueue#StrictPriorityQueue(Comparator, MergeOperation)}. In
         * turn, they must guarantee that also {@code cmp.compare(result, oldObject) == 0} holds for the return value
         * {@code result}.
         *
         * @param oldObject
         *         the old element
         * @param newObject
         *         the new element
         *
         * @return the replacement element
         */
        E merge(E oldObject, E newObject);
    }

}
