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
package net.automatalib.util.minimizer;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import net.automatalib.common.util.collection.IteratorUtil;

/**
 * Class that maps a {@link Collection} of states to a collection of the respective original states.
 *
 * @param <S>
 *         state class.
 */
class OriginalStateCollection<S> extends AbstractCollection<S> {

    private final Collection<? extends State<S, ?>> stateColl;

    /**
     * Constructor.
     *
     * @param stateColl
     *         the backing state collection.
     */
    OriginalStateCollection(Collection<? extends State<S, ?>> stateColl) {
        this.stateColl = stateColl;
    }

    @Override
    public Iterator<S> iterator() {
        return IteratorUtil.map(stateColl.iterator(), State::getOriginalState);
    }

    @Override
    public int size() {
        return stateColl.size();
    }
}
