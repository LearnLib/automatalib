package net.automatalib.automaton.ra.guard.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automaton.ra.GuardExpression;
import net.automatalib.data.DataValue;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.Valuation;
import net.automatalib.data.VarMapping;

public class SmallerGuard<T extends Comparable<T>, L extends SymbolicDataValue<T>, R extends SymbolicDataValue<T>> implements GuardExpression {

    private final L left;
    private final R right;

    public SmallerGuard(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfied(Valuation<?, ?> val) {
        DataValue<T> lv = val.get(left);
        DataValue<T> rv = val.get(right);

        assert lv != null && rv != null;

        return lv.getValue().compareTo(rv.getValue()) < 0;
    }

    @Override
    public GuardExpression relabel(VarMapping<?, ?> relabelling) {
        SymbolicDataValue<T> newLeft = relabelling.get(left);
        if (newLeft == null) {
            newLeft = left;
        }
        SymbolicDataValue<T> newRight = relabelling.get(right);
        if (newRight == null) {
            newRight = right;
        }

        return new SmallerGuard<>(newLeft, newRight);
    }

    @Override
    public Set<SymbolicDataValue<?>> getSymbolicDataValues() {
        return new LinkedHashSet<>(Arrays.asList(left, right));
    }

}
