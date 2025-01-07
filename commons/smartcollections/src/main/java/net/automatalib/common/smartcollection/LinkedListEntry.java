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
package net.automatalib.common.smartcollection;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic interface for entries in a linked list.
 *
 * @param <E>
 *         element class.
 * @param <T>
 *         linked list entry class.
 */
public interface LinkedListEntry<E, T extends LinkedListEntry<E, T>> extends ElementReference {

    /**
     * Retrieves the element stored at this position in the list.
     *
     * @return the element.
     */
    E getElement();

    /**
     * Retrieves the previous entry in the list, or {@code null} if this is the first entry.
     *
     * @return the previous entry or {@code null}.
     */
    @Nullable T getPrev();

    /**
     * Sets the predecessor of this entry.
     *
     * @param prev
     *         the new predecessor.
     */
    void setPrev(@Nullable T prev);

    /**
     * Retrieves the next entry in the list, or {@code null} if this is the last entry.
     *
     * @return the next entry or {@code null}.
     */
    @Nullable T getNext();

    /**
     * Sets the successor of this entry.
     *
     * @param next
     *         the new successor.
     */
    void setNext(@Nullable T next);
}
