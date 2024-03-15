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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.data.DataValue;
import net.automatalib.data.Mapping;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.VarMapping;
import net.automatalib.automaton.ra.GuardExpression;

/**
 *
 * @author falk
 * @param <Left>
 * @param <Right>
 */
public class AtomicGuardExpression<Left extends SymbolicDataValue, Right extends SymbolicDataValue> implements
                                                                                                    GuardExpression {

    private final Left left;

    private final Relation relation;

    private final Right right;

    public AtomicGuardExpression(Left left, Relation relation, Right right) {
        this.left = left;
        this.relation = relation;
        this.right = right;
    }

    @Override
    public boolean isSatisfied(Mapping<SymbolicDataValue, DataValue<?>> val) {

        DataValue<?> lv = val.get(left);
        DataValue<?> rv = val.get(right);

        //System.out.println(this);
        //System.out.println(val.toString());

        assert lv != null && rv != null;

        switch (relation) {
            case EQUALS:
                return lv.equals(rv);
            case NOT_EQUALS:
                return !lv.equals(rv);
            case BIGGER:
            case SMALLER:
                return numCompare(lv, rv, relation);

            default:
                throw new UnsupportedOperationException(
                        "Relation " + relation + " is not supported in guards");
        }
    }

    @Override
    public GuardExpression relabel(VarMapping<?, ?> relabelling) {
        SymbolicDataValue newLeft = relabelling.get(left);
        if (newLeft == null) {
            newLeft = left;
        }
        SymbolicDataValue newRight = relabelling.get(right);
        if (newRight == null) {
            newRight = right;
        }

        return new AtomicGuardExpression<>(newLeft, relation, newRight);
    }

    @Override
    public String toString() {
        return "(" + left + relation + right + ")";
    }

    public Left getLeft() {
        return left;
    }

    public Right getRight() {
        return right;
    }

    @Override
    public Set<SymbolicDataValue> getSymbolicDataValues() {
        return new LinkedHashSet<>(Arrays.asList(left, right));
    }

    public Relation getRelation() {
        return relation;
    }

    private boolean numCompare(DataValue l, DataValue r, Relation relation) {
        if (!l.getType().equals(r.getType())) {
            return false;
        }

        Comparable lc = (Comparable) l.getId();
        int result = lc.compareTo(r.getId());
        switch (relation) {
            case SMALLER:
                return result < 0;
            case BIGGER:
                return result > 0;

            default:
                throw new UnsupportedOperationException(
                        "Relation " + relation + " is not supported in guards");
        }
    }

}
