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
 * Mutable pair class.
 *
 * @param <T1>
 *         type of the pair's first component.
 * @param <T2>
 *         type of the pair's second component.
 *
 * @author Malte Isberner
 */
@EqualsAndHashCode
public class Pair<T1, T2> extends AbstractPrintable implements Serializable {

    private static final long serialVersionUID = -1L;

    /*
     * Components
     */
    protected T1 first;
    protected T2 second;

    /**
     * Constructs a pair with the given members.
     *
     * @param first
     *         first member.
     * @param second
     *         second member.
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Convenience function for creating a pair, allowing the user to omit the type parameters.
     *
     * @see #Pair(Object, Object)
     */
    public static <T1, T2> Pair<T1, T2> make(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    /**
     * Getter for the first member.
     *
     * @return the first member.
     */
    public T1 getFirst() {
        return first;
    }

    /**
     * Setter for the first member.
     *
     * @param first
     *         the new value for the first member.
     */
    public void setFirst(T1 first) {
        this.first = first;
    }

    /**
     * Getter for the second member.
     *
     * @return the second member.
     */
    public T2 getSecond() {
        return second;
    }

    /**
     * Setter for the second member.
     *
     * @param second
     *         the new value for the second member.
     */
    public void setSecond(T2 second) {
        this.second = second;
    }

    @Override
    public void print(Appendable a) throws IOException {
        StringUtil.appendObject(a, first);
        a.append(", ");
        StringUtil.appendObject(a, second);
    }
}
