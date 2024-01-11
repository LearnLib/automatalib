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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

class MappingCollection<D, R> extends AbstractCollection<R> {

    private final Collection<D> delegate;
    private final Function<? super D, ? extends R> mapping;

    MappingCollection(Collection<D> delegate, Function<? super D, ? extends R> mapping) {
        this.delegate = delegate;
        this.mapping = mapping;
    }

    @Override
    public Iterator<R> iterator() {
        return IteratorUtil.map(this.delegate.iterator(), this.mapping);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    @Override
    public void forEach(Consumer<? super R> action) {
        this.delegate.forEach((f) -> action.accept(this.mapping.apply(f)));
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super R> filter) {
        return this.delegate.removeIf((element) -> filter.test(this.mapping.apply(element)));
    }

    @Override
    public int size() {
        return this.delegate.size();
    }
}
