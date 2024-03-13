package net.automatalib.automaton.ra;

import java.util.Collection;

import net.automatalib.common.util.Triple;
import net.automatalib.data.Constants;
import net.automatalib.data.DataValue;
import net.automatalib.data.ParValuation;
import net.automatalib.data.SymbolicDataValueGenerator.ParameterGenerator;
import net.automatalib.data.VarValuation;
import net.automatalib.symbol.PSymbolInstance;
import net.automatalib.symbol.ParameterizedSymbol;
import net.automatalib.ts.output.MealyTransitionSystem;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TransducerView<L, T extends GuardedOutputTransition>
        implements MealyTransitionSystem<State<L>, PSymbolInstance, Triple<State<L>, PSymbolInstance, T>, PSymbolInstance> {

    private final RegisterMealyMachine<L, T> rmm;

    public TransducerView(RegisterMealyMachine<L, T> rmm) {
        this.rmm = rmm;
    }

    @Override
    public PSymbolInstance getTransitionOutput(Triple<State<L>, PSymbolInstance, T> triple) {
        State<L> state = triple.getFirst();
        PSymbolInstance input = triple.getSecond();
        VarValuation vars = state.getValuation();
        ParValuation pars = new ParValuation(input);
        Constants constants = rmm.getConstants();
        ParValuation outputMapping = triple.getThird().output(vars, pars, constants);

        ParameterizedSymbol output = this.rmm.getTransitionProperty(triple.getThird());

        ParameterGenerator pgen = new ParameterGenerator();
        DataValue<?>[] values = new DataValue[output.getArity()];

        for (int i = 0; i < values.length; i++) {
            values[i] = outputMapping.get(pgen.next(output.getPtypes()[i]));
        }

        return new PSymbolInstance(output, values);
    }

    @Override
    public @Nullable Triple<State<L>, PSymbolInstance, T> getTransition(State<L> state, PSymbolInstance input) {
        L location = state.getLocation();
        VarValuation vars = state.getValuation();
        ParValuation pars = new ParValuation(input);
        Constants constants = rmm.getConstants();

        Collection<T> candidates = rmm.getTransitions(location, input.getBaseSymbol());

        for (T t : candidates) {
            if (t.isEnabled(vars, pars, constants)) {
                return Triple.of(state, input, t);
            }
        }

        return null;
    }

    @Override
    public State<L> getSuccessor(Triple<State<L>, PSymbolInstance, T> triple) {
        VarValuation vars = triple.getFirst().getValuation();
        ParValuation pars = new ParValuation(triple.getSecond());
        Constants constants = rmm.getConstants();

        vars = triple.getThird().execute(vars, pars, constants);
        L succ = rmm.getSuccessor(triple.getThird());
        return new State<>(succ, vars);
    }

    @Override
    public @Nullable State<L> getInitialState() {
        final L initialState = rmm.getInitialState();

        if (initialState == null) {
            return null;
        }

        return new State<>(initialState, rmm.getInitialRegisters());
    }
}