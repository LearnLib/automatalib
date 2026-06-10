package net.automatalib.semantics;

import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalAutomaton;

public interface FiniteSemantics {

    interface SimpleSemantics<S, I> extends Semantics.SimpleSemantics<S, I> {

        Automaton<S, I, ?> getSemantics();

    }

    interface UniversalSemantics<S, I, SP, TP>
            extends SimpleSemantics<S, I>, Semantics.UniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalAutomaton<S, I, ?, SP, TP> getSemantics();
    }

    interface FullSemantics<S, I, T, SP, TP>
            extends UniversalSemantics<S, I, SP, TP>, Semantics.FullSemantics<S, I, T, SP, TP> {

        @Override
        UniversalAutomaton<S, I, T, SP, TP> getSemantics();
    }

    interface MutableSemantics<S, I, T, SP, TP> extends FullSemantics<S, I, T, SP, TP> {

        @Override
        MutableAutomaton<S, I, T, SP, TP> getSemantics();

    }

}
