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

import net.automatalib.common.util.array.ArrayUtil;

class ConcatIterable<T> extends AbstractTwoLevelIterator<Iterable<? extends T>, T, T> {

    @SafeVarargs
    ConcatIterable(Iterable<? extends T>... delegates) {
        super(ArrayUtil.iterator(delegates));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Iterator<T> l2Iterator(Iterable<? extends T> l1Object) {
        return (Iterator<T>) l1Object.iterator();
    }

    @Override
    protected T combine(Iterable<? extends T> l1Object, T l2Object) {
        return l2Object;
    }

}
