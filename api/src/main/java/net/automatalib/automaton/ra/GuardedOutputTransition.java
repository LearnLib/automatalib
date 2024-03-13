package net.automatalib.automaton.ra;

import net.automatalib.data.Constants;
import net.automatalib.data.ParValuation;
import net.automatalib.data.VarValuation;

public interface GuardedOutputTransition extends GuardedTransition{

    //TODO needs to be VarMapping<Parameter, ? extends SymbolicDataValue>
    Assignment getOutputAssignment();

    ParValuation output(VarValuation registers, ParValuation parameters, Constants consts);

}
