package net.automatalib.automaton.ra;

import net.automatalib.data.Constants;
import net.automatalib.data.ParValuation;
import net.automatalib.data.VarValuation;

public interface GuardedTransition {

    Assignment getAssignment();

    TransitionGuard getGuard();

    default boolean isEnabled(VarValuation registers, ParValuation parameters, Constants consts) {
        return getGuard().isSatisfied(registers, parameters, consts);
    };

    default VarValuation execute(VarValuation registers, ParValuation parameters, Constants consts) {
        return getAssignment().compute(registers, parameters, consts);
    }
}
