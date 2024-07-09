package net.automatalib.automaton.ra.guard.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automaton.ra.GuardExpression;
import net.automatalib.data.DataValue;
import net.automatalib.data.SymbolicDataValue;
import net.automatalib.data.Valuation;
import net.automatalib.data.VarMapping;

public class EqualityGuard implements GuardExpression {

    private final SymbolicDataValue<?> left;
    private final SymbolicDataValue<?> right;

    public EqualityGuard(SymbolicDataValue<?> left, SymbolicDataValue<?> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfied(Valuation<?, ?> val) {
        DataValue<?> lv = val.get(left);
        DataValue<?> rv = val.get(right);

        assert lv != null && rv != null;

        return lv.equals(rv);
    }

    @Override
    public GuardExpression relabel(VarMapping<?, ?> relabelling) {
        SymbolicDataValue<?> newLeft = relabelling.get(left);
        if (newLeft == null) {
            newLeft = left;
        }
        SymbolicDataValue<?> newRight = relabelling.get(right);
        if (newRight == null) {
            newRight = right;
        }

        return new EqualityGuard(newLeft, newRight);
    }

    @Override
    public Set<SymbolicDataValue<?>> getSymbolicDataValues() {
        return new LinkedHashSet<>(Arrays.asList(left, right));
    }

}
