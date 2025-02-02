/* Copyright (C) 2013-2025 TU Dortmund University
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
package net.automatalib.automaton.transducer.probabilistic.impl;

import java.util.Collection;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.probabilistic.MutableProbabilisticMealy;
import net.automatalib.automaton.transducer.probabilistic.ProbabilisticOutput;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MutableProbabilisticTest {

    @Test
    public void testFastProbMealy() {
        testMutableProbMealy(FastProbMealy::new);
    }

    private static <S, I, T, O, A extends MutableProbabilisticMealy<S, Integer, T, Character>> void testMutableProbMealy(
            AutomatonCreator<A, Integer> creator) {

        final Alphabet<Integer> alphabet = Alphabets.integers(0, 1);
        final A mealy = creator.createAutomaton(alphabet);

        final S s0 = mealy.addState();
        final S s1 = mealy.addState();
        final S s2 = mealy.addState();

        T t0 = mealy.addTransition(s0, 0, s1, new ProbabilisticOutput<>(0.5f, 'a'));
        T t1 = mealy.addTransition(s0, 1, s0, new ProbabilisticOutput<>(0.25f, 'a'));
        T t2 = mealy.addTransition(s1, 1, s2, new ProbabilisticOutput<>(0.6f, 'b'));

        Assert.assertEquals(mealy.getTransitionOutput(t0), 'a');
        Assert.assertEquals(mealy.getTransitionOutput(t1), 'a');
        Assert.assertEquals(mealy.getTransitionOutput(t2), 'b');

        Assert.assertEquals(mealy.getTransitionProbability(t0), 0.5f);
        Assert.assertEquals(mealy.getTransitionProbability(t1), 0.25f);
        Assert.assertEquals(mealy.getTransitionProbability(t2), 0.6f);

        mealy.setTransitionOutput(t0, 'b');
        mealy.setTransitionProbability(t1, 0.5f);
        mealy.setTransitionProbability(t2, 1.0f);

        final Collection<T> ts0 = mealy.getTransitions(s0, 0);
        final Collection<T> ts1 = mealy.getTransitions(s0, 1);
        final Collection<T> ts2 = mealy.getTransitions(s1, 0);
        final Collection<T> ts3 = mealy.getTransitions(s1, 1);

        Assert.assertEquals(ts0.size(), 1);
        Assert.assertEquals(ts1.size(), 1);
        Assert.assertEquals(ts2.size(), 0);
        Assert.assertEquals(ts3.size(), 1);

        t0 = ts0.iterator().next();
        t1 = ts1.iterator().next();
        t2 = ts3.iterator().next();

        Assert.assertEquals(mealy.getTransitionOutput(t0), 'b');
        Assert.assertEquals(mealy.getTransitionOutput(t1), 'a');
        Assert.assertEquals(mealy.getTransitionOutput(t2), 'b');

        Assert.assertEquals(mealy.getTransitionProbability(t0), 0.5f);
        Assert.assertEquals(mealy.getTransitionProbability(t1), 0.5f);
        Assert.assertEquals(mealy.getTransitionProbability(t2), 1.0f);
    }
}
