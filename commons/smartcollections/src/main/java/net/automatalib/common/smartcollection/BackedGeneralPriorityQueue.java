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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.automatalib.common.util.collection.IteratorUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link SmartGeneralPriorityQueue} implementation that is backed by a {@link SmartDynamicPriorityQueue}.
 * <p>
 * The default {@link SmartDynamicPriorityQueue} to be used is a {@link BinaryHeap}, but every other implementation of
 * this interface may be used. The backing queue is specified in the constructor.
 *
 * @param <E>
 *         element class.
 * @param <K>
 *         key class.
 */
public class BackedGeneralPriorityQueue<E, K extends Comparable<K>> extends AbstractSmartCollection<E>
        implements SmartGeneralPriorityQueue<E, K> {

    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private final SmartDynamicPriorityQueue<Entry<E, K>> backingQueue;
    private @Nullable K defaultKey;

    public BackedGeneralPriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public BackedGeneralPriorityQueue(int initialCapacity) {
        this.backingQueue = BinaryHeap.create(initialCapacity);
    }

    public BackedGeneralPriorityQueue(List<? extends E> init, List<K> keys) {
        List<Entry<E, K>> entries = new ArrayList<>(init.size());

        Iterator<? extends E> elemIt = init.iterator();
        Iterator<K> keyIt = keys.iterator();

        while (elemIt.hasNext()) {
            K key = keyIt.hasNext() ? keyIt.next() : null;
            entries.add(new Entry<>(elemIt.next(), key));
        }

        this.backingQueue = BinaryHeap.create(entries);
    }

    public BackedGeneralPriorityQueue(Collection<Entry<E, K>> backingQueue) {
        this(backingQueue.size());
        this.backingQueue.addAll(backingQueue);
    }

    @Override
    public E choose() {
        return backingQueue.choose().element;
    }

    @Override
    public ElementReference chooseRef() {
        return backingQueue.chooseRef();
    }

    @Override
    public @Nullable ElementReference find(@Nullable Object element) {
        for (ElementReference ref : backingQueue.references()) {
            Entry<E, K> entry = backingQueue.get(ref);
            if (Objects.equals(entry.element, element)) {
                return ref;
            }
        }
        return null;
    }

    @Override
    public void quickClear() {
        backingQueue.quickClear();
    }

    @Override
    public void deepClear() {
        backingQueue.deepClear();
    }

    @Override
    public Iterator<E> iterator() {
        return IteratorUtil.map(backingQueue.iterator(), e -> e.element);
    }

    @Override
    public E get(ElementReference ref) {
        return backingQueue.get(ref).element;
    }

    @Override
    public ElementReference referencedAdd(E elem) {
        return add(elem, defaultKey);
    }

    @Override
    public ElementReference add(E elem, @Nullable K key) {
        Entry<E, K> entry = new Entry<>(elem, key);
        return backingQueue.referencedAdd(entry);
    }

    @Override
    public void setDefaultKey(K defaultKey) {
        this.defaultKey = defaultKey;
    }

    @Override
    public void changeKey(ElementReference ref, K newKey) {
        Entry<E, K> entry = backingQueue.get(ref);
        entry.key = newKey;
        backingQueue.keyChanged(ref);
    }

    @Override
    public void remove(ElementReference ref) {
        backingQueue.remove(ref);
    }

    @Override
    public Iterator<ElementReference> referenceIterator() {
        return backingQueue.referenceIterator();
    }

    @Override
    public void replace(ElementReference ref, E newElement) {
        Entry<E, K> entry = backingQueue.get(ref);
        entry.element = newElement;
    }

    @Override
    public int size() {
        return backingQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return backingQueue.isEmpty();
    }

    @Override
    public void clear() {
        backingQueue.clear();
    }

    @Override
    public E peekMin() {
        return backingQueue.peekMin().element;
    }

    @Override
    public E extractMin() {
        return backingQueue.extractMin().element;
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    public static class Entry<E, K extends Comparable<K>> implements Comparable<Entry<E, K>> {

        public E element;
        public @Nullable K key;

        Entry(E element, @Nullable K key) {
            this.element = element;
            this.key = key;
        }

        @Override
        public int compareTo(Entry<E, K> o) {
            if (o.key == null) {
                return key == null ? 0 : -1;
            }
            return key == null ? 1 : key.compareTo(o.key);
        }
    }

}
