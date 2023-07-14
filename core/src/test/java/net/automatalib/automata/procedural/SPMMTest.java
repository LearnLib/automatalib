/* Copyright (C) 2013-2023 TU Dortmund
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
package net.automatalib.automata.procedural;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MutableMealyMachine;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.automata.transducers.impl.FastMealyState;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultProceduralInputAlphabet;
import net.automatalib.words.impl.DefaultProceduralOutputAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class SPMMTest {

    private final SPMM<?, Character, ?, Character> spmm;

    public SPMMTest() {
        final ProceduralInputAlphabet<Character> inputAlphabet =
                new DefaultProceduralInputAlphabet<>(Alphabets.characters('a', 'c'), Alphabets.fromArray('S', 'T'), 'R');
        final ProceduralOutputAlphabet<Character> outputAlphabet =
                new DefaultProceduralOutputAlphabet<>(Alphabets.characters('x', 'z'), '✗');

        final Map<Character, MealyMachine<?, Character, ?, Character>> mealies =
                ImmutableMap.of('S', buildSProcedure(inputAlphabet), 'T', buildTProcedure(inputAlphabet));

        spmm = new StackSPMM<>(inputAlphabet, outputAlphabet, 'S', '✓', mealies);
    }

    private static MealyMachine<?, Character, ?, Character> buildSProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final CompactMealy<Character, Character> procedure = new CompactMealy<>(alphabet);

        final int s0 = procedure.addInitialState();
        final int s1 = procedure.addState();
        final int s2 = procedure.addState();
        final int s3 = procedure.addState();
        final int s4 = procedure.addState();
        final int s5 = procedure.addState();
        final int s6 = procedure.addState();

        procedure.addTransition(s0, 'a', s1, 'x');
        procedure.addTransition(s0, 'b', s2, 'y');
        procedure.addTransition(s0, 'T', s5, '✓');
        procedure.addTransition(s0, 'R', s6, '✓');
        procedure.addTransition(s1, 'S', s3, '✓');
        procedure.addTransition(s1, 'R', s6, '✓');
        procedure.addTransition(s2, 'S', s4, '✓');
        procedure.addTransition(s2, 'R', s6, '✓');
        procedure.addTransition(s3, 'a', s5, 'x');
        procedure.addTransition(s4, 'b', s5, 'y');
        procedure.addTransition(s5, 'R', s6, '✓');

        complete(procedure, alphabet, '✗');

        return procedure;
    }

    private static MealyMachine<?, Character, ?, Character> buildTProcedure(ProceduralInputAlphabet<Character> alphabet) {
        final FastMealy<Character, Character> procedure = new FastMealy<>(alphabet);

        final FastMealyState<Character> t0 = procedure.addInitialState();
        final FastMealyState<Character> t1 = procedure.addState();
        final FastMealyState<Character> t2 = procedure.addState();
        final FastMealyState<Character> t3 = procedure.addState();
        final FastMealyState<Character> t4 = procedure.addState();

        procedure.addTransition(t0, 'c', t1, 'z');
        procedure.addTransition(t0, 'S', t3, '✓');
        procedure.addTransition(t1, 'T', t2, '✓');
        procedure.addTransition(t1, 'R', t4, '✓');
        procedure.addTransition(t2, 'c', t3, 'z');
        procedure.addTransition(t3, 'R', t4, '✓');

        complete(procedure, alphabet, '✗');

        return procedure;
    }

    private static <S, I, O> void complete(MutableMealyMachine<S, I, ?, O> mealy,
                                           Collection<? extends I> inputs,
                                           O undefinedOutput) {
        S sink = null;

        for (S state : mealy) {
            for (I input : inputs) {
                S succ = mealy.getSuccessor(state, input);
                if (succ == null) {
                    if (sink == null) {
                        sink = mealy.addState();
                        for (I inputSym : inputs) {
                            mealy.addTransition(sink, inputSym, sink, undefinedOutput);
                        }
                    }
                    mealy.addTransition(state, input, sink, undefinedOutput);
                }
            }
        }
    }

    @Test
    public void testTransduction() {
        final Word<Character> i1 = Word.fromCharSequence("SaSTcRRaR");
        final Word<Character> o1 = Word.fromCharSequence("✓x✓✓z✓✓x✓");

        final Word<Character> i2 = Word.fromCharSequence("SaSbRaR");
        final Word<Character> o2 = Word.fromCharSequence("✓x✓y✓x✓");

        final Word<Character> i3 = Word.fromCharSequence("SaSbaRcRabc");
        final Word<Character> o3 = Word.fromCharSequence("✓x✓y✗✗✗✗✗✗✗");

        Assert.assertEquals(spmm.computeOutput(i1), o1);
        Assert.assertEquals(spmm.computeOutput(i2), o2);
        Assert.assertEquals(spmm.computeOutput(i3), o3);
    }
}
