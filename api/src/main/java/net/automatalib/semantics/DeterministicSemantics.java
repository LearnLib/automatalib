package net.automatalib.semantics;

import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;

public interface DeterministicSemantics {

    interface SimpleSemantics<S, I> {

        DeterministicTransitionSystem<S, I, ?> getSemantics();

    }

    interface UniversalSemantics<S, I, SP, TP>
            extends SimpleSemantics<S, I>, Semantics.UniversalSemantics<S, I, SP, TP> {

        @Override
        UniversalDTS<S, I, ?, SP, TP> getSemantics();
    }

    interface FullSemantics<S, I, T, SP, TP>
            extends UniversalSemantics<S, I, SP, TP>, Semantics.FullSemantics<S, I, T, SP, TP> {

        @Override
        UniversalDTS<S, I, T, SP, TP> getSemantics();
    }

}
