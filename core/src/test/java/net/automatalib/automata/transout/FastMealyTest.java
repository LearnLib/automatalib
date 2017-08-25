/* Copyright (C) 2013-2017 TU Dortmund
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

    private final static Symbol in_a = new Symbol("a");
    private final static Symbol in_b = new Symbol("b");

    private final static String out_ok = "ok";
    private final static String out_error = "error";

    @Test
    public static void testTrace() {

        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(in_a);
        trace.add(in_a);
        trace.add(in_a);

        List<String> output = new ArrayList<>();
        fm.trace(trace, output);

        Assert.assertEquals(output.size(), 3);

        Assert.assertEquals(output.get(0), out_ok);
        Assert.assertEquals(output.get(1), out_ok);
        Assert.assertEquals(output.get(2), out_error);

    }

    private static FastMealy<Symbol, String> constructMachine() {
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(in_a);
        alpha.add(in_b);

        FastMealy<Symbol, String> fm = new FastMealy<>(alpha);

        FastMealyState<String> s0 = fm.addInitialState(), s1 = fm.addState(), s2 = fm.addState();

        fm.addTransition(s0, in_a, s1, out_ok);
        fm.addTransition(s0, in_b, s0, out_error);

        fm.addTransition(s1, in_a, s2, out_ok);
        fm.addTransition(s1, in_b, s0, out_ok);

        fm.addTransition(s2, in_a, s2, out_error);
        fm.addTransition(s2, in_b, s1, out_ok);

        return fm;
    }

    @Test
    public static void testRemoveTransition() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(in_a);
        trace.add(in_a);

        FastMealyState<String> state = fm.getSuccessor(fm.getInitialState(), trace);
        MealyTransition<FastMealyState<String>, String> trans = fm.getTransition(state, in_a);

        fm.removeTransition(state, in_a, trans);

        trace.add(in_a);

        Assert.assertEquals(fm.getSuccessor(fm.getInitialState(), trace), null);
    }

    @Test
    public static void testRemoveAllTransitions() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(in_a);
        trace.add(in_a);

        FastMealyState<String> state = fm.getSuccessor(fm.getInitialState(), trace);

        fm.removeAllTransitions(state, in_a);

        trace.add(in_a);

        Assert.assertEquals(fm.getSuccessor(fm.getInitialState(), trace), null);
    }

    @Test
    public static void testRedefineTransition() {
        FastMealy<Symbol, String> fm = constructMachine();

        List<Symbol> trace = new ArrayList<>();
        trace.add(in_a);
        trace.add(in_a);

        // find last state in 3-state automaton
        FastMealyState<String> laststate = fm.getSuccessor(fm.getInitialState(), trace);

        // prepare and execute trace
        trace.add(in_a);
        List<String> output = new ArrayList<>();
        fm.trace(trace, output);

        // compare trace output with expectations
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), out_error);

        // redefine transition with diverging output
        fm.removeAllTransitions(laststate, in_a);
        fm.addTransition(laststate, in_a, fm.getInitialState(), out_ok);

        // retrace
        output.clear();
        fm.trace(trace, output);

        // compare output
        Assert.assertEquals(output.size(), 3);
        Assert.assertEquals(output.get(2), out_ok);

    }

}
