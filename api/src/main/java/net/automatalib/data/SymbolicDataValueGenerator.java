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

/**
 * Generates symbolic data values with increasing ids starting from id=1.
 *
 * @author falk
 */
public abstract class SymbolicDataValueGenerator {

    protected int id = 1;

    private SymbolicDataValueGenerator() {
    }

    public void set(SymbolicDataValueGenerator g) {
        id = g.id;
    }

    public abstract SymbolicDataValue next(DataType<?> type);

    public static final class ParameterGenerator extends SymbolicDataValueGenerator {
        @Override
        public SymbolicDataValue.Parameter next(DataType<?> type) {
            return new SymbolicDataValue.Parameter(type, id++);
        }
    };

    public static final class RegisterGenerator extends SymbolicDataValueGenerator {
        @Override
        public SymbolicDataValue.Register next(DataType<?> type) {
            return new SymbolicDataValue.Register(type, id++);
        }
    };

    public static final class SuffixValueGenerator extends SymbolicDataValueGenerator {
        @Override
        public SymbolicDataValue.SuffixValue next(DataType<?> type) {
            return new SymbolicDataValue.SuffixValue(type, id++);
        }
    };

    public static final class ConstantGenerator extends SymbolicDataValueGenerator {
        @Override
        public SymbolicDataValue.Constant next(DataType<?> type) {
            return new SymbolicDataValue.Constant(type, id++);
        }
    };
}
