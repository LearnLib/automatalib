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
package net.automatalib.common.util.collection;

import java.util.Iterator;
import java.util.function.Function;

class MappingIterator<D, R> implements Iterator<R> {

    private final Iterator<D> delegate;
    private final Function<? super D, ? extends R> mapping;

    MappingIterator(Iterator<D> delegate, Function<? super D, ? extends R> mapping) {
        this.delegate = delegate;
        this.mapping = mapping;
    }

    @Override
    public boolean hasNext() {
        return this.delegate.hasNext();
    }

    @Override
    public R next() {
        return this.mapping.apply(this.delegate.next());
    }

    @Override
    public void remove() {
        this.delegate.remove();
    }
}
