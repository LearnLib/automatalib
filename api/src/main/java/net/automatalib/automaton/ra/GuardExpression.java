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
package net.automatalib.automaton.ra;

import java.util.Set;

import net.automatalib.data.DataValue;
import net.automatalib.data.Mapping;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.VarMapping;

/**
 *
 * @author falk
 *
 */
public interface GuardExpression {

    GuardExpression relabel(VarMapping relabelling);

    boolean isSatisfied(Mapping<SymbolicDataValue, DataValue<?>> val);

    Set<SymbolicDataValue> getSymbolicDataValues();
}
