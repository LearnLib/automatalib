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
package net.automatalib.examples.modelchecking;

import java.util.Objects;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.modelcheckers.ltsmin.LTSminUtil;
import net.automatalib.modelcheckers.ltsmin.ltl.LTSminLTLIO;
import net.automatalib.modelcheckers.ltsmin.ltl.LTSminLTLIOBuilder;
import net.automatalib.modelcheckers.ltsmin.monitor.LTSminMonitorIO;
import net.automatalib.modelcheckers.ltsmin.monitor.LTSminMonitorIOBuilder;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Example for using LTSmin to perform modelchecking. Make sure to correctly setup your LTSmin installation.
 * <p>
 * This example requires at least an LTSmin version ≥ 3.1.0.
 *
 * @author frohme
 * @author Jeroen Meijer
 * @see <a href="http://ltsmin.utwente.nl">http://ltsmin.utwente.nl</a>
 * @see net.automatalib.AutomataLibProperty#LTSMIN_PATH
 */
public final class LTSminExample {

    private LTSminExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {

        final Alphabet<Character> inputAlphabet = Alphabets.characters('a', 'c');

        // @formatter:off
        MealyMachine<?, Character, ?, Character> mealy = AutomatonBuilders.<Character, Character>newMealy(inputAlphabet)
                .withInitial("q0")
                .from("q0").on('a').withOutput('1').to("q1")
                .from("q1").on('b').withOutput('2').to("q2")
                .from("q2").on('c').withOutput('3').to("q0")
                .create();
        // @formatter:on

        // do LTL model checking with Buchi automata
        final LTSminLTLIO<Character, Character> ltsminBuchi =
                new LTSminLTLIOBuilder<Character, Character>().withString2Input(s -> s.charAt(0))
                                                              .withString2Output(s -> s.charAt(0))
                                                              .withMinimumUnfolds(3)
                                                              .create();

        // We can eventually read a 'b'
        final String p1 = "X input == \"b\"";

        // Globally, whenever we output a '1' the next output must be '2'
        final String p2 = "[] (output == \"1\" -> X output == \"2\")";

        // Eventually, we observe output '2' when performing input 'a'
        final String p3 = "<> (input == \"a\" && output == \"2\")";

        // In the first step we don't see output '1'.
        final String p4 = "! output == \"1\"";

        System.out.println("performing LTL model checking with Buchi automata");
        System.out.println();

        final MealyLasso<Character, Character> ce1b =
                ltsminBuchi.findCounterExample(mealy, inputAlphabet, p1);

        System.out.println("First property is satisfied: " + Objects.isNull(ce1b));

        final MealyLasso<Character, Character> ce2b =
                ltsminBuchi.findCounterExample(mealy, inputAlphabet, p2);

        System.out.println("Second property is satisfied: " + Objects.isNull(ce2b));

        final MealyLasso<Character, Character> ce3b =
                ltsminBuchi.findCounterExample(mealy, inputAlphabet, p3);

        System.out.println("Third property is satisfied: " + Objects.isNull(ce3b));
        System.out.println("Counterexample prefix+loop: " + ce3b.getPrefix() + ':' + ce3b.getLoop());

        final MealyLasso<Character, Character> ce4b =
                ltsminBuchi.findCounterExample(mealy, inputAlphabet, p4);

        System.out.println("Fourth property is satisfied: " + Objects.isNull(ce4b));
        System.out.println("Counterexample prefix+loop: " + ce4b.getPrefix() + ':' + ce4b.getLoop());

        LTSminUtil.setCheckVersion(false);

        // do LTL model checking with monitors
        final LTSminMonitorIO<Character, Character> ltsminMonitor =
                new LTSminMonitorIOBuilder<Character, Character>()
                        .withString2Input(s -> s.charAt(0))
                        .withString2Output(s -> s.charAt(0))
                        .create();

        System.out.println();
        System.out.println("performing LTL model checking with monitors");
        System.out.println();

        final MealyMachine<?, Character, ?, Character> ce1m =
                ltsminMonitor.findCounterExample(mealy, inputAlphabet, p1);

        System.out.println("First property is satisfied: " + Objects.isNull(ce1m));

        final MealyMachine<?, Character, ?, Character> ce2m =
                ltsminMonitor.findCounterExample(mealy, inputAlphabet, p2);

        System.out.println("Second property is satisfied: " + Objects.isNull(ce2m));

        final MealyMachine<?, Character, ?, Character> ce3m =
                ltsminMonitor.findCounterExample(mealy, inputAlphabet, p3);

        System.out.println("Third property is satisfied: " + Objects.isNull(ce3m));

        final MealyMachine<?, Character, ?, Character> ce4m =
                ltsminMonitor.findCounterExample(mealy, inputAlphabet, p4);

        System.out.println("Fourth property is satisfied: " + Objects.isNull(ce4m));
        System.out.println("Counterexample length: " + (ce4m.size()));
    }
}
