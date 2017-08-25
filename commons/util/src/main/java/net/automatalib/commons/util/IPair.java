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
package net.automatalib.commons.util;

import java.io.IOException;

import net.automatalib.commons.util.strings.AbstractPrintable;
import net.automatalib.commons.util.strings.StringUtil;

/**
 * Immutable pair class.
 *
 * @param <T1>
 *         first component type
 * @param <T2>
 *         second component type
 *
 * @author Malte Isberner
 */
public class IPair<T1, T2> extends AbstractPrintable {

    /*
     * Components
     */
    public final T1 first;
    public final T2 second;

    /**
     * Constructs a pair with the given members.
     *
     * @param first
     *         first member.
     * @param second
     *         second member.
     */
    public IPair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
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
     * Getter for the second member.
     *
     * @return the second member.
     */
    public T2 getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IPair<?, ?> other = (IPair<?, ?>) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see de.ls5.util.Printable#print(java.lang.Appendable)
     */
    @Override
    public void print(Appendable a) throws IOException {
        StringUtil.appendObject(a, first);
        a.append(", ");
        StringUtil.appendObject(a, second);
    }
}
