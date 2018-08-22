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
import net.automatalib.modelcheckers.ltsmin.LTSminLTLIO;
import net.automatalib.modelcheckers.ltsmin.LTSminLTLIOBuilder;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * Example for using LTSmin to perform modelchecking. Make sure to correctly setup your LTSmin installation.
 *
 * @author frohme
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

        final LTSminLTLIO<Character, Character> ltsmin =
                new LTSminLTLIOBuilder<Character, Character>().withString2Input(s -> s.charAt(0))
                                                              .withString2Output(s -> s.charAt(0))
                                                              .withMinimumUnfolds(3)
                                                              .create();

        // We can eventually read a 'b'
        final MealyLasso<Character, Character> ce1 =
                ltsmin.findCounterExample(mealy, inputAlphabet, "X input == \"b\"");

        System.out.println("First property is satisfied: " + Objects.isNull(ce1));

        // Globally, whenever we output a '1' the next output must be '2'
        final MealyLasso<Character, Character> ce2 =
                ltsmin.findCounterExample(mealy, inputAlphabet, "[] (output == \"1\" -> X output == \"2\")");

        System.out.println("Second property is satisfied: " + Objects.isNull(ce2));

        // Eventually, we observe output '2' when performing input 'a'
        final MealyLasso<Character, Character> ce3 =
                ltsmin.findCounterExample(mealy, inputAlphabet, "<> (input == \"a\" && output == \"2\")");

        System.out.println("Second property is satisfied: " + Objects.isNull(ce3));
        System.out.println("Counterexample prefix+loop: " + ce3.getPrefix() + ':' + ce3.getLoop());

    }
}
