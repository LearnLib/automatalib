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

import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automaton.ra.GuardExpression;
import net.automatalib.common.util.string.StringUtil;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.Valuation;
import net.automatalib.data.VarMapping;

/**
 *
 * @author falk
 */
public class Conjunction implements GuardExpression {

    private final GuardExpression[] conjuncts;

    public Conjunction(GuardExpression ... conjuncts) {
        this.conjuncts = conjuncts;
    }

    @Override
    public GuardExpression relabel(VarMapping<?, ?> relabelling) {
        GuardExpression[] newExpr = new GuardExpression[conjuncts.length];
        int i = 0;
        for (GuardExpression ge : conjuncts) {
            newExpr[i++] = ge.relabel(relabelling);
        }
        return new Conjunction(newExpr);
    }

    @Override
    public boolean isSatisfied(Valuation<?, ?> val) {
        for (GuardExpression ge : conjuncts) {
            if (!ge.isSatisfied(val)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return StringUtil.join(" && ", conjuncts);
    }

    @Override
    public Set<SymbolicDataValue<?>> getSymbolicDataValues() {
        final Set<SymbolicDataValue<?>> result = new LinkedHashSet<>();
        for (GuardExpression ge : conjuncts) {
            result.addAll(ge.getSymbolicDataValues());
        }
        return result;
    }

    public GuardExpression[] getConjuncts() {
        return conjuncts;
    }

}
