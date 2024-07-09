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
 * Symbolic Data Values (Parameters, registers, etc.).
 *
 * @author falk
 */
public abstract class SymbolicDataValue<T> {

    protected final DataType<T> type;
    protected final int id;

    private SymbolicDataValue(DataType<T> dataType, int id) {
        this.type = dataType;
        this.id = id;
    }

    public DataType<T> getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public static final class Parameter<T> extends SymbolicDataValue<T> {

        public Parameter(DataType<T> dataType, int id) {
            super(dataType, id);
        }

        @Override
        public SymbolicDataValue.Parameter<T> copy() {
        	return new SymbolicDataValue.Parameter<>(type, id);
        }

        @Override
        public String toString() {
            return "p" + this.id;
        }
    };

    public static final class Register<T> extends SymbolicDataValue<T> {

        public Register(DataType<T> dataType, int id) {
            super(dataType, id);
        }

        @Override
        public SymbolicDataValue.Register<T> copy() {
        	return new SymbolicDataValue.Register<>(type, id);
        }

        @Override
        public String toString() {
            return "r" + this.id;
        }
    };

    public static final class Constant<T> extends SymbolicDataValue<T> {

        public Constant(DataType<T> dataType, int id) {
            super(dataType, id);
        }

        @Override
        public SymbolicDataValue.Constant<T> copy() {
        	return new SymbolicDataValue.Constant<>(type, id);
        }

        @Override
        public String toString() {
            return "c" + this.id;
        }
    };

    public static final class SuffixValue<T> extends SymbolicDataValue<T> {

        public SuffixValue(DataType<T> dataType, int id) {
            super(dataType, id);
        }

        @Override
        public SymbolicDataValue.SuffixValue<T> copy() {
        	return new SymbolicDataValue.SuffixValue<>(type, id);
        }

        @Override
        public String toString() {
            return "s" + this.id;
        }
    };

    public abstract SymbolicDataValue<T> copy();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final SymbolicDataValue<?> other = (SymbolicDataValue<?>) obj;

        return this.id == other.id && Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.getClass());
        return hash;
    }

    public boolean isRegister() {
        return this.getClass().equals(Register.class);
    }

    public boolean isParameter() {
        return this.getClass().equals(Parameter.class);
    }

    public boolean isConstant() {
        return this.getClass().equals(Constant.class);
    }

    public boolean isSuffixValue() {
        return this.getClass().equals(SuffixValue.class);
    }
}
