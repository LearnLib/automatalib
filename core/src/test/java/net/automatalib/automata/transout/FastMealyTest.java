/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.automata.transout;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Maik Merten
 */
public class FastMealyTest {

    private static final Symbol IN_A = new Symbol("a");
    private static final Symbol IN_B = new Symbol("b");

    private static final String OUT_OK = "ok";
    private static final String OUT_ERROR = "error";

    @Test
    public void testTrace() {

        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(IN_A);
        trace.add(IN_A);
        trace.add(IN_A);

        List<String> output = new ArrayList<>();
        fm.trace(trace, output);

        Assert.assertEquals(output.size(), 3);

        Assert.assertEquals(output.get(0), OUT_OK);
        Assert.assertEquals(output.get(1), OUT_OK);
        Assert.assertEquals(output.get(2), OUT_ERROR);

    }

    private static FastMealy<Symbol, String> constructMachine() {
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(IN_A);
        alpha.add(IN_B);

        FastMealy<Symbol, String> fm = new FastMealy<>(alpha);

        FastMealyState<String> s0 = fm.addInitialState(), s1 = fm.addState(), s2 = fm.addState();

        fm.addTransition(s0, IN_A, s1, OUT_OK);
        fm.addTransition(s0, IN_B, s0, OUT_ERROR);

        fm.addTransition(s1, IN_A, s2, OUT_OK);
        fm.addTransition(s1, IN_B, s0, OUT_OK);

        fm.addTransition(s2, IN_A, s2, OUT_ERROR);
        fm.addTransition(s2, IN_B, s1, OUT_OK);

        return fm;
    }

    @Test
    public void testRemoveTransition() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(IN_A);
        trace.add(IN_A);

        FastMealyState<String> state = fm.getSuccessor(fm.getInitialState(), trace);
        MealyTransition<FastMealyState<String>, String> trans = fm.getTransition(state, IN_A);

        fm.removeTransition(state, IN_A, trans);

        trace.add(IN_A);

        Assert.assertNull(fm.getSuccessor(fm.getInitialState(), trace));
    }

    @Test
    public static void testRemoveAllTransitions() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(IN_A);
        trace.add(IN_A);

        FastMealyState<String> state = fm.getSuccessor(fm.getInitialState(), trace);

        fm.removeAllTransitions(state, IN_A);

        trace.add(IN_A);

        Assert.assertNull(fm.getSuccessor(fm.getInitialState(), trace));
    }

    @Test
    public void testRedefineTransition() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(IN_A);
        trace.add(IN_A);

        // find last state in 3-state automaton
        FastMealyState<String> laststate = fm.getSuccessor(fm.getInitialState(), trace);

        // prepare and execute trace
        trace.add(IN_A);
        List<String> output = new ArrayList<>();
        fm.trace(trace, output);

        // compare trace output with expectations
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), OUT_ERROR);

        // redefine transition with diverging output
        fm.removeAllTransitions(laststate, IN_A);
        fm.addTransition(laststate, IN_A, fm.getInitialState(), OUT_OK);

        // retrace
        output.clear();
        fm.trace(trace, output);

        // compare output
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), OUT_OK);

    }

}
