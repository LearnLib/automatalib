/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.automaton;

import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.MutableDFA;
import net.automatalib.automaton.fsa.MutableNFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.MutableMooreMachine;
import net.automatalib.automaton.transducer.MutableSubsequentialTransducer;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.automaton.transducer.probabilistic.MutableProbabilisticMealy;
import net.automatalib.automaton.transducer.probabilistic.ProbabilisticMealyMachine;
import net.automatalib.semantic.Semantics;
import net.automatalib.ts.TransitionSystem;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SemanticsTest {

    @Test
    public void testIdentitySemantics() {
        checkSameSemantics(Automaton.RegularAutomaton.class);
        checkSameSemantics(UniversalAutomaton.RegularAutomaton.class);
        checkSameSemantics(DeterministicAutomaton.RegularAutomaton.class);
        checkSameSemantics(UniversalDeterministicAutomaton.RegularAutomaton.class);
        checkSameSemantics(MutableAutomaton.RegularAutomaton.class);
        checkSameSemantics(MutableDeterministic.RegularAutomaton.class);

        checkSameSemantics(DFA.class);
        checkSameSemantics(MutableDFA.class);
        checkSameSemantics(MealyMachine.class);
        checkSameSemantics(MutableMealyMachine.class);
        checkSameSemantics(ProbabilisticMealyMachine.class);
        checkSameSemantics(MutableProbabilisticMealy.class);
        checkSameSemantics(MooreMachine.class);
        checkSameSemantics(MutableMooreMachine.class);
        checkSameSemantics(NFA.class);
        checkSameSemantics(MutableNFA.class);
        checkSameSemantics(SubsequentialTransducer.class);
        checkSameSemantics(MutableSubsequentialTransducer.class);
        checkSameSemantics(ModalTransitionSystem.class);
        checkSameSemantics(MutableModalTransitionSystem.class);

        checkSameSemantics(SPA.class);
        checkSameSemantics(SBA.class);
        checkSameSemantics(SPMM.class);
    }

    private <T extends Semantics> void checkSameSemantics(Class<T> clazz) {
        final Semantics mock = Mockito.mock(clazz);
        final T automaton = Mockito.when(mock.getSemantics()).thenCallRealMethod().getMock();
        final TransitionSystem<?, ?, ?> semantics = automaton.getSemantics();

        Assert.assertSame(semantics, automaton);
    }

}
