package net.automatalib.automaton.ra;

import net.automatalib.data.VarValuation;

public class State<L> {

    private final L location;
    private final VarValuation valuation;

    public State(L location, VarValuation valuation) {
        this.location = location;
        this.valuation = valuation;
    }

    public L getLocation() {
        return location;
    }

    public VarValuation getValuation() {
        return valuation;
    }
}
