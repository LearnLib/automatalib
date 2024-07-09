/*
 * Copyright (C) 2014-2015 The LearnLib Contributors
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package net.automatalib.data;

import java.util.Objects;

/**
 * A user-defined type of data values.
 *
 * @author falk
 */
public final class DataType<T> {

    /**
     * name of type (defining member)
     */
    final String name;

    /**
     * base type
     */
    final Class<T> base;

    public DataType(String name, Class<T> base) {
        this.name = name;
        this.base = base;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataType<?> other = (DataType<?>) obj;
        return Objects.equals(this.name, other.name);
    }

    public String getName() {
        return name;
    }

    public Class<T> getBase() {
        return base;
    }
}
