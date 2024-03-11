package net.automatalib.automaton.ra;

import net.automatalib.data.Constants;
import net.automatalib.data.ParValuation;
import net.automatalib.data.VarValuation;

public interface TransitionGuard {

    boolean isSatisfied(VarValuation registers, ParValuation parameters, Constants consts);

    GuardExpression getCondition();
}
