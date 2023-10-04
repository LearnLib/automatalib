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
package net.automatalib.util.automata.fsa;

import java.util.Random;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class MutableDFAsTest {

    @DataProvider(name = "creators")
    public static Object[][] creators() {
        final AutomatonCreator<DFA<?, Character>, Character> compact = CompactDFA::new;
        final AutomatonCreator<DFA<?, Character>, Character> fast = FastDFA::new;

        return new Object[][] {{compact}, {fast}};
    }

    @Test(dataProvider = "creators")
    public void testComplement(AutomatonCreator<? extends MutableDFA<?, Character>, Character> creator) {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'e');
        final CompactDFA<Character> src = RandomAutomata.randomDFA(new Random(42), 15, alphabet);
        final MutableDFA<?, Character> tgt = creator.createAutomaton(alphabet, src.size());

        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, src, alphabet, tgt);

        MutableDFAs.complement(tgt, alphabet);

        final WpMethodTestsIterator<Character> iter = new WpMethodTestsIterator<>(src, alphabet);

        while (iter.hasNext()) {
            Word<Character> test = iter.next();
            Assert.assertNotEquals(tgt.accepts(test), src.accepts(test), test.toString());
        }
    }
}
