package net.automatalib.automaton.ra;

import java.util.Collection;

import net.automatalib.data.Constants;
import net.automatalib.data.ParValuation;
import net.automatalib.data.VarValuation;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.ts.acceptor.DeterministicAcceptorTS;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AcceptorView<L, T extends GuardedTransition>
        implements DeterministicAcceptorTS<State<L>, PSymbolInstance> {

    private final RegisterAutomaton<L, T> ra;

    public AcceptorView(RegisterAutomaton<L, T> ra) {
        this.ra = ra;
    }

    @Override
    public @Nullable State<L> getTransition(State<L> state, PSymbolInstance input) {
        L location = state.getLocation();
        VarValuation vars = state.getValuation();
        ParValuation pars = new ParValuation(input);
        Constants constants = ra.getConstants();

        Collection<T> candidates = ra.getTransitions(location, input.getBaseSymbol());

        for (T t : candidates) {
            if (t.isEnabled(vars, pars, constants)) {
                vars = t.execute(vars, pars, constants);
                L succ = ra.getSuccessor(t);
                return new State<>(succ, vars);
            }
        }

        return null;
    }

    @Override
    public boolean isAccepting(State<L> state) {
        return ra.getStateProperty(state.getLocation());
    }

    @Override
    public @Nullable State<L> getInitialState() {
        final L initialState = ra.getInitialState();

        if (initialState == null) {
            return null;
        }

        return new State<>(initialState, ra.getInitialRegisters());
    }
}