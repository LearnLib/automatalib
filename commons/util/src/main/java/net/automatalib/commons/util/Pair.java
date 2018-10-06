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
package net.automatalib.commons.util;

import java.io.IOException;
import java.io.Serializable;

import lombok.EqualsAndHashCode;
import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.commons.util.strings.StringUtil;

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
 *         type of the pair's first component.
 * @param <T2>
 *         type of the pair's second component.
 *
 * @author Malte Isberner
 * @author frohme
 */
@EqualsAndHashCode(callSuper = false)
public final class Pair<T1, T2> extends AbstractPrintable implements Serializable {

    private static final long serialVersionUID = -1L;

    /*
     * Components
     */
    private final T1 first;
    private final T2 second;

    private Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Convenience function for creating a pair, allowing the user to omit the type parameters.
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
        StringUtil.appendObject(a, first);
        a.append(", ");
        StringUtil.appendObject(a, second);
    }
}
