package net.automatalib.semantics;

import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.MutableAutomaton;
import net.automatalib.automaton.UniversalAutomaton;

public interface FiniteSemantics extends Semantics {

    interface WildcardPlainSemantics<S, I> extends FiniteSemantics, Semantics.WildcardPlainSemantics<S, I> {

        Automaton<S, I, ?> getSemantics();

    }

    interface PlainSemantics<S, I, T>
            extends WildcardPlainSemantics<S, I>, Semantics.PlainSemantics<S, I, T>, Automaton<S, I, T> {

        @Override
        default Automaton<S, I, T> getSemantics() {
            return this;
        }
    }

    interface WildcardUniversalSemantics<S, I, SP, TP>
            extends WildcardPlainSemantics<S, I>, Semantics.WildcardUniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalAutomaton<S, I, ?, SP, TP> getSemantics();
    }

    interface UniversalSemantics<S, I, T, SP, TP> extends PlainSemantics<S, I, T>,
                                                          WildcardUniversalSemantics<S, I, SP, TP>,
                                                          Semantics.UniversalSemantics<S, I, T, SP, TP>,
                                                          UniversalAutomaton<S, I, T, SP, TP> {

        @Override
        default UniversalAutomaton<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

    interface MutableSemantics<S, I, T, SP, TP>
            extends UniversalSemantics<S, I, T, SP, TP>, MutableAutomaton<S, I, T, SP, TP> {}

}
