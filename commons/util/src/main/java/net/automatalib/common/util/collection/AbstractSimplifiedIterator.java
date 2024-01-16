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
import java.util.NoSuchElementException;

/**
 * A simplified immutable iterator that only requires to implement the calculation of the next element.
 *
 * @param <E>
 *         element type
 */
public abstract class AbstractSimplifiedIterator<E> implements Iterator<E> {

    protected E nextValue;
    private State state = State.AWAIT_NEXT;

    private enum State {
        AWAIT_NEXT,
        HAS_NEXT,
        FINISHED
    }

    /**
     * The method to calculate the next element. This method <b>must</b> set the {@link #nextValue} reference to
     * indicate the next object and return {@code true} to signal that the next element is valid.
     *
     * @return {@code true} if the next element is valid, {@code false} if no more elements can be computed
     */
    protected abstract boolean calculateNext();

    private boolean advance() {
        boolean ret = calculateNext();
        if (!ret) {
            state = State.FINISHED;
        } else {
            state = State.HAS_NEXT;
        }
        return ret;
    }

    @Override
    public boolean hasNext() {
        switch (state) {
            case AWAIT_NEXT:
                return advance();
            case HAS_NEXT:
                return true;
            default: // case FINISHED:
                return false;
        }
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.AWAIT_NEXT;
        return nextValue;
    }

}
