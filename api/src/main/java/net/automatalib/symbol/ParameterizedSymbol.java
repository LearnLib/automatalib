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
package net.automatalib.symbol;

import java.util.Arrays;
import java.util.Objects;

import net.automatalib.data.DataType;

/**
 * A symbol with typed parameters
 * (sometimes called an action).
 *
 * @author falk
 */
public abstract class ParameterizedSymbol {

    /**
     * name of symbol
     */
    private final String name;

    /**
     * parameter types
     */
    private final DataType<?>[] ptypes;

    public ParameterizedSymbol(String name, DataType<?> ... ptypes) {
        this.name = name;
        this.ptypes = ptypes;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + this.name.hashCode();
        hash = 71 * hash + Arrays.deepHashCode(this.ptypes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParameterizedSymbol other = (ParameterizedSymbol) obj;

        return Objects.equals(this.name, other.name) && Arrays.deepEquals(this.ptypes, other.ptypes);
    }

    @Override
    public String toString() {
        String[] tnames = new String[this.ptypes.length];
        for (int i = 0; i < this.ptypes.length; i++) {
            tnames[i] = ptypes[i].getName();
        }
        return this.name + Arrays.toString(tnames);
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the arity
     */
    public int getArity() {
        return this.ptypes.length;
    }

    public DataType<?>[] getPtypes() {
        return ptypes;
    }

}
