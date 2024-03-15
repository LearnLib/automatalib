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

import net.automatalib.data.DataValue;

/**
 * A concrete data symbol.
 *
 * @author falk
 */
public class PSymbolInstance {

    /**
     * action
     */
    private final ParameterizedSymbol baseSymbol;

    /**
     * concrete parameter values
     */
    private final DataValue<?>[] parameterValues;

    public PSymbolInstance(ParameterizedSymbol baseSymbol,
            DataValue<?> ... parameterValues) {
        this.baseSymbol = baseSymbol;
        this.parameterValues = parameterValues;
    }

    public ParameterizedSymbol getBaseSymbol() {
        return baseSymbol;
    }

    public DataValue<?>[] getParameterValues() {
        return parameterValues;
    }

    @Override
    public String toString() {
        return this.baseSymbol.getName() + Arrays.toString(parameterValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PSymbolInstance other = (PSymbolInstance) obj;

        return  Objects.equals(this.baseSymbol, other.baseSymbol) && Arrays.deepEquals(this.parameterValues, other.parameterValues);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.baseSymbol != null ? this.baseSymbol.hashCode() : 0);
        hash = 11 * hash + Arrays.deepHashCode(this.parameterValues);
        return hash;
    }


}
