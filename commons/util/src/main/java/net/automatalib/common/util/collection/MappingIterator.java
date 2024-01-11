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

class MappingIterator<T1, T2> implements Iterator<T2> {

    private final Iterator<T1> delegate;
    private final Function<? super T1, ? extends T2> mapping;

    MappingIterator(Iterator<T1> delegate, Function<? super T1, ? extends T2> mapping) {
        this.delegate = delegate;
        this.mapping = mapping;
    }

    @Override
    public boolean hasNext() {
        return this.delegate.hasNext();
    }

    @Override
    public T2 next() {
        return this.mapping.apply(this.delegate.next());
    }

    @Override
    public void remove() {
        this.delegate.remove();
    }
}
