package net.automatalib.semantics;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;

public interface DeterministicSemantics extends Semantics {

    interface WildcardPlainSemantics<S, I> extends DeterministicSemantics, Semantics.WildcardPlainSemantics<S, I> {

        DeterministicTransitionSystem<S, I, ?> getSemantics();

    }

    interface PlainSemantics<S, I, T> extends WildcardPlainSemantics<S, I>,
                                              Semantics.PlainSemantics<S, I, T>,
                                              DeterministicTransitionSystem<S, I, T> {

        @Override
        default DeterministicTransitionSystem<S, I, T> getSemantics() {
            return this;
        }
    }

    interface WildcardUniversalSemantics<S, I, SP, TP>
            extends WildcardPlainSemantics<S, I>, Semantics.WildcardUniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalDTS<S, I, ?, SP, TP> getSemantics();
    }

    interface UniversalSemantics<S, I, T, SP, TP> extends PlainSemantics<S, I, T>,
                                                          WildcardUniversalSemantics<S, I, SP, TP>,
                                                          Semantics.UniversalSemantics<S, I, T, SP, TP>,
                                                          UniversalDTS<S, I, T, SP, TP> {

        @Override
        default UniversalDTS<S, I, T, SP, TP> getSemantics() {
            return this;
        }
    }

}
