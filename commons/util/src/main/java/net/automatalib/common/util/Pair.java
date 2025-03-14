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
package net.automatalib.common.util;

import java.io.IOException;
import java.util.Objects;

import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.common.util.string.StringUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Immutable pair class.
 * <p>
 * <b>Note</b>: this class should only be used for internal representations of tuples with value type semantics (e.g.
 * equality, only if all components are equal).
 * <p>
 * Whenever a public interface returns an aggregation of individual objects, a separate class should be created/used
 * that has meaningful identifiers for the individual components.
 *
 * @param <T1>
 *         type of the first component
 * @param <T2>
 *         type of the second component
 */
public final class Pair<T1, T2> extends AbstractPrintable {

    private final T1 first;
    private final T2 second;

    private Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new pair from the given components.
     *
     * @param first
     *         the first pair component
     * @param second
     *         the second pair component
     * @param <T1>
     *         type of the first component
     * @param <T2>
     *         type of the second component
     *
     * @return the new pair object
     */
    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public void print(Appendable a) throws IOException {
        a.append('(');
        StringUtil.appendObject(a, first);
        a.append(", ");
        StringUtil.appendObject(a, second);
        a.append(')');
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }

        final Pair<?, ?> that = (Pair<?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(first);
        result = 31 * result + Objects.hashCode(second);
        return result;
    }
}
