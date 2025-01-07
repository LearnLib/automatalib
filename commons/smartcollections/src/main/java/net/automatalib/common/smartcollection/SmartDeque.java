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

/**
 * A double-ended queue (deque), allowing access, removal and insertion of elements both at the beginning and the end.
 *
 * @param <E>
 *         element class.
 */
public interface SmartDeque<E> extends SmartCollection<E> {

    /**
     * Adds an element at the beginning of the sequence.
     *
     * @param element
     *         the element to be added.
     *
     * @return the reference to the newly added element.
     */
    ElementReference pushFront(E element);

    /**
     * Adds an element at the end of the sequence.
     *
     * @param element
     *         the element to be added.
     *
     * @return the reference to the newly added element.
     */
    ElementReference pushBack(E element);

    /**
     * Retrieves the element at the beginning of the sequence, or {@code null} if the sequence is empty.
     *
     * @return the first element or {@code null}.
     */
    E getFront();

    /**
     * Retrieves the element at the end of the sequence, or {@code null} if the sequence is empty.
     *
     * @return the last element or {@code null}.
     */
    E getBack();

    /**
     * Retrieves the reference to the element at the beginning of the sequence, or {@code null} if the sequence is
     * empty.
     *
     * @return reference to the first element or {@code null}.
     */
    ElementReference getFrontReference();

    /**
     * Retrieves the reference to the element at the end of the sequence, or {@code null} if the sequence is empty.
     *
     * @return reference to the last element or {@code null}.
     */
    ElementReference getBackReference();

    /**
     * Retrieves and removes the element at the beginning of the sequence. If the sequence is empty, {@code null} is
     * returned.
     *
     * @return the previously first element or {@code null}.
     */
    E popFront();

    /**
     * Retrieves and removes the element at the beginning of the sequence. If the sequence is empty, {@code null} is
     * returned.
     *
     * @return the previously first element or {@code null}.
     */
    E popBack();

}
