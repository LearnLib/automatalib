/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.commons.util.mappings;

import java.util.Iterator;

/**
 * A transparent iterator wrapper that applies a {@link Mapping} to each element returned by an underlying iterator.
 *
 * @param <D>
 *         domain (original iterator type) class.
 * @param <R>
 *         range (resulting iterator type) class.
 *
 * @author Malte Isberner
 */
final class MappedIterator<D, R> implements Iterator<R> {

    // the mapping to apply
    private final Mapping<? super D, R> mapping;
    // the underlying iterator
    private final Iterator<? extends D> baseIt;

    /**
     * Constructor.
     *
     * @param mapping
     *         the mapping to apply.
     * @param baseIt
     *         the underlying iterator.
     */
    MappedIterator(Mapping<? super D, R> mapping, Iterator<? extends D> baseIt) {
        this.mapping = mapping;
        this.baseIt = baseIt;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return baseIt.hasNext();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public R next() {
        return mapping.get(baseIt.next());
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        baseIt.remove();
    }

}
