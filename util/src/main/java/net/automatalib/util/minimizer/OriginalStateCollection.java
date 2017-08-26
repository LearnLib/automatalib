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
package net.automatalib.util.minimizer;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class that maps a {@link Collection} of states to a collection of the respective original states.
 *
 * @param <S>
 *         state class.
 *
 * @author Malte Isberner
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
        final Iterator<? extends State<S, ?>> stateIt = stateColl.iterator();
        return new Iterator<S>() {

            @Override
            public boolean hasNext() {
                return stateIt.hasNext();
            }

            @Override
            public S next() {
                return stateIt.next().getOriginalState();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removal not " + "allowed on this collection!");
            }

        };
    }

    @Override
    public int size() {
        return stateColl.size();
    }
}
