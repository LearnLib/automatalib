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
package net.automatalib.automaton.ra.guard.impl;

import java.util.Collections;
import java.util.Set;

import net.automatalib.data.DataValue;
import net.automatalib.data.Mapping;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.VarMapping;
import net.automatalib.automaton.ra.GuardExpression;

/**
 *
 * @author falk
 */
public class TrueGuardExpression implements GuardExpression {

    public static final TrueGuardExpression TRUE = new TrueGuardExpression();

    @Override
    public GuardExpression relabel(VarMapping<?, ?> relabelling) {
        return TRUE;
    }

    @Override
    public boolean isSatisfied(Mapping<SymbolicDataValue, DataValue<?>> val) {
        return true;
    }

    @Override
    public String toString() {
        return "TRUE";
    }

    @Override
    public Set<SymbolicDataValue> getSymbolicDataValues() {
        return Collections.emptySet();
    }
}
