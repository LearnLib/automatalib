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
package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that aggregates elements of a given source iterator in batches of a given size. While elements are
 * collected eagerly within a batch, the overall batches are computed lazily.
 * <p>
 * The source iterator is consumed in this process.
 * <p>
 * <b>Note:</b> Subsequent calls to the {@link #next()} method return a reference to the same batch, and only update the
 * contents of the batch. If you plan to reuse intermediate results, you'll need to explicitly copy them.
 *
 * @param <T>
 *         type of elements to aggregate
 *
 * @author frohme
 */
public class BatchingIterator<T> implements Iterator<List<T>> {

    private final int batchSize;

    private final Iterator<T> source;
    private final List<T> batch;

    public BatchingIterator(final Iterator<T> source, final int batchSize) {
        this.batchSize = batchSize;
        this.source = source;
        this.batch = new ArrayList<>(batchSize);
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    @Override
    public List<T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        this.batch.clear();

        while (source.hasNext() && batch.size() < batchSize) {
            batch.add(source.next());
        }

        return batch;
    }

}